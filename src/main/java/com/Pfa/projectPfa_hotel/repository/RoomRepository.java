package com.Pfa.projectPfa_hotel.repository;

import com.Pfa.projectPfa_hotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    @Query("SELECT COUNT(r) FROM Room r JOIN r.accessories a WHERE a.id = :accessoryId")
    long countRoomsWithAccessory(@Param("accessoryId") Long accessoryId);

    long countByRoomType(String roomType);

    List<Room> findByRoomType(String roomType);

    @Query("SELECT DISTINCT r.roomType FROM Room r")
    List<String> findDistinctRoomTypes();

    @Query("SELECT COALESCE(AVG(r.roomPrice), 0) FROM Room r")
    BigDecimal averageRoomPrice();

    boolean existsByRoomNumberIgnoreCase(String roomNumber);

    boolean existsByRoomNumberIgnoreCaseAndIdNot(String roomNumber, Long id);
}
