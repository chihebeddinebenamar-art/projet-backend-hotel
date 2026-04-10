package com.Pfa.projectPfa_hotel.repository;

import com.Pfa.projectPfa_hotel.model.HotelRoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotelRoomTypeRepository extends JpaRepository<HotelRoomType, Long> {

    boolean existsByNameIgnoreCase(String name);

    Optional<HotelRoomType> findByNameIgnoreCase(String name);
}
