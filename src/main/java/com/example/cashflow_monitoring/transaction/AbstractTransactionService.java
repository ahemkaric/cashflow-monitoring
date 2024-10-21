package com.example.cashflow_monitoring.transaction;

import com.example.cashflow_monitoring.companyinfo.CompanyInfo;
import com.example.cashflow_monitoring.companyinfo.CompanyInfoService;
import com.example.cashflow_monitoring.countrydetail.CountryDetailService;
import com.example.cashflow_monitoring.exchangerate.ExchangeRate;
import com.example.cashflow_monitoring.exchangerate.ExchangeRateService;
import com.example.cashflow_monitoring.util.UrlBuilderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.cashflow_monitoring.util.Constants.DEFAULT_LIMIT_FOR_REQUESTS;

public abstract class AbstractTransactionService<T extends TransactionDTO, P extends TransactionParams, R extends Transaction>
        implements TransactionService<P, R> {
    private static final Logger log = LoggerFactory.getLogger(AbstractTransactionService.class);
    protected final TransactionClient transactionClient;
    protected final TransactionParamsValidator validator;
    protected final UrlBuilderUtils urlBuilderUtils;
    protected final TransactionMapper<T, R> transactionMapper;
    protected final CompanyInfoService companyInfoService;
    protected final ExchangeRateService exchangeRateService;
    protected final CountryDetailService countryDetailService;

    protected AbstractTransactionService(TransactionClient transactionClient, TransactionParamsValidator validator,
                                         UrlBuilderUtils urlBuilderUtils, TransactionMapper<T, R> transactionMapper,
                                         CompanyInfoService companyInfoService, ExchangeRateService exchangeRateService,
                                         CountryDetailService countryDetailService) {
        this.transactionClient = transactionClient;
        this.validator = validator;
        this.urlBuilderUtils = urlBuilderUtils;
        this.transactionMapper = transactionMapper;
        this.companyInfoService = companyInfoService;
        this.exchangeRateService = exchangeRateService;
        this.countryDetailService = countryDetailService;
    }

    protected abstract void validateParams(P params);

    protected abstract String buildTransactionUrl(P params);

    protected abstract Mono<List<T>> getTransactions(String url);

    protected abstract P createUpdatedParams(P params, Integer limit, String afterUuid, String afterTimestamp);

    protected abstract Mono<CompanyInfo> updateBalance(CompanyInfo companyInfo, R transaction,
                                                       Mono<List<ExchangeRate>> exchangeRates, boolean isRecipient);

    @Override
    public Mono<List<R>> getTransactions(P params) {
        validateParams(params);
        var url = buildTransactionUrl(params);
        return fetchTransactions(url);
    }

    private Mono<List<R>> fetchTransactions(String url) {
        return getTransactions(url)
                .flatMapMany(Flux::fromIterable)
                .map(transactionMapper::toEntity)
                .collectList();
    }

    @Override
    public Mono<List<R>> getPaginatedTransactions(P params) {
        List<R> accumulatedTransactions = new ArrayList<>();
        return getPaginatedTransactions(params, accumulatedTransactions)
                .thenReturn(accumulatedTransactions);
    }

    private Mono<Void> getPaginatedTransactions(P params, List<R> accumulatedTransactions) {
        return getTransactions(params)
                .flatMap(transactions -> queryTransactions(params, accumulatedTransactions, transactions))
                .onErrorResume(error -> Mono.empty());
    }

    private Mono<Void> queryTransactions(P params, List<R> accumulatedTransactions, List<R> transactions) {
        return Mono.just(transactions)
                .flatMap(trans -> filteredTransactions(trans, params.beforeTimestamp())
                        .filter(filteredTransactions -> !filteredTransactions.isEmpty())
                        .flatMap(filteredTransactions -> {
                            accumulatedTransactions.addAll(filteredTransactions);
                            log.info("accumulatedTransactions size: {}", accumulatedTransactions.size());
                            return handlePagination(params, accumulatedTransactions, filteredTransactions);
                        }));
    }

    private Mono<Void> handlePagination(P params, List<R> accumulatedTransactions, List<R> filteredTransactions) {
        var lastTransaction = filteredTransactions.getLast();
        var newLimit = Math.max(0, params.limit() - DEFAULT_LIMIT_FOR_REQUESTS);
        var newAfterUuid = lastTransaction.id();
        var newAfterTimestamp = lastTransaction.timestamp();

        if (filteredTransactions.size() >= DEFAULT_LIMIT_FOR_REQUESTS) {
            var updatedParams = updateParamsWithPagination(params, newLimit, String.valueOf(newAfterUuid),
                    String.valueOf(newAfterTimestamp));
            return getPaginatedTransactions(updatedParams, accumulatedTransactions);
        }
        return Mono.empty();
    }

    private Mono<List<R>> filteredTransactions(List<R> transactions, String beforeTimestamp) {
        return Mono.just(transactions.stream()
                .filter(transaction -> beforeTimestamp == null ||
                        transaction.timestamp()
                                .isBefore(OffsetDateTime.parse(beforeTimestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME)))
                .toList());
    }

    private P updateParamsWithPagination(P params, Integer limit, String afterUuid, String afterTimestamp) {
        return createUpdatedParams(params, limit, afterUuid, afterTimestamp);
    }

    @Override
    public Mono<Void> processTransaction(R transaction, Mono<List<ExchangeRate>> exchangeRates,
                                         Mono<Map<String, Integer>> ibanToCompanyIdMap) {
        return ibanToCompanyIdMap
                .flatMap(ibanCompanyIdMap -> updateCompanyInfoWithTransaction(ibanCompanyIdMap, transaction, exchangeRates));
    }

    private Mono<Void> updateCompanyInfoWithTransaction(Map<String, Integer> ibanCompanyIdMap, R transaction,
                                                        Mono<List<ExchangeRate>> exchangeRates) {
        var optionalIssuerCompanyId = Optional.ofNullable(ibanCompanyIdMap.get(transaction.issuer()));
        var optionalRecipientCompanyId = Optional.ofNullable(ibanCompanyIdMap.get(transaction.recipient()));

        var issuerCompanyInfo = optionalIssuerCompanyId
                .map(issuerCompanyId -> updateCompanyInfo(transaction, issuerCompanyId, exchangeRates, false))
                .orElse(Mono.empty());

        var recipientCompanyInfo = optionalRecipientCompanyId
                .map(recipientCompanyId -> updateCompanyInfo(transaction, recipientCompanyId, exchangeRates, true))
                .orElse(Mono.empty());

        return Mono.when(issuerCompanyInfo, recipientCompanyInfo).then();
    }

    private synchronized Mono<CompanyInfo> updateCompanyInfo(R transaction, Integer companyId,
                                                             Mono<List<ExchangeRate>> exchangeRates, boolean isRecipient) {
        return companyInfoService.getCachedCompanyInfoByCompanyId(companyId)
                .flatMap(companyInfo -> {
                    if ((companyInfo.getLastSepaTransactionId() != null && companyInfo.getLastSepaTransactionId().equals(transaction.id())) ||
                            (companyInfo.getLastSwiftTransactionId() != null && companyInfo.getLastSwiftTransactionId().equals(transaction.id()))) {
                        return Mono.just(companyInfo);
                    }
                    return updateBalance(companyInfo, transaction, exchangeRates, isRecipient);
                });
    }

}
