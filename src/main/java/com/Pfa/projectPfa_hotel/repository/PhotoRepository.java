package com.Pfa.projectPfa_hotel.repository;

import com.Pfa.projectPfa_hotel.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {

    List<Photo> findByRoom_IdOrderByPrimaryPhotoDescSortOrderAscIdAsc(Long roomId);

    long countByRoom_Id(Long roomId);
}
