package com.example.cashflow_monitoring.exception;

public class ExchangeRateNotFoundException extends RuntimeException {
    public ExchangeRateNotFoundException(String currency) {
        super("Exchange rate not found for currency: " + currency);
    }
}
