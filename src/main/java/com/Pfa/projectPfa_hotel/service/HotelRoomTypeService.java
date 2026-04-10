package com.Pfa.projectPfa_hotel.service;

import com.Pfa.projectPfa_hotel.dto.RoomTypeDto;
import com.Pfa.projectPfa_hotel.dto.RoomTypeRequest;
import com.Pfa.projectPfa_hotel.exception.BadRequestException;
import com.Pfa.projectPfa_hotel.exception.RessourceNotFoundException;
import com.Pfa.projectPfa_hotel.model.HotelRoomType;
import com.Pfa.projectPfa_hotel.model.Room;
import com.Pfa.projectPfa_hotel.repository.HotelRoomTypeRepository;
import com.Pfa.projectPfa_hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelRoomTypeService {

    private final HotelRoomTypeRepository hotelRoomTypeRepository;
    private final RoomRepository roomRepository;

    public List<RoomTypeDto> findAll() {
        return hotelRoomTypeRepository.findAll().stream()
                .sorted(Comparator.comparing(HotelRoomType::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDto)
                .toList();
    }

    public RoomTypeDto create(RoomTypeRequest req) {
        validate(req);
        String name = req.getName().trim();
        if (hotelRoomTypeRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Ce nom de type existe déjà.");
        }
        HotelRoomType entity = HotelRoomType.builder()
                .name(name)
                .maxOccupancy(req.getMaxOccupancy())
                .build();
        return toDto(hotelRoomTypeRepository.save(entity));
    }

    @Transactional
    public RoomTypeDto update(Long id, RoomTypeRequest req) {
        validate(req);
        HotelRoomType rt = hotelRoomTypeRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Type de chambre introuvable."));
        String oldName = rt.getName();
        String newName = req.getName().trim();
        if (!oldName.equalsIgnoreCase(newName) && hotelRoomTypeRepository.existsByNameIgnoreCase(newName)) {
            throw new BadRequestException("Ce nom de type existe déjà.");
        }
        if (!oldName.equals(newName)) {
            List<Room> rooms = roomRepository.findByRoomType(oldName);
            for (Room r : rooms) {
                r.setRoomType(newName);
            }
            roomRepository.saveAll(rooms);
        }
        rt.setName(newName);
        rt.setMaxOccupancy(req.getMaxOccupancy());
        return toDto(hotelRoomTypeRepository.save(rt));
    }

    public void delete(Long id) {
        HotelRoomType rt = hotelRoomTypeRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Type de chambre introuvable."));
        long n = roomRepository.countByRoomType(rt.getName());
        if (n > 0) {
            throw new BadRequestException(
                    "Impossible de supprimer : " + n + " chambre(s) utilisent encore ce type.");
        }
        hotelRoomTypeRepository.delete(rt);
    }

    private void validate(RoomTypeRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new BadRequestException("Le nom du type est obligatoire.");
        }
        if (req.getMaxOccupancy() < 1) {
            throw new BadRequestException("Le nombre de personnes doit être au moins 1.");
        }
    }

    private RoomTypeDto toDto(HotelRoomType e) {
        return new RoomTypeDto(e.getId(), e.getName(), e.getMaxOccupancy());
    }
}
