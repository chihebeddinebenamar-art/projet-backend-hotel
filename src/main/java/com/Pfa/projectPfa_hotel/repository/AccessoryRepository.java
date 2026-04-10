package com.Pfa.projectPfa_hotel.repository;

import com.Pfa.projectPfa_hotel.model.Accessory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessoryRepository extends JpaRepository<Accessory, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Accessory> findByNameIgnoreCase(String name);
}
