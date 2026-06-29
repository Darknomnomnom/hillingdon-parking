package com.hillingdon.parking.controllers;

import com.hillingdon.parking.dto.UserResponse;
import com.hillingdon.parking.models.User;
import com.hillingdon.parking.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('PATIENT', 'STAFF')")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
        return ResponseEntity.ok(UserResponse.from(user));
    }
}
