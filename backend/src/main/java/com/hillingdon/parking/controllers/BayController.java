package com.hillingdon.parking.controllers;

import com.hillingdon.parking.models.Bay;
import com.hillingdon.parking.repositories.BayRepository;
import com.hillingdon.parking.repositories.FloorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bays")
@RequiredArgsConstructor
public class BayController {

    private final BayRepository bayRepository;
    private final FloorRepository floorRepository;

    @GetMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<List<Bay>> getAllBays() {
        return ResponseEntity.ok(bayRepository.findAll());
    }

    @GetMapping("/floors")
    @PreAuthorize("hasAnyRole('PATIENT', 'STAFF')")
    public ResponseEntity<?> getFloorBreakdown() {
        // Task 6 — dashboard floor breakdown
        return ResponseEntity.ok().build();
    }
}
