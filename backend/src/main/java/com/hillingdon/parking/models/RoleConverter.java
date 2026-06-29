package com.hillingdon.parking.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<User.Role, String> {

    @Override
    public String convertToDatabaseColumn(User.Role role) {
        return role == null ? null : role.name().toLowerCase();
    }

    @Override
    public User.Role convertToEntityAttribute(String value) {
        return value == null ? null : User.Role.valueOf(value.toUpperCase());
    }
}
