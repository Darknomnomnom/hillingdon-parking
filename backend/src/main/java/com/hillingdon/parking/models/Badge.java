package com.hillingdon.parking.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "badges")
@Data
@NoArgsConstructor
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String badgeNumber;

    private LocalDate expiryDate;

    // S3 key for uploaded photo
    @Column(nullable = false)
    private String photoKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BadgeStatus status = BadgeStatus.PENDING;

    private Instant submittedAt = Instant.now();
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    public enum BadgeStatus {
        PENDING, APPROVED, REJECTED
    }
}
