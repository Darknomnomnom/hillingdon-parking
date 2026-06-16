package com.hillingdon.parking.controllers;

import com.hillingdon.parking.services.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping("/pending")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getPendingBadges() {
        return ResponseEntity.ok(badgeService.getPendingBadges());
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> submitBadge(
            @RequestParam String badgeNumber,
            @RequestParam MultipartFile photo) {
        // Task 7 — badge upload + S3 storage
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> approveBadge(@PathVariable Long id) {
        // Task 8
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> rejectBadge(@PathVariable Long id) {
        // Task 8
        return ResponseEntity.ok().build();
    }
}
