package com.example.cashflow_monitoring.countrydetail;

import com.example.cashflow_monitoring.companyinfo.CompanyInfo;
import com.example.cashflow_monitoring.companyinfo.CompanyInfoService;
import com.example.cashflow_monitoring.exception.NotFoundException;
import com.example.cashflow_monitoring.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class CountryDetailService {

    private static final Logger log = LoggerFactory.getLogger(CountryDetailService.class);
    private final CompanyInfoService companyInfoService;

    public CountryDetailService(CompanyInfoService companyInfoService) {
        this.companyInfoService = companyInfoService;
    }

    public void addCountryDetails(CompanyInfo companyInfo, String currency) {
        companyInfo.getCountryDetails().add(new CountryDetail(currency, 1));
    }

    public void updateCountryDetails(CountryDetail countryDetail) {
        countryDetail.setNumberOfTransactions(countryDetail.getNumberOfTransactions() + 1);
    }

    public synchronized <R extends Transaction> Mono<CompanyInfo> updateCountryDetails(CompanyInfo companyInfo, R transaction) {
        return Mono.just(companyInfo)
                .flatMap(info -> {
                    findCountryDetail(info, transaction.currency())
                            .ifPresentOrElse(
                                    this::updateCountryDetails,
                                    () -> addCountryDetails(info, transaction.currency())
                            );
                    return Mono.just(info);
                });
    }

    private Optional<CountryDetail> findCountryDetail(CompanyInfo companyInfo, String currency) {
        return companyInfo.getCountryDetails().stream()
                .filter(countryDetails -> countryDetails.getCountryCode().equals(currency))
                .findFirst();
    }

    public Mono<List<CountryDetail>> getCountryDetailsById(Integer companyId) {
        log.info("Fetching country details for company ID: {}", companyId);
        return companyInfoService.getCompanyInfoByCompanyId(companyId)
                .switchIfEmpty(Mono.error(new NotFoundException("Could not find companyInfo for this CompanyId")))
                .map(companyInfo -> {
                    List<CountryDetail> sortedCountryDetails = companyInfo.getCountryDetails().stream()
                            .sorted(Comparator.comparingInt(CountryDetail::getNumberOfTransactions).reversed())
                            .toList();
                    companyInfo.setCountryDetails(sortedCountryDetails);
                    return companyInfo;
                })
                .map(CompanyInfo::getCountryDetails);
    }
}
