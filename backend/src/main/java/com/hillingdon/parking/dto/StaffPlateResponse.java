package com.hillingdon.parking.dto;

import com.hillingdon.parking.models.StaffPlate;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class StaffPlateResponse {
    private UUID id;
    private String plate;
    private String holderName;
    private StaffPlate.Category category;
    private Instant createdAt;

    public static StaffPlateResponse from(StaffPlate staffPlate) {
        StaffPlateResponse r = new StaffPlateResponse();
        r.id = staffPlate.getId();
        r.plate = staffPlate.getPlate();
        r.holderName = staffPlate.getHolderName();
        r.category = staffPlate.getCategory();
        r.createdAt = staffPlate.getCreatedAt();
        return r;
    }
}
