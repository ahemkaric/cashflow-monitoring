package com.example.cashflow_monitoring.companyinfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataMongoTest
class CompanyInfoRepositoryTest {
    @Autowired
    private CompanyInfoRepository companyInfoRepository;

    @BeforeEach
    void setup() {
        companyInfoRepository.deleteAll().block();
    }

    @Test
    void testFindByCompanyId() {
        Integer companyId = (int) (Math.random() * 10);
        CompanyInfo companyInfo = new CompanyInfo();
        companyInfo.setCompanyId(companyId);
        companyInfoRepository.save(companyInfo).block();

        Mono<CompanyInfo> foundCompanyInfo = companyInfoRepository.findByCompanyId(companyId);

        StepVerifier.create(foundCompanyInfo)
                .assertNext(info -> {
                    assertEquals(companyId, info.getCompanyId());
                })
                .verifyComplete();
    }

    @Test
    void shouldSaveSingleCompanyInfo() {
        Integer companyId = (int) (Math.random() * 10);
        CompanyInfo companyInfo = new CompanyInfo();
        companyInfo.setCompanyId(companyId);
        Publisher<CompanyInfo> setup = companyInfoRepository.save(companyInfo);
        StepVerifier
                .create(setup)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testFindTopByOrderByCompanyIdDesc() {
        CompanyInfo company1 = new CompanyInfo();
        int companyId1 = 101;
        company1.setCompanyId(companyId1);
        companyInfoRepository.save(company1).block();

        CompanyInfo company2 = new CompanyInfo();
        int companyId = 202;
        company2.setCompanyId(companyId);
        companyInfoRepository.save(company2).block();

        Mono<CompanyInfo> topCompanyInfo = companyInfoRepository.findTopByOrderByCompanyIdDesc();

        StepVerifier.create(topCompanyInfo)
                .assertNext(info -> {
                    assertEquals(companyId, info.getCompanyId());
                })
                .verifyComplete();
    }
}