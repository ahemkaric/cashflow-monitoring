package com.example.cashflow_monitoring.transaction;

public interface TransactionParams {

    Integer limit();

    String afterTimestamp();

    String afterUuid();

    String beforeTimestamp();

    String issuer();

    String recipient();
}
