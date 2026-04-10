package com.Pfa.projectPfa_hotel.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.Pfa.projectPfa_hotel.exception.BadRequestException;
import com.Pfa.projectPfa_hotel.exception.InternalServiceException;
import com.Pfa.projectPfa_hotel.exception.RessourceNotFoundException;
import com.Pfa.projectPfa_hotel.model.Accessory;
import com.Pfa.projectPfa_hotel.model.Room;
import com.Pfa.projectPfa_hotel.response.RoomResponse;
import com.Pfa.projectPfa_hotel.repository.AccessoryRepository;
import com.Pfa.projectPfa_hotel.repository.HotelRoomTypeRepository;
import com.Pfa.projectPfa_hotel.repository.RoomRepository;
import com.Pfa.projectPfa_hotel.util.RoomMapper;
import com.Pfa.projectPfa_hotel.specification.RoomSpecification;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class RoomService implements IRoomService {
    private final RoomRepository roomRepository;
    private final HotelRoomTypeRepository hotelRoomTypeRepository;
    private final AccessoryRepository accessoryRepository;
    private final PhotoService photoService;

    @Override
    @Transactional
    public Room addNewRoom(MultipartFile file, String roomNumber, String roomType, BigDecimal roomPrice) {
        try {
            if (roomNumber == null || roomNumber.isBlank()) {
                throw new BadRequestException("Le numéro de chambre est obligatoire.");
            }
            String roomNumberTrim = roomNumber.trim();
            if (roomRepository.existsByRoomNumberIgnoreCase(roomNumberTrim)) {
                throw new BadRequestException("Ce numéro de chambre existe déjà.");
            }
            if (roomType == null || roomType.isBlank()) {
                throw new BadRequestException("Le type de chambre est obligatoire.");
            }
            String typeTrim = roomType.trim();
            if (hotelRoomTypeRepository.count() > 0
                    && hotelRoomTypeRepository.findByNameIgnoreCase(typeTrim).isEmpty()) {
                throw new BadRequestException(
                        "Type de chambre inconnu. Ajoutez-le d'abord dans le catalogue des types (admin).");
            }
            Room room = new Room();
            room.setRoomNumber(roomNumberTrim);
            room.setRoomType(typeTrim);
            room.setRoomPrice(roomPrice);
            room = roomRepository.save(room);

            if (file != null && !file.isEmpty()) {
                photoService.addPhoto(room.getId(), file, 0, true);
            }

            return roomRepository.findById(room.getId())
                    .orElseThrow(() -> new RessourceNotFoundException("Chambre introuvable après création."));

        } catch (BadRequestException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture ou envoi de la photo: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Erreur enregistrement chambre: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getAllRoomTypes() {
        if (hotelRoomTypeRepository.count() > 0) {
            return hotelRoomTypeRepository.findAll().stream()
                    .map(t -> t.getName())
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
        }
        return roomRepository.findDistinctRoomTypes();
    }

    @Override
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    @Override
    public String getPrimaryPhotoUrl(Long roomId) {
        return photoService.getPrimaryPhotoUrl(roomId);
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomID) {
        Optional<Room> theRoom = roomRepository.findById(roomID);
        if (theRoom.isPresent()) {
            photoService.deleteAllPhotosForRoom(roomID);
            roomRepository.deleteById(roomID);
        } else {
            throw new RessourceNotFoundException("Sorry, Room not found!");
        }
    }

    @Override
    public Room getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new RessourceNotFoundException("Sorry,Room not found !"));
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoomDetail(Long roomId) {
        Room room = getRoomById(roomId);
        room.getAccessories().size();
        room.getPhotos().size();
        RoomResponse response = RoomMapper.toResponse(room);
        hotelRoomTypeRepository.findByNameIgnoreCase(room.getRoomType())
                .ifPresent(t -> response.setMaxOccupancy(t.getMaxOccupancy()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRoomResponses() {
        return roomRepository.findAll().stream()
                .map(r -> {
                    r.getAccessories().size();
                    r.getPhotos().size();
                    RoomResponse response = RoomMapper.toResponse(r);
                    hotelRoomTypeRepository.findByNameIgnoreCase(r.getRoomType())
                            .ifPresent(t -> response.setMaxOccupancy(t.getMaxOccupancy()));
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional
    public RoomResponse setRoomAccessories(Long roomId, List<Long> accessoryIds) {
        Room room = getRoomById(roomId);
        room.getAccessories().clear();
        if (accessoryIds != null && !accessoryIds.isEmpty()) {
            List<Accessory> found = accessoryRepository.findAllById(accessoryIds);
            if (found.size() != accessoryIds.size()) {
                throw new BadRequestException("Un ou plusieurs accessoires sont invalides.");
            }
            room.getAccessories().addAll(found);
        }
        roomRepository.save(room);
        return getRoomDetail(roomId);
    }

    @Override
    public Page<Room> searchRooms(String roomType, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Room> spec = RoomSpecification.combined(roomType, minPrice, maxPrice);
        return roomRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public Room updateRoom(Long roomId, String roomNumber, String roomType, BigDecimal roomPrice, MultipartFile photoFile) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RessourceNotFoundException("Sorry, Room not found!"));
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new BadRequestException("Le numéro de chambre est obligatoire.");
        }
        String roomNumberTrim = roomNumber.trim();
        if (roomRepository.existsByRoomNumberIgnoreCaseAndIdNot(roomNumberTrim, roomId)) {
            throw new BadRequestException("Ce numéro de chambre existe déjà.");
        }
        room.setRoomNumber(roomNumberTrim);
        if (roomType != null) {
            String trimmed = roomType.trim();
            if (hotelRoomTypeRepository.count() > 0
                    && hotelRoomTypeRepository.findByNameIgnoreCase(trimmed).isEmpty()) {
                throw new BadRequestException(
                        "Type de chambre inconnu. Ajoutez-le d'abord dans le catalogue des types (admin).");
            }
            room.setRoomType(trimmed);
        }
        if (roomPrice != null) {
            room.setRoomPrice(roomPrice);
        }
        if (photoFile != null && !photoFile.isEmpty()) {
            try {
                photoService.replacePrimaryPhoto(roomId, photoFile);
            } catch (IOException e) {
                throw new InternalServiceException("Erreur lors de l’upload de la photo.");
            }
        }
        return roomRepository.save(room);
    }
}
