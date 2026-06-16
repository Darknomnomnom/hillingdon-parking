package com.hillingdon.parking.jobs;

import com.hillingdon.parking.services.NoShowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoShowReleaseJob {

    private final NoShowService noShowService;

    // Runs every 5 minutes
    @Scheduled(fixedDelay = 300_000)
    public void run() {
        int released = noShowService.releaseExpiredReservations();
        if (released > 0) {
            log.info("No-show release: freed {} bays", released);
        }
    }
}
