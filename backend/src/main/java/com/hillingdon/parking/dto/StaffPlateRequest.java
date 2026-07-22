package com.hillingdon.parking.dto;

import com.hillingdon.parking.models.StaffPlate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StaffPlateRequest {

    @NotBlank
    private String plate;

    @NotBlank
    private String holderName;

    @NotNull
    private StaffPlate.Category category;
}
