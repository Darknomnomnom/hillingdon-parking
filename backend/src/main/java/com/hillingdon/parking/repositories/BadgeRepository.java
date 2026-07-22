package com.hillingdon.parking.repositories;

import com.hillingdon.parking.models.Badge;
import com.hillingdon.parking.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BadgeRepository extends JpaRepository<Badge, UUID> {
    List<Badge> findByStatus(Badge.BadgeStatus status);
    List<Badge> findByUser(User user);
    List<Badge> findByStatusNotOrderByVerifiedAtDesc(Badge.BadgeStatus status);
}
