package com.hillingdon.parking.dto;

import com.hillingdon.parking.models.Badge;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class BadgeResponse {

    private UUID id;
    private String badgeNumber;
    private String photoUrl;
    private Badge.BadgeStatus status;
    private LocalDate expiresAt;
    private String rejectionReason;
    private Instant createdAt;
    private String patientName;
    private String patientEmail;
    private String plate;
    private String verifiedByName;
    private Instant verifiedAt;

    public static BadgeResponse from(Badge badge) {
        BadgeResponse res = new BadgeResponse();
        res.setId(badge.getId());
        res.setBadgeNumber(badge.getBadgeNumber());
        res.setPhotoUrl(badge.getPhotoUrl());
        res.setStatus(badge.getStatus());
        res.setExpiresAt(badge.getExpiresAt());
        res.setRejectionReason(badge.getRejectionReason());
        res.setCreatedAt(badge.getCreatedAt());
        if (badge.getUser() != null) {
            res.setPatientName(badge.getUser().getFirstName() + " " + badge.getUser().getLastName());
            res.setPatientEmail(badge.getUser().getEmail());
        }
        if (badge.getVehicle() != null) {
            res.setPlate(badge.getVehicle().getPlate());
        }
        if (badge.getVerifiedBy() != null) {
            res.setVerifiedByName(badge.getVerifiedBy().getFirstName() + " " + badge.getVerifiedBy().getLastName());
        }
        res.setVerifiedAt(badge.getVerifiedAt());
        return res;
    }
}
