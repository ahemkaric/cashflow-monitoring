package com.example.cashflow_monitoring.transaction.swift;

import com.example.cashflow_monitoring.transaction.TransactionParams;

public record SwiftTransactionParams(
        Integer limit,
        String afterTimestamp,
        String afterUuid,
        String beforeTimestamp,
        String sender,
        String beneficiary
) implements TransactionParams {
    @Override
    public String issuer() {
        return this.sender();
    }

    @Override
    public String recipient() {
        return this.beneficiary();
    }
}
