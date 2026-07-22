package com.hillingdon.parking.controllers;

import com.hillingdon.parking.dto.RoleUpdateRequest;
import com.hillingdon.parking.dto.UserResponse;
import com.hillingdon.parking.models.User;
import com.hillingdon.parking.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;

    @GetMapping
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleUpdateRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getEmail().equalsIgnoreCase(principal.getUsername())) {
            throw new IllegalArgumentException("You cannot change your own role");
        }

        user.setRole(request.getRole());
        userRepository.save(user);

        return ResponseEntity.ok(UserResponse.from(user));
    }
}
