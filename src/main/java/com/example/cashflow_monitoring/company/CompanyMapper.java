package com.example.cashflow_monitoring.company;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CompanyMapper {
    @Mapping(target = "ibans", expression = "java(getIbansListFromString(companyDTO.ibans()))")
    Company toEntity(CompanyDTO companyDTO);

    @Mapping(target = "ibans", expression = "java(getIbansStringFromList(company.ibans()))")
    CompanyDTO toDTO(Company company);

    List<CompanyDTO> toDTOList(List<Company> company);

    default List<String> getIbansListFromString(String ibansString) {
        if (ibansString == null || ibansString.isBlank()) {
            return Collections.emptyList();
        }
        String cleanedIbansString = ibansString
                .replace("[", "")
                .replace("]", "")
                .replace("'", "")
                .trim();
        return Arrays.stream(cleanedIbansString.split(","))
                .map(String::trim)
                .toList();
    }

    default String getIbansStringFromList(List<String> ibans) {
        if (ibans == null || ibans.isEmpty()) {
            return "[]";
        }
        String quotedIbans = ibans.stream()
                .map(iban -> "'" + iban + "'")
                .collect(Collectors.joining(", "));
        return "[" + quotedIbans + "]";
    }
}