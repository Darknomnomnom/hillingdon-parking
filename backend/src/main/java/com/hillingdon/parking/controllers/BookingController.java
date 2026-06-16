package com.hillingdon.parking.controllers;

import com.hillingdon.parking.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getAllBookings() {
        // Task 4
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('PATIENT', 'STAFF')")
    public ResponseEntity<?> getMyBookings() {
        // Task 4
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<?> createBooking(@RequestBody Object request) {
        // Task 4
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT', 'STAFF')")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id) {
        // Task 4
        return ResponseEntity.ok().build();
    }
}
