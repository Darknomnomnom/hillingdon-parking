package com.hillingdon.parking.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DirectionConverter implements AttributeConverter<AnprEvent.Direction, String> {

    @Override
    public String convertToDatabaseColumn(AnprEvent.Direction direction) {
        return direction == null ? null : direction.name().toLowerCase();
    }

    @Override
    public AnprEvent.Direction convertToEntityAttribute(String value) {
        return value == null ? null : AnprEvent.Direction.valueOf(value.toUpperCase());
    }
}
