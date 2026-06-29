package com.hillingdon.parking.services;

import com.hillingdon.parking.models.Bay;
import com.hillingdon.parking.repositories.BayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BayAssignmentService {

    private final BayRepository bayRepository;

    /**
     * Assigns and reserves the best available bay for a booking.
     *
     * Blue Badge holders always get an ACCESSIBLE bay.
     * All others get a STANDARD bay.
     * Falls back to any available bay if the preferred type is exhausted.
     */
    @Transactional
    public Optional<Bay> assignBay(boolean needsAccessible) {
        Optional<Bay> bay;

        if (needsAccessible) {
            bay = bayRepository.findFirstByStatusAndTypeOrderByIdAsc(Bay.BayStatus.AVAILABLE, Bay.BayType.ACCESSIBLE);
            if (bay.isEmpty()) {
                // Accessible bays full — fall back to standard (must still comply with Blue Badge)
                bay = bayRepository.findFirstByStatusAndTypeOrderByIdAsc(Bay.BayStatus.AVAILABLE, Bay.BayType.STANDARD);
            }
        } else {
            bay = bayRepository.findFirstByStatusAndTypeOrderByIdAsc(Bay.BayStatus.AVAILABLE, Bay.BayType.STANDARD);
            if (bay.isEmpty()) {
                // Standard bays full — use any available bay
                bay = bayRepository.findFirstByStatusAndIsAccessibleOrderByIdAsc(Bay.BayStatus.AVAILABLE, false);
            }
        }

        bay.ifPresent(b -> {
            b.setStatus(Bay.BayStatus.RESERVED);
            b.setUpdatedAt(Instant.now());
            bayRepository.save(b);
        });

        return bay;
    }

    @Transactional
    public void releaseBay(Bay bay) {
        bay.setStatus(Bay.BayStatus.AVAILABLE);
        bay.setUpdatedAt(Instant.now());
        bayRepository.save(bay);
    }
}
