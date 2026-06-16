package com.hillingdon.parking.services;

import com.hillingdon.parking.models.Badge;
import com.hillingdon.parking.models.User;
import com.hillingdon.parking.repositories.BadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;

    public List<Badge> getPendingBadges() {
        return badgeRepository.findByStatus(Badge.BadgeStatus.PENDING);
    }

    public List<Badge> getBadgesForUser(User user) {
        return badgeRepository.findByUser(user);
    }

    // Review logic in Task 8
}
