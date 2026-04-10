package com.Pfa.projectPfa_hotel.service;

import com.Pfa.projectPfa_hotel.dto.AccessoryDto;
import com.Pfa.projectPfa_hotel.dto.AccessoryRequest;
import com.Pfa.projectPfa_hotel.exception.BadRequestException;
import com.Pfa.projectPfa_hotel.exception.RessourceNotFoundException;
import com.Pfa.projectPfa_hotel.model.Accessory;
import com.Pfa.projectPfa_hotel.repository.AccessoryRepository;
import com.Pfa.projectPfa_hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessoryService {

    private final AccessoryRepository accessoryRepository;
    private final RoomRepository roomRepository;

    public List<AccessoryDto> findAll() {
        return accessoryRepository.findAll().stream()
                .sorted(Comparator.comparing(Accessory::getName, String.CASE_INSENSITIVE_ORDER))
                .map(this::toDto)
                .toList();
    }

    public AccessoryDto create(AccessoryRequest req) {
        validate(req);
        String name = req.getName().trim();
        if (accessoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Ce nom d’accessoire existe déjà.");
        }
        Accessory entity = Accessory.builder()
                .name(name)
                .description(req.getDescription() != null ? req.getDescription().trim() : null)
                .build();
        return toDto(accessoryRepository.save(entity));
    }

    @Transactional
    public AccessoryDto update(Long id, AccessoryRequest req) {
        validate(req);
        Accessory a = accessoryRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Accessoire introuvable."));
        String name = req.getName().trim();
        if (!a.getName().equalsIgnoreCase(name) && accessoryRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException("Ce nom d’accessoire existe déjà.");
        }
        a.setName(name);
        a.setDescription(req.getDescription() != null && !req.getDescription().isBlank()
                ? req.getDescription().trim() : null);
        return toDto(accessoryRepository.save(a));
    }

    public void delete(Long id) {
        Accessory a = accessoryRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Accessoire introuvable."));
        long n = roomRepository.countRoomsWithAccessory(id);
        if (n > 0) {
            throw new BadRequestException(
                    "Impossible de supprimer : " + n + " chambre(s) utilisent encore cet accessoire.");
        }
        accessoryRepository.delete(a);
    }

    private void validate(AccessoryRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new BadRequestException("Le nom de l’accessoire est obligatoire.");
        }
    }

    private AccessoryDto toDto(Accessory e) {
        return new AccessoryDto(e.getId(), e.getName(), e.getDescription());
    }
}
