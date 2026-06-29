package com.hillingdon.parking.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bay_id")
    private Bay bay;

    @Column(nullable = false)
    private String plate;

    @Column(nullable = false)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(nullable = false)
    private VisitType visitType;

    @Column(nullable = false)
    private Instant appointmentTime;

    @Column(nullable = false)
    private Instant arrivalWindowStart;

    @Column(nullable = false)
    private Instant arrivalWindowEnd;

    @Column(nullable = false, updatable = false)
    private String confirmationCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    public enum BookingStatus {
        PENDING, CONFIRMED, ARRIVED, COMPLETED, CANCELLED, NO_SHOW
    }

    public enum VisitType {
        OUTPATIENT, PLANNED_ADMISSION, OTHER
    }
}
