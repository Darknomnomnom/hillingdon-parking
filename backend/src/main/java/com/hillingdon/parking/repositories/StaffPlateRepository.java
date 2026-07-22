package com.hillingdon.parking.repositories;

import com.hillingdon.parking.models.StaffPlate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StaffPlateRepository extends JpaRepository<StaffPlate, UUID> {
    Optional<StaffPlate> findByPlate(String plate);
}
