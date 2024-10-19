package com.example.cashflow_monitoring.exchangerate;

import com.example.cashflow_monitoring.exception.ExchangeRateNotFoundException;
import com.example.cashflow_monitoring.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.example.cashflow_monitoring.util.Constants.EUR;

@Service
public class ExchangeRateService {
    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);
    private final ExchangeRateClient exchangeRateClient;
    private final ExchangeRateMapper exchangeRateMapper;

    public ExchangeRateService(ExchangeRateClient exchangeRateClient, ExchangeRateMapper exchangeRateMapper) {
        this.exchangeRateClient = exchangeRateClient;
        this.exchangeRateMapper = exchangeRateMapper;
    }

    public Mono<List<ExchangeRate>> getExchangeRates() {
        return exchangeRateClient.getExchangeRates()
                .flatMapMany(Flux::fromIterable)
                .map(exchangeRateMapper::toEntity)
                .collectList()
                .doOnError(e -> log.error("Error fetching exchange rates: {}", e.getMessage()));
    }

    public <R extends Transaction> Mono<BigDecimal> getTotalTransactionAmount(R transaction, Mono<List<ExchangeRate>> exchangeRates) {
        return exchangeRates.flatMap(rates -> calculateAmountInEUR(transaction, rates))
                .doOnError(e -> log.error("Error calculating transaction amount: {}", e.getMessage()));
    }

    private <R extends Transaction> Mono<BigDecimal> calculateAmountInEUR(R transaction, List<ExchangeRate> rates) {
        if (EUR.equals(transaction.currency())) {
            return Mono.just(transaction.amount());
        }
        return Mono.justOrEmpty(findExchangeRate(rates, transaction.currency()))
                .map(exchangeRate -> transaction.amount().multiply(BigDecimal.valueOf(exchangeRate.eurRate())))
                .switchIfEmpty(Mono.error(new ExchangeRateNotFoundException(transaction.currency())));
    }

    private Optional<ExchangeRate> findExchangeRate(List<ExchangeRate> rates, String currency) {
        return rates.stream()
                .filter(rate -> rate.currency().equals(currency))
                .findFirst();
    }

}
