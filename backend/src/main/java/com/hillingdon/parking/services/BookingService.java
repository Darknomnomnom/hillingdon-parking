package com.hillingdon.parking.services;

import com.hillingdon.parking.dto.BookingResponse;
import com.hillingdon.parking.dto.CreateBookingRequest;
import com.hillingdon.parking.models.Bay;
import com.hillingdon.parking.models.Booking;
import com.hillingdon.parking.models.User;
import com.hillingdon.parking.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BayAssignmentService bayAssignmentService;
    private final NotificationService notificationService;

    private static final Duration ARRIVAL_WINDOW = Duration.ofMinutes(30);

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest req, User patient) {
        Booking booking = new Booking();
        booking.setPatient(patient);
        booking.setPlate(req.getPlate().toUpperCase().replaceAll("\\s+", ""));
        booking.setVisitType(req.getVisitType());
        booking.setAppointmentTime(req.getAppointmentTime());
        booking.setArrivalWindowStart(req.getAppointmentTime().minus(ARRIVAL_WINDOW));
        booking.setArrivalWindowEnd(req.getAppointmentTime().plus(ARRIVAL_WINDOW));
        booking.setNotes(req.getNotes());
        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        Optional<Bay> bay = bayAssignmentService.assignBay(req.isNeedsAccessible());
        bay.ifPresent(booking::setBay);

        bookingRepository.save(booking);

        notificationService.sendBookingConfirmation(booking);

        return BookingResponse.from(booking);
    }

    public List<BookingResponse> getMyBookings(User patient) {
        return bookingRepository.findByPatientOrderByAppointmentTimeDesc(patient)
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "appointmentTime"))
                .stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse cancelBooking(UUID id, User requestingUser) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        boolean isOwner = booking.getPatient().getId().equals(requestingUser.getId());
        boolean isStaff = requestingUser.getRole() == User.Role.STAFF || requestingUser.getRole() == User.Role.ADMIN;

        if (!isOwner && !isStaff) {
            throw new SecurityException("Not authorised to cancel this booking");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }

        if (booking.getStatus() == Booking.BookingStatus.ARRIVED ||
            booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a booking that has already arrived or completed");
        }

        if (booking.getBay() != null) {
            bayAssignmentService.releaseBay(booking.getBay());
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse markNoShow(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only a confirmed booking that hasn't arrived can be marked as a no-show");
        }

        if (booking.getBay() != null) {
            bayAssignmentService.releaseBay(booking.getBay());
        }

        booking.setStatus(Booking.BookingStatus.NO_SHOW);
        booking.setUpdatedAt(Instant.now());
        bookingRepository.save(booking);

        return BookingResponse.from(booking);
    }
}
