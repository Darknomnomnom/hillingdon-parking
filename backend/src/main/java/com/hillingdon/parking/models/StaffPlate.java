package com.hillingdon.parking.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "staff_plates")
@Data
@NoArgsConstructor
public class StaffPlate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String plate;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false)
    private Category category = Category.STAFF;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public enum Category {
        DOCTOR, STAFF
    }
}
