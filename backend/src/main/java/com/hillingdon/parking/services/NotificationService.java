package com.hillingdon.parking.services;

import com.hillingdon.parking.models.Booking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    public void sendBookingConfirmation(Booking booking) {
        String to = booking.getPatient().getEmail();
        String bayRef = "Floor " + booking.getBay().getFloor().getNumber() + " — Bay " + booking.getBay().getId();

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Your Hillingdon Hospital Parking Booking");
        msg.setText("""
                Your parking has been reserved.
                Bay: %s
                Arrival time: %s
                Vehicle: %s

                Please arrive within 30 minutes of your scheduled time.
                Hillingdon Hospital Parking Team
                """.formatted(bayRef, booking.getScheduledArrival(), booking.getPlate()));

        mailSender.send(msg);
        log.info("Sent booking confirmation to {}", to);
    }

    public void sendArrivalReminder(Booking booking) {
        String to = booking.getPatient().getEmail();
        String bayRef = "Floor " + booking.getBay().getFloor().getNumber() + " — Bay " + booking.getBay().getId();

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Reminder: Parking at Hillingdon Hospital Tomorrow");
        msg.setText("""
                This is a reminder for your parking booking tomorrow.
                Bay: %s
                Arrival time: %s
                Vehicle: %s

                Hillingdon Hospital Parking Team
                """.formatted(bayRef, booking.getScheduledArrival(), booking.getPlate()));

        mailSender.send(msg);
        log.info("Sent arrival reminder to {}", to);
    }
}
