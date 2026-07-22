package com.hillingdon.parking.jobs;

import com.hillingdon.parking.models.Booking;
import com.hillingdon.parking.repositories.BookingRepository;
import com.hillingdon.parking.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArrivalReminderJobTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private NotificationService notificationService;

    private ArrivalReminderJob job;

    private Booking bookingWithId() {
        Booking booking = new Booking();
        return booking;
    }

    @Test
    void sendReminders_queriesConfirmedBookingsInApprox24hWindow() {
        job = new ArrivalReminderJob(bookingRepository, notificationService);
        when(bookingRepository.findBookingsForReminder(any(), any(), any())).thenReturn(List.of());

        Instant before = Instant.now();
        job.sendReminders();
        Instant after = Instant.now();

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(bookingRepository).findBookingsForReminder(eq(Booking.BookingStatus.CONFIRMED), fromCaptor.capture(), toCaptor.capture());

        Instant from = fromCaptor.getValue();
        Instant to = toCaptor.getValue();

        assertThat(from).isBetween(before.plus(23, ChronoUnit.HOURS), after.plus(23, ChronoUnit.HOURS));
        assertThat(to).isBetween(before.plus(25, ChronoUnit.HOURS), after.plus(25, ChronoUnit.HOURS));
        assertThat(ChronoUnit.HOURS.between(from, to)).isEqualTo(2);
    }

    @Test
    void sendReminders_sendsOneReminderPerMatchedBooking() {
        job = new ArrivalReminderJob(bookingRepository, notificationService);
        Booking b1 = bookingWithId();
        Booking b2 = bookingWithId();
        when(bookingRepository.findBookingsForReminder(any(), any(), any())).thenReturn(List.of(b1, b2));

        job.sendReminders();

        verify(notificationService).sendArrivalReminder(b1);
        verify(notificationService).sendArrivalReminder(b2);
        verifyNoMoreInteractions(notificationService);
    }

    @Test
    void sendReminders_continuesRemainingBookingsWhenOneNotificationFails() {
        job = new ArrivalReminderJob(bookingRepository, notificationService);
        Booking failing = bookingWithId();
        Booking succeeding = bookingWithId();
        when(bookingRepository.findBookingsForReminder(any(), any(), any())).thenReturn(List.of(failing, succeeding));
        doThrow(new RuntimeException("boom")).when(notificationService).sendArrivalReminder(failing);

        job.sendReminders();

        verify(notificationService).sendArrivalReminder(failing);
        verify(notificationService).sendArrivalReminder(succeeding);
    }

    @Test
    void sendReminders_withNoMatchingBookings_sendsNothing() {
        job = new ArrivalReminderJob(bookingRepository, notificationService);
        when(bookingRepository.findBookingsForReminder(any(), any(), any())).thenReturn(List.of());

        job.sendReminders();

        verifyNoInteractions(notificationService);
    }
}
