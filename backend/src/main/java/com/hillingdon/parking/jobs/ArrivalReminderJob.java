package com.hillingdon.parking.jobs;

import com.hillingdon.parking.models.Booking;
import com.hillingdon.parking.repositories.BookingRepository;
import com.hillingdon.parking.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArrivalReminderJob {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    // Runs daily at 08:00 UTC
    @Scheduled(cron = "0 0 8 * * *")
    public void sendReminders() {
        Instant from = Instant.now().plus(23, ChronoUnit.HOURS);
        Instant to   = Instant.now().plus(25, ChronoUnit.HOURS);

        List<Booking> upcoming = bookingRepository.findBookingsForReminder(from, to);
        for (Booking booking : upcoming) {
            try {
                notificationService.sendArrivalReminder(booking);
            } catch (Exception e) {
                log.error("Failed to send reminder for booking {}: {}", booking.getId(), e.getMessage());
            }
        }
        log.info("Arrival reminders sent for {} bookings", upcoming.size());
    }
}
