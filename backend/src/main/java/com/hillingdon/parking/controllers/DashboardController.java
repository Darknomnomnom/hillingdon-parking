package com.hillingdon.parking.controllers;

import com.hillingdon.parking.repositories.BayRepository;
import com.hillingdon.parking.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final BayRepository bayRepository;
    private final BookingRepository bookingRepository;

    @GetMapping("/kpis")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getKpis() {
        // Task 6 — KPI card data
        return ResponseEntity.ok().build();
    }

    @GetMapping("/trends")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getHourlyTrends() {
        // Task 6 — hourly occupancy + revenue chart
        return ResponseEntity.ok().build();
    }

    @GetMapping("/no-shows")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getNoShowStats() {
        // Task 6 — no-show analytics
        return ResponseEntity.ok().build();
    }
}
