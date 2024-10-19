package com.example.cashflow_monitoring.transaction;

import com.example.cashflow_monitoring.exchangerate.ExchangeRate;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface TransactionService<P extends TransactionParams, R extends Transaction> {
    Mono<List<R>> getTransactions(P params);

    Mono<List<R>> getPaginatedTransactions(P params);

    Mono<Void> processTransaction(R transaction, Mono<List<ExchangeRate>> exchangeRates,
                                  Mono<Map<String, Integer>> ibanToCompanyIdMap);
}
