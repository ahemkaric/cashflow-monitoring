package com.example.cashflow_monitoring.exchangerate;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ExchangeRateMapper {
    ExchangeRate toEntity(ExchangeRateDTO exchangeRateDTO);

    ExchangeRateDTO toDTO(ExchangeRate exchangeRate);
}
