package com.example.cashflow_monitoring.config.mongo;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@WritingConverter
public class OffsetDateTimeToStringConverter implements Converter<OffsetDateTime, String> {

    @Override
    public String convert(OffsetDateTime source) {
        return source.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
