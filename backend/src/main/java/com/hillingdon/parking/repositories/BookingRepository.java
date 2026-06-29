package com.hillingdon.parking.repositories;

import com.hillingdon.parking.models.Booking;
import com.hillingdon.parking.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByPatientOrderByAppointmentTimeDesc(User patient);

    List<Booking> findByStatus(Booking.BookingStatus status);

    Optional<Booking> findByPlateAndStatus(String plate, Booking.BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.appointmentTime < :cutoff")
    List<Booking> findExpiredReservations(@Param("status") Booking.BookingStatus status, @Param("cutoff") Instant cutoff);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.appointmentTime BETWEEN :from AND :to")
    List<Booking> findBookingsForReminder(@Param("status") Booking.BookingStatus status, @Param("from") Instant from, @Param("to") Instant to);

    List<Booking> findAllByPlate(String plate);
}
