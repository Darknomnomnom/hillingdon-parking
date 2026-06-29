package com.hillingdon.parking.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class VisitTypeConverter implements AttributeConverter<Booking.VisitType, String> {

    @Override
    public String convertToDatabaseColumn(Booking.VisitType type) {
        return type == null ? null : type.name().toLowerCase();
    }

    @Override
    public Booking.VisitType convertToEntityAttribute(String value) {
        return value == null ? null : Booking.VisitType.valueOf(value.toUpperCase());
    }
}
