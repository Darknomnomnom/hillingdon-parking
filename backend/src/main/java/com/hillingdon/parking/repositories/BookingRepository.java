package com.hillingdon.parking.repositories;

import com.hillingdon.parking.models.Booking;
import com.hillingdon.parking.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByPatientOrderByScheduledArrivalDesc(User patient);

    List<Booking> findByStatus(Booking.BookingStatus status);

    Optional<Booking> findByPlateAndStatus(String plate, Booking.BookingStatus status);

    // Find bookings that are still RESERVED but past their arrival window — candidates for no-show release
    @Query("SELECT b FROM Booking b WHERE b.status = 'RESERVED' AND b.scheduledArrival < :cutoff")
    List<Booking> findExpiredReservations(Instant cutoff);

    // Find bookings due for 24h reminder
    @Query("SELECT b FROM Booking b WHERE b.status = 'RESERVED' AND b.scheduledArrival BETWEEN :from AND :to")
    List<Booking> findBookingsForReminder(Instant from, Instant to);

    List<Booking> findAllByPlate(String plate);
}
