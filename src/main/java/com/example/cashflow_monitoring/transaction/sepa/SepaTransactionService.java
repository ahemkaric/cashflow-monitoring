package com.example.cashflow_monitoring.transaction.sepa;

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
public class SepaTransactionService extends AbstractTransactionService<SepaTransactionDTO, SepaTransactionParams, SepaTransaction> {


    public SepaTransactionService(TransactionClient transactionClient, TransactionParamsValidator validator,
                                  UrlBuilderUtils urlBuilderUtils, SepaTransactionMapper mapper,
                                  CompanyInfoService companyInfoService, ExchangeRateService exchangeRateService,
                                  CountryDetailService countryDetailService) {
        super(transactionClient, validator, urlBuilderUtils, mapper, companyInfoService, exchangeRateService, countryDetailService);
    }

    @Override
    protected void validateParams(SepaTransactionParams params) {
        validator.validateSepaParams(params);
    }

    @Override
    protected String buildTransactionUrl(SepaTransactionParams params) {
        return urlBuilderUtils.buildSepaTransactionUrl(params);
    }

    @Override
    public Mono<List<SepaTransactionDTO>> getTransactions(String url) {
        return transactionClient.getTransactions(url, SepaTransactionDTO[].class);
    }

    @Override
    protected SepaTransactionParams createUpdatedParams(SepaTransactionParams params, Integer limit, String afterUuid, String afterTimestamp) {
        return new SepaTransactionParams(limit, afterTimestamp, afterUuid, params.beforeTimestamp(), params.issuer(), params.recipient());
    }

    @Override
    protected synchronized Mono<CompanyInfo> updateBalance(CompanyInfo companyInfo, SepaTransaction transaction,
                                                           Mono<List<ExchangeRate>> exchangeRates, boolean isRecipient) {
        BigDecimal updatedBalance = isRecipient
                ? companyInfo.getBalanceEur().add(transaction.amount())
                : companyInfo.getBalanceEur().subtract(transaction.amount());

        companyInfo.setBalanceEur(updatedBalance);
        companyInfo.setLastSepaTransactionId(transaction.id());
        companyInfo.setLastSepaTransactionTimestamp(transaction.timestamp());

        return countryDetailService.updateCountryDetails(companyInfo, transaction)
                .then(companyInfoService.updateCompanyInfo(companyInfo))
                .thenReturn(companyInfo);
    }
}
