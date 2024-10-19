package com.example.cashflow_monitoring.transaction;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionDTO {
    UUID id();

    BigDecimal amount();

    String currency();

    String timestamp();

    String issuer();

    String recipient();
}
