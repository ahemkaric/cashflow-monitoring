package com.example.cashflow_monitoring.exchangerate;

public record ExchangeRate(
        String currency,
        double eurRate,
        double usdRate
) {

}
