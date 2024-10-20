package com.example.cashflow_monitoring.companyinfo;

import com.example.cashflow_monitoring.company.CompanyService;
import com.example.cashflow_monitoring.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

@Service
public class CompanyInfoService {

    private static final Logger log = LoggerFactory.getLogger(CompanyInfoService.class);
    private static final String CACHE_KEY_PREFIX = "companyInfo:";
    private final CompanyService companyService;
    private final CompanyInfoRepository companyInfoRepository;
    private final ReactiveRedisTemplate<String, CompanyInfo> redisTemplate;

    public CompanyInfoService(CompanyService companyService, CompanyInfoRepository companyInfoRepository,
                              ReactiveRedisTemplate<String, CompanyInfo> redisTemplate) {
        this.companyService = companyService;
        this.companyInfoRepository = companyInfoRepository;
        this.redisTemplate = redisTemplate;
    }

    public Mono<CompanyInfo> getCachedCompanyInfoByCompanyId(Integer companyId) {
        var cacheKey = CACHE_KEY_PREFIX + companyId.toString();
        return redisTemplate.opsForValue()
                .get(cacheKey)
                .flatMap(cachedCompanyInfo -> {
                    log.info("Fetched company info from cache for companyId: {}", companyId);
                    return Mono.just(cachedCompanyInfo);
                })
                .switchIfEmpty(
                        getCompanyInfoByCompanyId(companyId)
                                .flatMap(companyInfo -> redisTemplate.opsForValue()
                                        .set(cacheKey, companyInfo, Duration.ofMinutes(10))
                                        .thenReturn(companyInfo))
                )
                .onErrorResume(error -> {
                    log.error("Redis cache unavailable, falling back to MongoDB", error);
                    return getCompanyInfoByCompanyId(companyId);
                });
    }

    public Mono<CompanyInfo> getCompanyInfoByCompanyId(Integer companyId) {
        return companyInfoRepository.findByCompanyId(companyId)
                .doOnError(error -> log.error("Error fetching company info for ID {}: {}", companyId, error.getMessage()));
    }

    public Mono<CompanyInfo> createCompanyInfoWithCompanyId(Integer companyId) {
        var companyInfo = new CompanyInfo();
        companyInfo.setCompanyId(companyId);
        return updateCompanyInfo(companyInfo);
    }

    public Mono<BigDecimal> getCompanyBalanceByCompanyId(Integer companyId) {
        log.info("Fetching balance for company ID: {}", companyId);
        return getCompanyInfoByCompanyId(companyId)
                .switchIfEmpty(Mono.error(new NotFoundException("Could not find companyInfo for this CompanyId")))
                .map(CompanyInfo::getBalanceEur)
                .doOnSuccess(result -> log.info("Successfully processed company balance: {}", result))
                .doOnError(error -> log.error("Error processing company balance: {}", error.getMessage()));
    }

    public Mono<CompanyInfo> updateCompanyInfo(CompanyInfo companyInfo) {
        var cacheKey = CACHE_KEY_PREFIX + companyInfo.getCompanyId().toString();
        return saveCompanyInfo(companyInfo)
                .flatMap(savedInfo ->
                        redisTemplate
                                .opsForValue()
                                .set(cacheKey, savedInfo, Duration.ofMinutes(10))
                                .thenReturn(savedInfo)
                );
    }

    public Mono<CompanyInfo> saveCompanyInfo(CompanyInfo companyInfo) {
        return companyInfoRepository.save(companyInfo);
    }

    public Flux<CompanyInfo> findAllCompanyInfo() {
        return companyInfoRepository.findAll();
    }

    public Mono<List<CompanyInfo>> getAndSaveNewCompanies(Integer limit) {
        log.info("Fetching and saving company IDs with limit: {}", limit);
        return getTopByOrderByCompanyIdDesc()
                .map(CompanyInfo::getCompanyId)
                .defaultIfEmpty(0)
                .doOnSuccess(maxCompanyId -> log.debug("Max company ID fetched: {}", maxCompanyId))
                .flatMap(maxCompanyId -> companyService.getAllCompaniesPaginated(limit, maxCompanyId)
                        .flatMapMany(Flux::fromIterable)
                        .flatMap(company -> createCompanyInfoWithCompanyId(company.id()))
                        .collectList());
    }

    private Mono<CompanyInfo> getTopByOrderByCompanyIdDesc() {
        return companyInfoRepository.findTopByOrderByCompanyIdDesc();
    }

    public Mono<CompanyInfo> findTopByOrderByLastSepaTransactionTimestampDesc() {
        return companyInfoRepository.findTopByOrderByLastSepaTransactionTimestampDesc()
                .doOnNext(info -> log.info("Company info found: {}", info))
                .switchIfEmpty(Mono.defer(() -> Mono.just(new CompanyInfo())));
    }

    public Mono<CompanyInfo> findTopByOrderByLastSwiftTransactionTimestampDesc() {
        return companyInfoRepository.findTopByOrderByLastSwiftTransactionTimestampDesc()
                .switchIfEmpty(Mono.defer(() -> {
                    var defaultCompanyInfo = new CompanyInfo();
                    return Mono.just(defaultCompanyInfo);
                }));
    }

    public Mono<Long> evictCache(Integer companyId) {
        var cacheKey = CACHE_KEY_PREFIX + companyId;
        return redisTemplate.delete(cacheKey);
    }
}

