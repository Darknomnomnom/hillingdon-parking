package com.hillingdon.parking.dto;

import com.hillingdon.parking.models.Booking;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class BookingResponse {

    private UUID id;
    private String confirmationCode;
    private String plate;
    private Booking.BookingStatus status;
    private Booking.VisitType visitType;
    private Instant appointmentTime;
    private Instant arrivalWindowStart;
    private Instant arrivalWindowEnd;
    private String notes;
    private Instant createdAt;

    // Patient
    private UUID patientId;
    private String patientName;

    // Bay (nullable — no bay if none available)
    private UUID bayId;
    private String bayNumber;
    private String bayType;
    private String floorName;

    public static BookingResponse from(Booking b) {
        BookingResponse r = new BookingResponse();
        r.setId(b.getId());
        r.setConfirmationCode(b.getConfirmationCode());
        r.setPlate(b.getPlate());
        r.setStatus(b.getStatus());
        r.setVisitType(b.getVisitType());
        r.setAppointmentTime(b.getAppointmentTime());
        r.setArrivalWindowStart(b.getArrivalWindowStart());
        r.setArrivalWindowEnd(b.getArrivalWindowEnd());
        r.setNotes(b.getNotes());
        r.setCreatedAt(b.getCreatedAt());

        if (b.getPatient() != null) {
            r.setPatientId(b.getPatient().getId());
            r.setPatientName(b.getPatient().getFirstName() + " " + b.getPatient().getLastName());
        }

        if (b.getBay() != null) {
            r.setBayId(b.getBay().getId());
            r.setBayNumber(b.getBay().getBayNumber());
            r.setBayType(b.getBay().getType().name());
            if (b.getBay().getFloor() != null) {
                r.setFloorName(b.getBay().getFloor().getName());
            }
        }

        return r;
    }
}
