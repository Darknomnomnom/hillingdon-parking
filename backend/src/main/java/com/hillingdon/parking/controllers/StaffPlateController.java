package com.hillingdon.parking.controllers;

import com.hillingdon.parking.dto.StaffPlateRequest;
import com.hillingdon.parking.dto.StaffPlateResponse;
import com.hillingdon.parking.models.StaffPlate;
import com.hillingdon.parking.repositories.StaffPlateRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * HR pre-registers doctor/staff plates here so ANPR can auto-categorise them on
 * arrival without a booking (see CLAUDE.md ANPR flow / AnprService#processEntry).
 */
@RestController
@RequestMapping("/api/admin/staff-plates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StaffPlateController {

    private final StaffPlateRepository staffPlateRepository;

    @GetMapping
    public List<StaffPlateResponse> list() {
        return staffPlateRepository.findAll().stream()
                .map(StaffPlateResponse::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<StaffPlateResponse> create(@Valid @RequestBody StaffPlateRequest request) {
        String normalisedPlate = request.getPlate().toUpperCase().replaceAll("\\s+", "");

        staffPlateRepository.findByPlate(normalisedPlate).ifPresent(p -> {
            throw new IllegalArgumentException("Plate " + normalisedPlate + " is already registered");
        });

        StaffPlate staffPlate = new StaffPlate();
        staffPlate.setPlate(normalisedPlate);
        staffPlate.setHolderName(request.getHolderName());
        staffPlate.setCategory(request.getCategory());

        return ResponseEntity.ok(StaffPlateResponse.from(staffPlateRepository.save(staffPlate)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!staffPlateRepository.existsById(id)) {
            throw new IllegalArgumentException("Staff plate not found");
        }
        staffPlateRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
