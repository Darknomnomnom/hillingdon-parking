package com.hillingdon.parking.controllers;

import com.hillingdon.parking.models.AnprEvent;
import com.hillingdon.parking.services.AnprService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anpr")
@RequiredArgsConstructor
public class ANPRController {

    private final AnprService anprService;

    @PostMapping("/event")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<AnprEvent> recordEvent(
            @RequestParam String plate,
            @RequestParam AnprEvent.Direction direction) {
        return ResponseEntity.ok(anprService.processPlateRead(plate, direction));
    }
}
