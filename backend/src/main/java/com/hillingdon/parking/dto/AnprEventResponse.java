package com.hillingdon.parking.dto;

import com.hillingdon.parking.models.AnprEvent;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AnprEventResponse {
    private UUID id;
    private String plate;
    private AnprEvent.Direction direction;
    private UUID matchedBookingId;
    private UUID matchedStaffPlateId;
    private UUID bayId;
    private String bayNumber;
    private Instant timestamp;
    private boolean isSimulated;

    public static AnprEventResponse from(AnprEvent event) {
        AnprEventResponse r = new AnprEventResponse();
        r.id = event.getId();
        r.plate = event.getPlate();
        r.direction = event.getDirection();
        r.matchedBookingId = event.getMatchedBooking() != null ? event.getMatchedBooking().getId() : null;
        r.matchedStaffPlateId = event.getMatchedStaffPlate() != null ? event.getMatchedStaffPlate().getId() : null;
        r.bayId = event.getBay() != null ? event.getBay().getId() : null;
        r.bayNumber = event.getBay() != null ? event.getBay().getBayNumber() : null;
        r.timestamp = event.getTimestamp();
        r.isSimulated = event.isSimulated();
        return r;
    }
}
