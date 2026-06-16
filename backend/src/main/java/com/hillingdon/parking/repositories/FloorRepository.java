package com.hillingdon.parking.repositories;

import com.hillingdon.parking.models.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FloorRepository extends JpaRepository<Floor, Long> {
    List<Floor> findAllByOrderByNumberAsc();
}
