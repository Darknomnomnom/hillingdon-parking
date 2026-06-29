package com.hillingdon.parking.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "floors")
@Data
@NoArgsConstructor
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Integer number;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer totalBays;

    @Column(nullable = false)
    private String type;
}
