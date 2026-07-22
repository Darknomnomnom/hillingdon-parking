package com.hillingdon.parking.services;

import com.hillingdon.parking.models.AnprEvent;
import com.hillingdon.parking.models.Bay;
import com.hillingdon.parking.models.Booking;
import com.hillingdon.parking.repositories.AnprEventRepository;
import com.hillingdon.parking.repositories.BayRepository;
import com.hillingdon.parking.repositories.BookingRepository;
import com.hillingdon.parking.repositories.StaffPlateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnprService {

    private final AnprEventRepository anprEventRepository;
    private final BookingRepository bookingRepository;
    private final StaffPlateRepository staffPlateRepository;
    private final BayRepository bayRepository;

    @Transactional
    public AnprEvent processPlateRead(String plate, AnprEvent.Direction direction) {
        AnprEvent event = new AnprEvent();
        event.setPlate(plate);
        event.setDirection(direction);
        event.setSimulated(true);

        if (direction == AnprEvent.Direction.ENTRY) {
            processEntry(plate, event);
        } else {
            processExit(plate, event);
        }

        return anprEventRepository.save(event);
    }

    private void processEntry(String plate, AnprEvent event) {
        Optional<Booking> booking = bookingRepository.findByPlateAndStatus(plate, Booking.BookingStatus.CONFIRMED);
        if (booking.isPresent()) {
            Booking b = booking.get();
            event.setMatchedBooking(b);
            event.setBay(b.getBay());
            b.setStatus(Booking.BookingStatus.ARRIVED);
            if (b.getBay() != null) {
                b.getBay().setStatus(Bay.BayStatus.OCCUPIED);
            }
            bookingRepository.save(b);
            return;
        }

        // No booking match — either a pre-registered doctor/staff plate or a drive-in patient.
        // Both get the next available standard bay per the documented ANPR flow.
        staffPlateRepository.findByPlate(plate).ifPresent(event::setMatchedStaffPlate);

        bayRepository.findFirstByStatusAndTypeOrderByIdAsc(Bay.BayStatus.AVAILABLE, Bay.BayType.STANDARD)
                .ifPresent(bay -> {
                    bay.setStatus(Bay.BayStatus.OCCUPIED);
                    bayRepository.save(bay);
                    event.setBay(bay);
                });
    }

    private void processExit(String plate, AnprEvent event) {
        Optional<Booking> booking = bookingRepository.findByPlateAndStatus(plate, Booking.BookingStatus.ARRIVED);
        if (booking.isPresent()) {
            Booking b = booking.get();
            event.setMatchedBooking(b);
            event.setBay(b.getBay());
            b.setStatus(Booking.BookingStatus.COMPLETED);
            if (b.getBay() != null) {
                b.getBay().setStatus(Bay.BayStatus.AVAILABLE);
            }
            bookingRepository.save(b);
            return;
        }

        // No booking — find the matching drive-in/staff entry event still occupying a bay and release it.
        anprEventRepository.findTopByPlateAndDirectionOrderByTimestampDesc(plate, AnprEvent.Direction.ENTRY)
                .ifPresent(entryEvent -> {
                    event.setMatchedStaffPlate(entryEvent.getMatchedStaffPlate());
                    Bay bay = entryEvent.getBay();
                    if (bay != null) {
                        bay.setStatus(Bay.BayStatus.AVAILABLE);
                        bayRepository.save(bay);
                        event.setBay(bay);
                    }
                });
    }
}
