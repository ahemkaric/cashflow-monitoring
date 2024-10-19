package com.example.cashflow_monitoring.transaction.swift;

import com.example.cashflow_monitoring.transaction.Transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SwiftTransaction(UUID id, String sender, String beneficiary, BigDecimal amount, String currency,
                               OffsetDateTime timestamp) implements Transaction {
    @Override
    public String issuer() {
        return this.sender();
    }

    @Override
    public String recipient() {
        return this.beneficiary();
    }
}
