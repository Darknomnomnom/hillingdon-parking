package com.hillingdon.parking.repositories;

import com.hillingdon.parking.models.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FloorRepository extends JpaRepository<Floor, UUID> {
    List<Floor> findAllByOrderByNumberAsc();
}
