package com.hillingdon.parking.services;

import com.hillingdon.parking.models.Booking;
import com.hillingdon.parking.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoShowService {

    private static final int NO_SHOW_WINDOW_MINUTES = 30;

    private final BookingRepository bookingRepository;

    @Transactional
    public int releaseExpiredReservations() {
        Instant cutoff = Instant.now().minus(NO_SHOW_WINDOW_MINUTES, ChronoUnit.MINUTES);
        List<Booking> expired = bookingRepository.findExpiredReservations(cutoff);

        for (Booking booking : expired) {
            booking.setStatus(Booking.BookingStatus.NO_SHOW);
            booking.getBay().setStatus(com.hillingdon.parking.models.Bay.BayStatus.AVAILABLE);
            bookingRepository.save(booking);
            log.info("Released bay {} for no-show booking {}", booking.getBay().getId(), booking.getId());
        }

        return expired.size();
    }
}
