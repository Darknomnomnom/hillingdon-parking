package com.hillingdon.parking.controllers;

import com.hillingdon.parking.dto.BadgeResponse;
import com.hillingdon.parking.models.User;
import com.hillingdon.parking.repositories.UserRepository;
import com.hillingdon.parking.services.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;
    private final UserRepository userRepository;

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<List<BadgeResponse>> getPendingBadges() {
        List<BadgeResponse> badges = badgeService.getPendingBadges().stream()
                .map(badgeService::toResponse)
                .toList();
        return ResponseEntity.ok(badges);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<BadgeResponse>> getMyBadges(@AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        List<BadgeResponse> badges = badgeService.getBadgesForUser(user).stream()
                .map(badgeService::toResponse)
                .toList();
        return ResponseEntity.ok(badges);
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<BadgeResponse> submitBadge(
            @RequestParam String plate,
            @RequestParam String badgeNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiresAt,
            @RequestParam MultipartFile photo,
            @AuthenticationPrincipal UserDetails principal) {
        User user = resolveUser(principal);
        BadgeResponse response = badgeService.toResponse(
                badgeService.submitBadge(user, plate, badgeNumber, expiresAt, photo));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> approveBadge(@PathVariable Long id) {
        // Task 8
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> rejectBadge(@PathVariable Long id) {
        // Task 8
        return ResponseEntity.ok().build();
    }

    private User resolveUser(UserDetails principal) {
        return userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));
    }
}
