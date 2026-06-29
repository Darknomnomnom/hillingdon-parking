package com.hillingdon.parking.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bays")
@Data
@NoArgsConstructor
public class Bay {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Column(nullable = false)
    private String bayNumber;

    @Column(nullable = false)
    private BayType type;

    @Column(nullable = false)
    private BayStatus status = BayStatus.AVAILABLE;

    @Column(nullable = false)
    private boolean isAccessible;

    @Column(nullable = false)
    private boolean isEv;

    @Column(nullable = false)
    private boolean isPremium;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    public enum BayType {
        STANDARD, ACCESSIBLE, EV, PREMIUM, SPECIFIC_NEEDS
    }

    public enum BayStatus {
        AVAILABLE, RESERVED, OCCUPIED, OUT_OF_SERVICE
    }
}
