package com.example.cashflow_monitoring.transaction;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public interface TransactionMapper<T extends TransactionDTO, R extends Transaction> extends Function<T, R> {
    R toEntity(T transactionDTO);

    T toDto(R transaction);

    default OffsetDateTime mapTimestamp(String timestamp) {
        return OffsetDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
