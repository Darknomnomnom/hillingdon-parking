package com.hillingdon.parking.repositories;

import com.hillingdon.parking.models.AnprEvent;
import com.hillingdon.parking.models.Bay;
import com.hillingdon.parking.models.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AnprEventRepository extends JpaRepository<AnprEvent, UUID> {
    Optional<AnprEvent> findByPlateAndDirection(String plate, AnprEvent.Direction direction);
    List<AnprEvent> findByMatchedBooking(Booking booking);
    boolean existsByMatchedBookingAndDirection(Booking booking, AnprEvent.Direction direction);
    Optional<AnprEvent> findTopByPlateAndDirectionOrderByTimestampDesc(String plate, AnprEvent.Direction direction);
    Optional<AnprEvent> findTopByBayAndDirectionOrderByTimestampDesc(Bay bay, AnprEvent.Direction direction);
}
