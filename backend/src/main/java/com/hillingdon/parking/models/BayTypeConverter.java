package com.hillingdon.parking.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BayTypeConverter implements AttributeConverter<Bay.BayType, String> {

    @Override
    public String convertToDatabaseColumn(Bay.BayType type) {
        return type == null ? null : type.name().toLowerCase();
    }

    @Override
    public Bay.BayType convertToEntityAttribute(String value) {
        return value == null ? null : Bay.BayType.valueOf(value.toUpperCase());
    }
}
