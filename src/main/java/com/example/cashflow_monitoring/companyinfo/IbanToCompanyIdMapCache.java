package com.example.cashflow_monitoring.companyinfo;

import com.example.cashflow_monitoring.company.CompanyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class IbanToCompanyIdMapCache {

    private static final Logger log = LoggerFactory.getLogger(IbanToCompanyIdMapCache.class);
    private final CompanyInfoService companyInfoService;
    private final CompanyService companyService;
    private Mono<Map<String, Integer>> ibanToCompanyIdMap;

    public IbanToCompanyIdMapCache(CompanyInfoService companyInfoService, CompanyService companyService) {
        this.companyInfoService = companyInfoService;
        this.companyService = companyService;
    }

    public Mono<Map<String, Integer>> getIbanToCompanyIdMap() {
        if (ibanToCompanyIdMap == null) {
            ibanToCompanyIdMap = createIbanCompanyIdMap().cache();
        }
        return ibanToCompanyIdMap;
    }

    private Mono<Map<String, Integer>> createIbanCompanyIdMap() {
        return companyInfoService.findAllCompanyInfo()
                .flatMap(companyInfo -> companyService.getCompanyById(companyInfo.getCompanyId())
                        .flatMapMany(company -> Flux.fromIterable(company.ibans())
                                .map(iban -> Map.entry(iban, companyInfo.getCompanyId()))
                        ))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .doOnNext(map -> log.info("Created IBAN to Company ID map: {}", map));
    }
}
