package com.hillingdon.parking.dto;

import com.hillingdon.parking.models.Booking;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateBookingRequest {

    @NotBlank
    private String plate;

    @NotNull
    private Booking.VisitType visitType;

    @NotNull
    @Future
    private Instant appointmentTime;

    private boolean needsAccessible;

    private String notes;
}
