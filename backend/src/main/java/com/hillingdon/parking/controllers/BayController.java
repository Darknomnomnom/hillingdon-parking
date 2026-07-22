package com.hillingdon.parking.controllers;

import com.hillingdon.parking.dto.FloorBreakdownItem;
import com.hillingdon.parking.models.Bay;
import com.hillingdon.parking.repositories.BayRepository;
import com.hillingdon.parking.services.DashboardService;
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
    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<Bay>> getAllBays() {
        return ResponseEntity.ok(bayRepository.findAll());
    }

    @GetMapping("/floors")
    @PreAuthorize("hasAnyRole('PATIENT', 'STAFF', 'ADMIN')")
    public ResponseEntity<List<FloorBreakdownItem>> getFloorBreakdown() {
        return ResponseEntity.ok(dashboardService.getFloorBreakdown());
    }
}
