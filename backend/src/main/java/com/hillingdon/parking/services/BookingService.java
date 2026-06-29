package com.hillingdon.parking.services;

import com.hillingdon.parking.models.Booking;
import com.hillingdon.parking.models.User;
import com.hillingdon.parking.repositories.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BayAssignmentService bayAssignmentService;
    private final NotificationService notificationService;

    public List<Booking> getBookingsForPatient(User patient) {
        return bookingRepository.findByPatientOrderByAppointmentTimeDesc(patient);
    }

    // Full implementation in Task 4
}
