package com.hillingdon.parking.services;

import com.hillingdon.parking.models.AnprEvent;
import com.hillingdon.parking.models.Bay;
import com.hillingdon.parking.models.Booking;
import com.hillingdon.parking.repositories.AnprEventRepository;
import com.hillingdon.parking.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnprService {

    private final AnprEventRepository anprEventRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public AnprEvent processPlateRead(String plate, AnprEvent.Direction direction) {
        AnprEvent event = new AnprEvent();
        event.setPlate(plate);
        event.setDirection(direction);
        event.setSimulated(true);

        if (direction == AnprEvent.Direction.ENTRY) {
            Optional<Booking> booking = bookingRepository.findByPlateAndStatus(plate, Booking.BookingStatus.CONFIRMED);
            booking.ifPresent(b -> {
                event.setMatchedBooking(b);
                event.setBay(b.getBay());
                b.setStatus(Booking.BookingStatus.ARRIVED);
                if (b.getBay() != null) {
                    b.getBay().setStatus(Bay.BayStatus.OCCUPIED);
                }
                bookingRepository.save(b);
            });
        } else {
            Optional<Booking> booking = bookingRepository.findByPlateAndStatus(plate, Booking.BookingStatus.ARRIVED);
            booking.ifPresent(b -> {
                event.setMatchedBooking(b);
                event.setBay(b.getBay());
                b.setStatus(Booking.BookingStatus.COMPLETED);
                if (b.getBay() != null) {
                    b.getBay().setStatus(Bay.BayStatus.AVAILABLE);
                }
                bookingRepository.save(b);
            });
        }

        return anprEventRepository.save(event);
    }
}
