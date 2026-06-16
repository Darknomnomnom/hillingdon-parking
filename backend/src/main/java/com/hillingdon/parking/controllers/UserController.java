package com.hillingdon.parking.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('PATIENT', 'STAFF')")
    public ResponseEntity<?> getCurrentUser() {
        // Task 3 — return JWT principal
        return ResponseEntity.ok().build();
    }
}
