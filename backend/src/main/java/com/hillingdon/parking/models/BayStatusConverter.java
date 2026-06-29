package com.hillingdon.parking.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BayStatusConverter implements AttributeConverter<Bay.BayStatus, String> {

    @Override
    public String convertToDatabaseColumn(Bay.BayStatus status) {
        return status == null ? null : status.name().toLowerCase();
    }

    @Override
    public Bay.BayStatus convertToEntityAttribute(String value) {
        return value == null ? null : Bay.BayStatus.valueOf(value.toUpperCase());
    }
}
