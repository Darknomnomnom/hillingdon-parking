package com.hillingdon.parking.dto;

import com.hillingdon.parking.models.User;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private User.Role role;
    private Instant createdAt;

    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.id = user.getId();
        r.email = user.getEmail();
        r.firstName = user.getFirstName();
        r.lastName = user.getLastName();
        r.phone = user.getPhone();
        r.role = user.getRole();
        r.createdAt = user.getCreatedAt();
        return r;
    }
}
