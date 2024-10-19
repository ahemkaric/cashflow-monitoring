package com.example.cashflow_monitoring.transaction.swift;

import com.example.cashflow_monitoring.transaction.TransactionMapper;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface SwiftTransactionMapper extends TransactionMapper<SwiftTransactionDTO, SwiftTransaction> {
    @Mapping(target = "timestamp", source = "timestamp")
    SwiftTransaction toEntity(SwiftTransactionDTO dto);

    @Mapping(target = "timestamp", expression = "java(entity.timestamp().toString())")
    SwiftTransactionDTO toDto(SwiftTransaction entity);
}
