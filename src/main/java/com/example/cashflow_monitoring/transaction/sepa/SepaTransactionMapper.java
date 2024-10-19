package com.example.cashflow_monitoring.transaction.sepa;

import com.example.cashflow_monitoring.transaction.TransactionMapper;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface SepaTransactionMapper extends TransactionMapper<SepaTransactionDTO, SepaTransaction> {
    @Mapping(target = "timestamp", source = "timestamp")
    SepaTransaction toEntity(SepaTransactionDTO dto);

    @Mapping(target = "timestamp", expression = "java(entity.timestamp().toString())")
    SepaTransactionDTO toDto(SepaTransaction entity);
}