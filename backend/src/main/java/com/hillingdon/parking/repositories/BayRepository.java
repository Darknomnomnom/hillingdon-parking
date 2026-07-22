package com.hillingdon.parking.repositories;

import com.hillingdon.parking.models.Bay;
import com.hillingdon.parking.models.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BayRepository extends JpaRepository<Bay, UUID> {

    List<Bay> findByFloor(Floor floor);

    List<Bay> findByStatus(Bay.BayStatus status);

    @Query("SELECT b FROM Bay b WHERE b.status = :status AND b.type = :type ORDER BY b.id ASC")
    List<Bay> findAvailableByType(@Param("status") Bay.BayStatus status, @Param("type") Bay.BayType type);

    @Query("SELECT COUNT(b) FROM Bay b WHERE b.status = :status")
    long countByStatus(@Param("status") Bay.BayStatus status);

    @Query("SELECT COUNT(b) FROM Bay b WHERE b.floor = :floor AND b.status = :status")
    long countByFloorAndStatus(@Param("floor") Floor floor, @Param("status") Bay.BayStatus status);

    @Query("SELECT COUNT(b) FROM Bay b WHERE b.type = :type")
    long countByType(@Param("type") Bay.BayType type);

    @Query("SELECT COUNT(b) FROM Bay b WHERE b.type = :type AND b.status = :status")
    long countByTypeAndStatus(@Param("type") Bay.BayType type, @Param("status") Bay.BayStatus status);

    @Query("SELECT COUNT(b) FROM Bay b WHERE b.floor = :floor AND b.type = :type")
    long countByFloorAndType(@Param("floor") Floor floor, @Param("type") Bay.BayType type);

    @Query("SELECT COUNT(b) FROM Bay b WHERE b.floor = :floor AND b.type = :type AND b.status = :status")
    long countByFloorAndTypeAndStatus(@Param("floor") Floor floor, @Param("type") Bay.BayType type, @Param("status") Bay.BayStatus status);

    Optional<Bay> findFirstByStatusAndIsAccessibleOrderByIdAsc(Bay.BayStatus status, boolean isAccessible);

    Optional<Bay> findFirstByStatusAndTypeOrderByIdAsc(Bay.BayStatus status, Bay.BayType type);
}
