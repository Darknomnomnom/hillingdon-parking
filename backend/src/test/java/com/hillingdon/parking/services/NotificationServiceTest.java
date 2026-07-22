package com.hillingdon.parking.services;

import com.hillingdon.parking.models.Bay;
import com.hillingdon.parking.models.Booking;
import com.hillingdon.parking.models.Floor;
import com.hillingdon.parking.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(mailSender);
    }

    private Booking bookingWithBay() {
        User patient = new User();
        patient.setEmail("patient@example.com");

        Floor floor = new Floor();
        floor.setNumber(2);

        Bay bay = new Bay();
        bay.setFloor(floor);
        bay.setBayNumber("A12");

        Booking booking = new Booking();
        booking.setPatient(patient);
        booking.setBay(bay);
        booking.setPlate("AB12CDE");
        booking.setAppointmentTime(Instant.parse("2026-07-23T09:00:00Z"));

        return booking;
    }

    @Test
    void sendArrivalReminder_sendsEmailToPatientWithBayAndPlateDetails() {
        Booking booking = bookingWithBay();

        notificationService.sendArrivalReminder(booking);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("patient@example.com");
        assertThat(sent.getSubject()).isEqualTo("Reminder: Parking at Hillingdon Hospital Tomorrow");
        assertThat(sent.getText()).contains("Floor 2 — Bay A12");
        assertThat(sent.getText()).contains("AB12CDE");
    }

    @Test
    void sendArrivalReminder_withNoBayAssigned_fallsBackToPlaceholderText() {
        Booking booking = bookingWithBay();
        booking.setBay(null);

        notificationService.sendArrivalReminder(booking);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("To be confirmed");
    }

    @Test
    void sendArrivalReminder_whenMailSenderThrows_doesNotPropagate() {
        Booking booking = bookingWithBay();
        doThrow(new RuntimeException("SMTP auth failed")).when(mailSender).send(any(SimpleMailMessage.class));

        notificationService.sendArrivalReminder(booking);
        // No exception thrown out of the service — failures are logged and swallowed here.
    }

    @Test
    void sendBookingConfirmation_withNoBayAssigned_mentionsCarParkFull() {
        Booking booking = bookingWithBay();
        booking.setBay(null);

        notificationService.sendBookingConfirmation(booking);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getText()).contains("car park is currently full");
    }
}
