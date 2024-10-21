package com.example.cashflow_monitoring.company;

import com.example.cashflow_monitoring.exception.InternalServerErrorException;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static com.example.cashflow_monitoring.util.Constants.DEFAULT_LIMIT_FOR_REQUESTS;

@Service
public class CompanyService {
    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);
    private final CompanyClient companyClient;
    private final CompanyMapper companyMapper;

    public CompanyService(CompanyClient companyClient, CompanyMapper companyMapper) {
        this.companyClient = companyClient;
        this.companyMapper = companyMapper;
    }

    public Mono<List<Company>> getAllCompaniesPaginated(@Nullable Integer limit, @Nullable Integer afterId) {
        return fetchPaginatedCompanies(limit, afterId, new ArrayList<>())
                .onErrorMap(exception -> new InternalServerErrorException(
                        "Failed to retrieve company data. Please try again later or contact support if the problem persists.",
                        exception
                ));
    }

    private Mono<List<Company>> fetchPaginatedCompanies(@Nullable Integer limit, @Nullable Integer afterId,
                                                        List<Company> accumulatedCompanies) {
        return companyClient.getCompanies(limit, afterId)
                .flatMapMany(Flux::fromIterable)
                .map(companyMapper::toEntity)
                .collectList()
                .flatMap(companies -> {
                    accumulatedCompanies.addAll(companies);
                    if (companies.size() >= DEFAULT_LIMIT_FOR_REQUESTS) {
                        var lastCompanyId = companies.getLast().id();
                        return fetchPaginatedCompanies(limit != null ? limit - DEFAULT_LIMIT_FOR_REQUESTS : null,
                                lastCompanyId, accumulatedCompanies);
                    }
                    return Mono.just(accumulatedCompanies);
                });
    }

    public Mono<Company> getCompanyById(Integer companyId) {
        return companyClient.getCompanyById(companyId)
                .map(companyMapper::toEntity)
                .doOnError(e -> log.error("Error mapping CompanyDTO to Company for ID {}", companyId, e));
    }

}