package com.hillingdon.parking.repositories;

import com.hillingdon.parking.models.Bay;
import com.hillingdon.parking.models.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BayRepository extends JpaRepository<Bay, Long> {

    List<Bay> findByFloor(Floor floor);

    List<Bay> findByStatus(Bay.BayStatus status);

    @Query("SELECT b FROM Bay b WHERE b.status = 'AVAILABLE' AND b.type = :type ORDER BY b.id ASC")
    List<Bay> findAvailableByType(Bay.BayType type);

    @Query("SELECT COUNT(b) FROM Bay b WHERE b.status = :status")
    long countByStatus(Bay.BayStatus status);

    @Query("SELECT COUNT(b) FROM Bay b WHERE b.floor = :floor AND b.status = :status")
    long countByFloorAndStatus(Floor floor, Bay.BayStatus status);

    Optional<Bay> findFirstByStatusAndAccessibleOrderByIdAsc(Bay.BayStatus status, boolean accessible);

    Optional<Bay> findFirstByStatusAndTypeOrderByIdAsc(Bay.BayStatus status, Bay.BayType type);
}
