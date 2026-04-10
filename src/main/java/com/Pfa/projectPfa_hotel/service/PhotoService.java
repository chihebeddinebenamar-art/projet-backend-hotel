package com.Pfa.projectPfa_hotel.service;

import com.Pfa.projectPfa_hotel.dto.PhotoUpdateRequest;
import com.Pfa.projectPfa_hotel.exception.BadRequestException;
import com.Pfa.projectPfa_hotel.exception.RessourceNotFoundException;
import com.Pfa.projectPfa_hotel.model.Photo;
import com.Pfa.projectPfa_hotel.model.Room;
import com.Pfa.projectPfa_hotel.repository.PhotoRepository;
import com.Pfa.projectPfa_hotel.repository.RoomRepository;
import com.Pfa.projectPfa_hotel.response.PhotoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final RoomRepository roomRepository;
    private final CloudinaryStorageService cloudinaryStorageService;

    public List<PhotoResponse> listByRoom(Long roomId) {
        ensureRoomExists(roomId);
        return photoRepository.findByRoom_IdOrderByPrimaryPhotoDescSortOrderAscIdAsc(roomId).stream()
                .map(this::toDto)
                .toList();
    }

    /** URL de la photo principale (tri : principale d’abord), ou null. */
    public String getPrimaryPhotoUrl(Long roomId) {
        List<Photo> list = photoRepository.findByRoom_IdOrderByPrimaryPhotoDescSortOrderAscIdAsc(roomId);
        return list.isEmpty() ? null : list.get(0).getUrl();
    }

    @Transactional
    public PhotoResponse addPhoto(Long roomId, MultipartFile file, Integer sortOrder, Boolean markPrimary)
            throws IOException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RessourceNotFoundException("Chambre introuvable."));
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Fichier image requis.");
        }
        long count = photoRepository.countByRoom_Id(roomId);
        boolean primary = Boolean.TRUE.equals(markPrimary) || count == 0;
        if (primary) {
            clearPrimaryFlags(roomId);
        }
        String[] uploaded = cloudinaryStorageService.uploadRoomImage(roomId, file);
        int order = sortOrder != null ? sortOrder : (int) count;
        Photo p = Photo.builder()
                .room(room)
                .url(uploaded[0])
                .publicId(uploaded[1])
                .sortOrder(order)
                .primaryPhoto(primary)
                .build();
        return toDto(photoRepository.save(p));
    }

    @Transactional
    public void deletePhoto(Long roomId, Long photoId) {
        Photo p = photoRepository.findById(photoId)
                .orElseThrow(() -> new RessourceNotFoundException("Photo introuvable."));
        if (p.getRoom().getId() != roomId) {
            throw new BadRequestException("Cette photo n’appartient pas à cette chambre.");
        }
        cloudinaryStorageService.deleteByPublicId(p.getPublicId());
        photoRepository.delete(p);
    }

    @Transactional
    public PhotoResponse updatePhoto(Long roomId, Long photoId, PhotoUpdateRequest req) {
        Photo p = photoRepository.findById(photoId)
                .orElseThrow(() -> new RessourceNotFoundException("Photo introuvable."));
        if (p.getRoom().getId() != roomId) {
            throw new BadRequestException("Cette photo n’appartient pas à cette chambre.");
        }
        if (req.getSortOrder() != null) {
            p.setSortOrder(req.getSortOrder());
        }
        if (Boolean.TRUE.equals(req.getPrimaryPhoto())) {
            clearPrimaryFlags(roomId);
            p.setPrimaryPhoto(true);
        } else if (req.getPrimaryPhoto() != null && !req.getPrimaryPhoto()) {
            p.setPrimaryPhoto(false);
        }
        return toDto(photoRepository.save(p));
    }

    /**
     * Remplace la photo principale (ou la seule) lors de la mise à jour de chambre avec nouveau fichier.
     */
    @Transactional
    public void replacePrimaryPhoto(Long roomId, MultipartFile file) throws IOException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RessourceNotFoundException("Chambre introuvable."));
        if (file == null || file.isEmpty()) {
            return;
        }
        List<Photo> photos = photoRepository.findByRoom_IdOrderByPrimaryPhotoDescSortOrderAscIdAsc(roomId);
        Photo target = photos.stream()
                .filter(Photo::isPrimaryPhoto)
                .findFirst()
                .orElseGet(() -> photos.isEmpty() ? null : photos.get(0));

        String[] uploaded = cloudinaryStorageService.uploadRoomImage(roomId, file);
        if (target != null) {
            cloudinaryStorageService.deleteByPublicId(target.getPublicId());
            target.setUrl(uploaded[0]);
            target.setPublicId(uploaded[1]);
            target.setPrimaryPhoto(true);
            photoRepository.save(target);
        } else {
            Photo p = Photo.builder()
                    .room(room)
                    .url(uploaded[0])
                    .publicId(uploaded[1])
                    .sortOrder(0)
                    .primaryPhoto(true)
                    .build();
            photoRepository.save(p);
        }
    }

    @Transactional
    public void deleteAllPhotosForRoom(Long roomId) {
        List<Photo> list = photoRepository.findByRoom_IdOrderByPrimaryPhotoDescSortOrderAscIdAsc(roomId);
        for (Photo p : list) {
            cloudinaryStorageService.deleteByPublicId(p.getPublicId());
        }
        photoRepository.deleteAll(list);
    }

    private void ensureRoomExists(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new RessourceNotFoundException("Chambre introuvable.");
        }
    }

    private void clearPrimaryFlags(Long roomId) {
        List<Photo> photos = photoRepository.findByRoom_IdOrderByPrimaryPhotoDescSortOrderAscIdAsc(roomId);
        for (Photo ph : photos) {
            if (ph.isPrimaryPhoto()) {
                ph.setPrimaryPhoto(false);
                photoRepository.save(ph);
            }
        }
    }

    private PhotoResponse toDto(Photo p) {
        return new PhotoResponse(p.getId(), p.getUrl(), p.getSortOrder(), p.isPrimaryPhoto());
    }
}
