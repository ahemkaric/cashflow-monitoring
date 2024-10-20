package com.example.cashflow_monitoring.transaction.swift;

import com.example.cashflow_monitoring.companyinfo.CompanyInfo;
import com.example.cashflow_monitoring.companyinfo.CompanyInfoService;
import com.example.cashflow_monitoring.countrydetail.CountryDetailService;
import com.example.cashflow_monitoring.exchangerate.ExchangeRate;
import com.example.cashflow_monitoring.exchangerate.ExchangeRateService;
import com.example.cashflow_monitoring.transaction.AbstractTransactionService;
import com.example.cashflow_monitoring.transaction.TransactionClient;
import com.example.cashflow_monitoring.transaction.TransactionParamsValidator;
import com.example.cashflow_monitoring.util.UrlBuilderUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SwiftTransactionService extends AbstractTransactionService<SwiftTransactionDTO, SwiftTransactionParams, SwiftTransaction> {

    public SwiftTransactionService(TransactionClient transactionClient, TransactionParamsValidator validator,
                                   UrlBuilderUtils urlBuilderUtils, SwiftTransactionMapper mapper,
                                   CompanyInfoService companyInfoService, ExchangeRateService exchangeRateService,
                                   CountryDetailService countryDetailService) {
        super(transactionClient, validator, urlBuilderUtils, mapper, companyInfoService, exchangeRateService,
                countryDetailService);
    }

    @Override
    protected void validateParams(SwiftTransactionParams params) {
        validator.validateSwiftParams(params);
    }

    @Override
    protected String buildTransactionUrl(SwiftTransactionParams params) {
        return urlBuilderUtils.buildSwiftTransactionUrl(params);
    }

    @Override
    public Mono<List<SwiftTransactionDTO>> getTransactions(String url) {
        return transactionClient.getTransactions(url, SwiftTransactionDTO[].class);
    }

    @Override
    protected SwiftTransactionParams createUpdatedParams(SwiftTransactionParams params, Integer limit, String afterUuid, String afterTimestamp) {
        return new SwiftTransactionParams(limit, afterTimestamp, afterUuid, params.beforeTimestamp(), params.issuer(), params.recipient());
    }

    @Override
    protected synchronized Mono<CompanyInfo> updateBalance(CompanyInfo companyInfo, SwiftTransaction transaction,
                                                           Mono<List<ExchangeRate>> exchangeRates, boolean isRecipient) {
        return (exchangeRateService.getTotalTransactionAmount(transaction, exchangeRates)
                .flatMap(amountInEuro -> {
                    var updatedBalance = isRecipient
                            ? companyInfo.getBalanceEur().add(amountInEuro)
                            : companyInfo.getBalanceEur().subtract(amountInEuro);
                    companyInfo.setBalanceEur(updatedBalance);
                    companyInfo.setLastSwiftTransactionId(transaction.id());
                    companyInfo.setLastSwiftTransactionTimestamp(transaction.timestamp());
                    return Mono.just(companyInfo);
                }))
                .then(countryDetailService.updateCountryDetails(companyInfo, transaction))
                .then(companyInfoService.updateCompanyInfo(companyInfo))
                .thenReturn(companyInfo);
    }

}
