package com.example.cashflow_monitoring.transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface Transaction {
    UUID id();

    BigDecimal amount();

    String currency();

    OffsetDateTime timestamp();

    String issuer();

    String recipient();
}
