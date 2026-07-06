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
        String bayRef = booking.getBay() != null
                ? "Floor " + booking.getBay().getFloor().getNumber() + " — Bay " + booking.getBay().getBayNumber()
                : "To be confirmed (car park is currently full — staff will contact you)";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Your Hillingdon Hospital Parking Booking");
        msg.setText("""
                Your parking has been reserved.
                Confirmation code: %s
                Bay: %s
                Arrival time: %s
                Vehicle: %s

                Please arrive within 30 minutes of your scheduled time.
                Hillingdon Hospital Parking Team
                """.formatted(booking.getConfirmationCode(), bayRef, booking.getAppointmentTime(), booking.getPlate()));

        try {
            mailSender.send(msg);
            log.info("Sent booking confirmation to {}", to);
        } catch (Exception e) {
            log.warn("Could not send booking confirmation email to {}: {}", to, e.getMessage());
        }
    }

    public void sendArrivalReminder(Booking booking) {
        String to = booking.getPatient().getEmail();
        String bayRef = booking.getBay() != null
                ? "Floor " + booking.getBay().getFloor().getNumber() + " — Bay " + booking.getBay().getBayNumber()
                : "To be confirmed";

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Reminder: Parking at Hillingdon Hospital Tomorrow");
        msg.setText("""
                This is a reminder for your parking booking tomorrow.
                Bay: %s
                Arrival time: %s
                Vehicle: %s

                Hillingdon Hospital Parking Team
                """.formatted(bayRef, booking.getAppointmentTime(), booking.getPlate()));

        try {
            mailSender.send(msg);
            log.info("Sent arrival reminder to {}", to);
        } catch (Exception e) {
            log.warn("Could not send arrival reminder email to {}: {}", to, e.getMessage());
        }
    }
}
