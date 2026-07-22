package com.hillingdon.parking.controllers;

import com.hillingdon.parking.dto.HourlyTrendPoint;
import com.hillingdon.parking.dto.KpiSummaryResponse;
import com.hillingdon.parking.dto.NoShowStatsResponse;
import com.hillingdon.parking.dto.UserDistributionResponse;
import com.hillingdon.parking.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpis")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<KpiSummaryResponse> getKpis() {
        return ResponseEntity.ok(dashboardService.getKpiSummary());
    }

    @GetMapping("/trends")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<HourlyTrendPoint>> getHourlyTrends() {
        return ResponseEntity.ok(dashboardService.getHourlyTrends());
    }

    @GetMapping("/no-shows")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<NoShowStatsResponse> getNoShowStats() {
        return ResponseEntity.ok(dashboardService.getNoShowStats());
    }

    @GetMapping("/user-distribution")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<UserDistributionResponse> getUserDistribution() {
        return ResponseEntity.ok(dashboardService.getUserDistribution());
    }
}
