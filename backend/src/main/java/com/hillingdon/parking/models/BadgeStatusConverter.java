package com.hillingdon.parking.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BadgeStatusConverter implements AttributeConverter<Badge.BadgeStatus, String> {

    @Override
    public String convertToDatabaseColumn(Badge.BadgeStatus status) {
        return status == null ? null : status.name().toLowerCase();
    }

    @Override
    public Badge.BadgeStatus convertToEntityAttribute(String value) {
        return value == null ? null : Badge.BadgeStatus.valueOf(value.toUpperCase());
    }
}
