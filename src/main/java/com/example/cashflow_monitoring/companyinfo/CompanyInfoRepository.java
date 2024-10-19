package com.example.cashflow_monitoring.companyinfo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CompanyInfoRepository extends ReactiveMongoRepository<CompanyInfo, String> {
    Mono<CompanyInfo> findByCompanyId(Integer companyId);

    Mono<CompanyInfo> findTopByOrderByCompanyIdDesc();

    Mono<CompanyInfo> findTopByOrderByLastSwiftTransactionTimestampDesc();

    Mono<CompanyInfo> findTopByOrderByLastSepaTransactionTimestampDesc();
}
