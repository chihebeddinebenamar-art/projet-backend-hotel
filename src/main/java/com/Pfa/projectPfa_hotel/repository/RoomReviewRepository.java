package com.Pfa.projectPfa_hotel.repository;

import com.Pfa.projectPfa_hotel.model.RoomReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoomReviewRepository extends JpaRepository<RoomReview, Long> {

    boolean existsByRoom_IdAndAuthorEmail(Long roomId, String authorEmail);

    Optional<RoomReview> findByRoom_IdAndAuthorEmail(Long roomId, String authorEmail);

    long countByRoom_Id(Long roomId);

    @Query("SELECT AVG(r.rating) FROM RoomReview r WHERE r.room.id = :roomId")
    Double averageRatingByRoomId(@Param("roomId") Long roomId);

    java.util.List<RoomReview> findByRoom_IdOrderByCreatedAtDesc(Long roomId);
}
