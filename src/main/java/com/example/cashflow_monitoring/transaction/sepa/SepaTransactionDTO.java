package com.example.cashflow_monitoring.transaction.sepa;

import com.example.cashflow_monitoring.transaction.TransactionDTO;

import java.math.BigDecimal;
import java.util.UUID;

public record SepaTransactionDTO(
        UUID id,
        String payer,
        String receiver,
        BigDecimal amount,
        String currency,
        String timestamp
) implements TransactionDTO {
    @Override
    public String issuer() {
        return this.payer();
    }

    @Override
    public String recipient() {
        return this.receiver();
    }
}
