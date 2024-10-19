package com.example.cashflow_monitoring.transaction;

import com.example.cashflow_monitoring.transaction.sepa.SepaTransaction;
import com.example.cashflow_monitoring.transaction.swift.SwiftTransaction;

import java.util.List;

public record TransactionResponseDTO(
        List<SepaTransaction> sepaTransactions,
        List<SwiftTransaction> swiftTransactions
) {
}
