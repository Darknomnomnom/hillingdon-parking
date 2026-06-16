package com.hillingdon.parking.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bays")
@Data
@NoArgsConstructor
public class Bay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BayType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BayStatus status = BayStatus.AVAILABLE;

    @Column(nullable = false)
    private boolean accessible;

    @Column(nullable = false)
    private boolean ev;

    @Column(nullable = false)
    private boolean premium;

    public enum BayType {
        STANDARD, ACCESSIBLE, EV, PREMIUM, SPECIFIC_NEEDS
    }

    public enum BayStatus {
        AVAILABLE, RESERVED, OCCUPIED
    }
}
