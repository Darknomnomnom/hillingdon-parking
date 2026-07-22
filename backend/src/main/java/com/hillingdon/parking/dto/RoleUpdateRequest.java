package com.hillingdon.parking.dto;

import com.hillingdon.parking.models.User;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoleUpdateRequest {

    @NotNull
    private User.Role role;
}
