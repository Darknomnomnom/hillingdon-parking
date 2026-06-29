package com.hillingdon.parking.repositories;

import com.hillingdon.parking.models.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
    Optional<Vehicle> findByPlate(String plate);
    boolean existsByPlate(String plate);
}
