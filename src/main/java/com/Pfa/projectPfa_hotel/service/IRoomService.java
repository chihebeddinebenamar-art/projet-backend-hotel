package com.Pfa.projectPfa_hotel.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.Pfa.projectPfa_hotel.model.Room;
import com.Pfa.projectPfa_hotel.response.RoomResponse;

import java.util.List;

public interface IRoomService {
    Room addNewRoom(MultipartFile photo, String roomNumber, String roomType, BigDecimal roomPrice);

    List<String> getAllRoomTypes();

    List<Room> getAllRooms();

    /** URL HTTPS Cloudinary de la photo principale, ou null. */
    String getPrimaryPhotoUrl(Long roomId);

    void deleteRoom(Long roomID);

    Room updateRoom(Long roomId, String roomNumber, String roomType, BigDecimal roomPrice, MultipartFile photo);

    Room getRoomById(Long roomId);

    /** Détail chambre pour l’API : URLs photos + capacité du type si connue */
    RoomResponse getRoomDetail(Long roomId);

    /** Liste des chambres avec accessoires chargés (pour l’API). */
    List<RoomResponse> getAllRoomResponses();

    /** Remplace les accessoires liés à la chambre. */
    RoomResponse setRoomAccessories(Long roomId, List<Long> accessoryIds);

    Page<Room> searchRooms(String roomType, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
}
