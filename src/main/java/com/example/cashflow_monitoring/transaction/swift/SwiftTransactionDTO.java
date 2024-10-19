package com.example.cashflow_monitoring.transaction.swift;

import com.example.cashflow_monitoring.transaction.TransactionDTO;

import java.math.BigDecimal;
import java.util.UUID;

public record SwiftTransactionDTO(
        UUID id,
        String sender,
        String beneficiary,
        BigDecimal amount,
        String currency,
        String timestamp
) implements TransactionDTO {
    @Override
    public String issuer() {
        return this.sender();
    }

    @Override
    public String recipient() {
        return this.beneficiary();
    }

}
