package com.example.cashflow_monitoring.transaction.sepa;

import com.example.cashflow_monitoring.transaction.TransactionParams;

public record SepaTransactionParams(
        Integer limit,
        String afterTimestamp,
        String afterUuid,
        String beforeTimestamp,
        String payer,
        String receiver
) implements TransactionParams {
    @Override
    public String issuer() {
        return this.payer();
    }

    @Override
    public String recipient() {
        return this.receiver();
    }
}
