package com.hillingdon.parking.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bay_id", nullable = false)
    private Bay bay;

    @Column(nullable = false)
    private String plate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.RESERVED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VisitType visitType;

    @Column(nullable = false)
    private Instant scheduledArrival;

    private Instant arrivedAt;
    private Instant departedAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public enum BookingStatus {
        RESERVED, ARRIVED, COMPLETED, NO_SHOW, CANCELLED
    }

    public enum VisitType {
        OUTPATIENT, PLANNED_ADMISSION, OTHER
    }
}
