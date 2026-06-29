package com.hillingdon.parking.services;

import com.hillingdon.parking.models.Bay;
import com.hillingdon.parking.repositories.BayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BayAssignmentService {

    private final BayRepository bayRepository;

    public Optional<Bay> assignBay(boolean needsAccessible, Bay.BayType preferredType) {
        if (needsAccessible) {
            return bayRepository.findFirstByStatusAndIsAccessibleOrderByIdAsc(Bay.BayStatus.AVAILABLE, true);
        }
        return bayRepository.findFirstByStatusAndTypeOrderByIdAsc(Bay.BayStatus.AVAILABLE, preferredType);
    }

    // Full bay assignment logic in Task 4
}
