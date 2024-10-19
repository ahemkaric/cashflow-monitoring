package com.example.cashflow_monitoring.transaction;

import com.example.cashflow_monitoring.transaction.sepa.SepaTransactionParams;
import com.example.cashflow_monitoring.transaction.swift.SwiftTransactionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TransactionParamsValidator {

    private static final Logger log = LoggerFactory.getLogger(TransactionParamsValidator.class);

    private static void validateParams(TransactionParams params) {
        if (params.issuer() != null && params.recipient() != null) {
            throw new IllegalArgumentException("Only one of 'issuer' or 'recipient' can be provided, but not both.");
        }
        if (params.afterTimestamp() == null && params.afterUuid() != null) {
            throw new IllegalArgumentException("Timestamp is required when UUID is provided.");
        }
        if (params.afterTimestamp() != null && params.afterUuid() == null) {
            log.warn("timestamp without afteruuid");
        }
    }

    public void validateSepaParams(SepaTransactionParams params) {
        validateParams(params);
    }

    public void validateSwiftParams(SwiftTransactionParams params) {
        validateParams(params);
    }
}

