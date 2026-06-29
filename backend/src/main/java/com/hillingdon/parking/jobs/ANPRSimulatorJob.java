package com.hillingdon.parking.jobs;

import com.hillingdon.parking.models.AnprEvent;
import com.hillingdon.parking.models.Booking;
import com.hillingdon.parking.repositories.BookingRepository;
import com.hillingdon.parking.services.AnprService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class ANPRSimulatorJob {

    private final BookingRepository bookingRepository;
    private final AnprService anprService;
    private final Random random = new Random();

    // Fires every 2 minutes — simulates an ANPR plate-read event
    @Scheduled(fixedDelay = 120_000)
    public void simulateEntry() {
        List<Booking> active = bookingRepository.findByStatus(Booking.BookingStatus.CONFIRMED);
        if (active.isEmpty()) {
            log.debug("ANPR simulator: no active reservations to simulate");
            return;
        }

        Booking target = active.get(random.nextInt(active.size()));
        anprService.processPlateRead(target.getPlate(), AnprEvent.Direction.ENTRY);
        log.info("ANPR simulator: fired entry event for plate {}", target.getPlate());
    }
}
