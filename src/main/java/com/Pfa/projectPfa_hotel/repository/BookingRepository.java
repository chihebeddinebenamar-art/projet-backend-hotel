package com.Pfa.projectPfa_hotel.repository;

import com.Pfa.projectPfa_hotel.model.BookedRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<BookedRoom, Long> {

    @Query("SELECT DISTINCT b FROM BookedRoom b JOIN FETCH b.room")
    List<BookedRoom> findAllWithRoom();

    long countByCheckInDate(LocalDate date);

    long countByCheckOutDate(LocalDate date);

    @Query("SELECT COUNT(DISTINCT b.room.id) FROM BookedRoom b WHERE b.checkInDate <= :d AND b.checkOutDate > :d")
    long countOccupiedRoomsOnDate(@Param("d") LocalDate d);

    @Query("SELECT b FROM BookedRoom b JOIN FETCH b.room WHERE b.room.id = :roomId")
    List<BookedRoom> findByRoomIdWithRoom(@Param("roomId") Long roomId);

    @Query("SELECT b FROM BookedRoom b JOIN FETCH b.room WHERE LOWER(TRIM(b.guestEmail)) = LOWER(TRIM(:email)) ORDER BY b.checkInDate DESC")
    List<BookedRoom> findByGuestEmailWithRoom(@Param("email") String email);

    /**
     * Réception : séjours concernant la date (arrivées, départs, ou nuitées ce jour).
     */
    @Query("SELECT DISTINCT b FROM BookedRoom b JOIN FETCH b.room WHERE "
            + "(b.checkInDate <= :d AND b.checkOutDate > :d) OR b.checkOutDate = :d OR b.checkInDate = :d "
            + "ORDER BY b.checkInDate ASC, b.checkOutDate ASC")
    List<BookedRoom> findActiveOnDateWithRoom(@Param("d") LocalDate d);

    BookedRoom findByBookingConfirmationCode(String confirmationCode);
}
