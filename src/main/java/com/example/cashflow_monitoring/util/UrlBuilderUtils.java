package com.example.cashflow_monitoring.util;

import com.example.cashflow_monitoring.transaction.TransactionParams;
import com.example.cashflow_monitoring.transaction.sepa.SepaTransactionParams;
import com.example.cashflow_monitoring.transaction.swift.SwiftTransactionParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;
import java.util.function.Consumer;

import static com.example.cashflow_monitoring.util.Constants.*;

@Service
public class UrlBuilderUtils {

    private final String externalApiBaseUrl;

    public UrlBuilderUtils(@Value("${external.db.api.base-url}") String externalApiBaseUrl) {
        this.externalApiBaseUrl = externalApiBaseUrl;
    }

    private static void addQueryParamIfPresent(UriComponentsBuilder builder, String paramName, Object paramValue) {
        Optional.ofNullable(paramValue)
                .ifPresent(value -> builder.queryParam(paramName, value));
    }

    private static void setLimitQueryParam(Integer limit, UriComponentsBuilder builder) {
        var effectiveLimit = limit != null ? Math.min(limit, DEFAULT_LIMIT_FOR_REQUESTS) : DEFAULT_LIMIT_FOR_REQUESTS;
        builder.queryParam(LIMIT, effectiveLimit);
    }

    private static void addQueryFromTransactionParams(UriComponentsBuilder builder, TransactionParams params) {
        setLimitQueryParam(params.limit(), builder);
        addQueryParamIfPresent(builder, AFTER_TIMESTAMP, params.afterTimestamp());
        addQueryParamIfPresent(builder, AFTER_UUID, params.afterUuid());
    }

    public String buildCompanyUrl(Integer companyId) {
        return UriComponentsBuilder.fromHttpUrl(externalApiBaseUrl)
                .pathSegment(COMPANIES, companyId.toString())
                .toUriString();
    }

    public String buildCompaniesUrl(Integer limit, Integer afterId) {
        var builder = UriComponentsBuilder.fromHttpUrl(externalApiBaseUrl)
                .pathSegment(COMPANIES);
        setLimitQueryParam(limit, builder);
        addQueryParamIfPresent(builder, AFTER_ID, afterId);
        return builder.toUriString();
    }

    public String buildExchangeRateUrl() {
        var builder = UriComponentsBuilder.fromHttpUrl(externalApiBaseUrl)
                .pathSegment(EXCHANGE_RATES);
        return builder.toUriString();
    }

    private String buildTransactionUrl(String transactionType, TransactionParams transactionParams,
                                       Consumer<UriComponentsBuilder> addQueryParams) {
        var builder = UriComponentsBuilder.fromHttpUrl(externalApiBaseUrl)
                .pathSegment(TRANSACTIONS_PATH, transactionType);
        addQueryFromTransactionParams(builder, transactionParams);
        addQueryParams.accept(builder);
        return builder.toUriString();
    }

    public String buildSepaTransactionUrl(SepaTransactionParams sepaTransactionParams) {
        return buildTransactionUrl(SEPA, sepaTransactionParams,
                builder -> addQueryParamsForSepa(builder, sepaTransactionParams));
    }

    public String buildSwiftTransactionUrl(SwiftTransactionParams swiftTransactionParams) {
        return buildTransactionUrl(SWIFT, swiftTransactionParams,
                builder -> addQueryParamsForSwift(builder, swiftTransactionParams));
    }

    private void addQueryParamsForSepa(UriComponentsBuilder builder, SepaTransactionParams params) {
        addQueryParamIfPresent(builder, PAYER, params.payer());
        addQueryParamIfPresent(builder, RECEIVER, params.receiver());
    }

    private void addQueryParamsForSwift(UriComponentsBuilder builder, SwiftTransactionParams params) {
        addQueryParamIfPresent(builder, SENDER, params.sender());
        addQueryParamIfPresent(builder, BENEFICIARY, params.beneficiary());
    }

}
