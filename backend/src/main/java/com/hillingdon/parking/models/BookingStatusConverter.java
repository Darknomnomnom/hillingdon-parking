package com.hillingdon.parking.models;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BookingStatusConverter implements AttributeConverter<Booking.BookingStatus, String> {

    @Override
    public String convertToDatabaseColumn(Booking.BookingStatus status) {
        return status == null ? null : status.name().toLowerCase();
    }

    @Override
    public Booking.BookingStatus convertToEntityAttribute(String value) {
        return value == null ? null : Booking.BookingStatus.valueOf(value.toUpperCase());
    }
}
