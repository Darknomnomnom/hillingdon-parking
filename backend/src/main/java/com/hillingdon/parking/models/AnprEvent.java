package com.hillingdon.parking.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "anpr_events")
@Data
@NoArgsConstructor
public class AnprEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String plate;

    @Column(nullable = false)
    private Direction direction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_booking_id")
    private Booking matchedBooking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bay_id")
    private Bay bay;

    private String cameraId;

    @Column(nullable = false)
    private Instant timestamp = Instant.now();

    @Column(nullable = false)
    private boolean isSimulated = false;

    public enum Direction {
        ENTRY, EXIT
    }
}
