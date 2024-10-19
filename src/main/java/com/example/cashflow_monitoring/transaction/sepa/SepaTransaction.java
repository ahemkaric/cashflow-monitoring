package com.example.cashflow_monitoring.transaction.sepa;

import com.example.cashflow_monitoring.transaction.Transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SepaTransaction(UUID id, String payer, String receiver, BigDecimal amount, String currency,
                              OffsetDateTime timestamp) implements Transaction {
    @Override
    public String issuer() {
        return this.payer();
    }

    @Override
    public String recipient() {
        return this.receiver();
    }

}

