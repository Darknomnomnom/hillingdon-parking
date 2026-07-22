package com.hillingdon.parking.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StaffPlateCategoryConverter implements AttributeConverter<StaffPlate.Category, String> {

    @Override
    public String convertToDatabaseColumn(StaffPlate.Category category) {
        return category == null ? null : category.name().toLowerCase();
    }

    @Override
    public StaffPlate.Category convertToEntityAttribute(String value) {
        return value == null ? null : StaffPlate.Category.valueOf(value.toUpperCase());
    }
}
