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

    public static BadgeResponse from(Badge badge) {
        BadgeResponse res = new BadgeResponse();
        res.setId(badge.getId());
        res.setBadgeNumber(badge.getBadgeNumber());
        res.setPhotoUrl(badge.getPhotoUrl());
        res.setStatus(badge.getStatus());
        res.setExpiresAt(badge.getExpiresAt());
        res.setRejectionReason(badge.getRejectionReason());
        res.setCreatedAt(badge.getCreatedAt());
        return res;
    }
}
