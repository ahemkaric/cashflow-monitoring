package com.example.cashflow_monitoring.exchangerate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExchangeRateDTO(
        String currency,
        @JsonProperty("eur_rate") double eurRate,
        @JsonProperty("usd_rate") double usdRate
) {

}
