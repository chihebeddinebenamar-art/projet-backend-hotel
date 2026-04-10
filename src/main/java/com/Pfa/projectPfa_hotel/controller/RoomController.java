package com.Pfa.projectPfa_hotel.controller;

import com.Pfa.projectPfa_hotel.dto.PhotoUpdateRequest;
import com.Pfa.projectPfa_hotel.dto.RoomAccessoriesRequest;
import com.Pfa.projectPfa_hotel.model.Room;
import com.Pfa.projectPfa_hotel.response.PhotoResponse;
import com.Pfa.projectPfa_hotel.response.RoomResponse;
import com.Pfa.projectPfa_hotel.service.IRoomService;
import com.Pfa.projectPfa_hotel.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")

public class RoomController {

    private final IRoomService roomService;
    private final PhotoService photoService;

    /**
     * Ajouter une chambre (multipart). Rôle ADMIN + JWT obligatoires.
     * Champs : photo (fichier), roomNumber, roomType, roomPrice.
     */
    @PostMapping("/add/new-room")
    public ResponseEntity<RoomResponse> addNewRoom(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("roomNumber") String roomNumber,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws IOException {

        Room savedRoom = roomService.addNewRoom(photo, roomNumber, roomType, roomPrice);
        return ResponseEntity.status(201).body(roomService.getRoomDetail(savedRoom.getId()));
    }

    @GetMapping("/room-types")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    @GetMapping("/all-rooms")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRoomResponses());
    }

    /**
     * Détail d’une chambre : type, prix, URLs photos (Cloudinary), capacité max si le type existe.
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomDetail(roomId));
    }

    /**
     * Redirection HTTP vers l’URL Cloudinary de la photo principale (compatibilité
     * {@code /rooms/{id}/photo} côté client).
     */
    @GetMapping("/{roomId}/photo")
    public ResponseEntity<Void> getRoomPhoto(@PathVariable @NonNull Long roomId) {
        String url = roomService.getPrimaryPhotoUrl(roomId);
        if (url == null || url.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

    /**
     * Liste des photos d’une chambre (CRUD galerie).
     */
    @GetMapping("/{roomId}/photos")
    public ResponseEntity<List<PhotoResponse>> listRoomPhotos(@PathVariable Long roomId) {
        return ResponseEntity.ok(photoService.listByRoom(roomId));
    }

    /**
     * Ajouter une photo à une chambre (multipart). Champs : file ; optionnel sortOrder, primary.
     */
    @PostMapping(value = "/{roomId}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoResponse> addRoomPhoto(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder,
            @RequestParam(value = "primary", required = false) Boolean primary) throws IOException {
        return ResponseEntity.status(201).body(photoService.addPhoto(roomId, file, sortOrder, primary));
    }

    /**
     * Met à jour l’ordre ou le statut « principale » d’une photo.
     */
    @PutMapping(value = "/{roomId}/photos/{photoId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PhotoResponse> updateRoomPhoto(
            @PathVariable Long roomId,
            @PathVariable Long photoId,
            @RequestBody PhotoUpdateRequest body) {
        return ResponseEntity.ok(photoService.updatePhoto(roomId, photoId, body));
    }

    @DeleteMapping("/{roomId}/photos/{photoId}")
    public ResponseEntity<Void> deleteRoomPhoto(
            @PathVariable Long roomId,
            @PathVariable Long photoId) {
        photoService.deletePhoto(roomId, photoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Modifier une chambre (multipart). Rôle ADMIN + JWT.
     * Champs : roomNumber, roomType, roomPrice ; photo optionnelle (remplace la photo principale sur Cloudinary).
     */
    @PutMapping(value = "/{roomId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable Long roomId,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam("roomNumber") String roomNumber,
            @RequestParam("roomType") String roomType,
            @RequestParam("roomPrice") BigDecimal roomPrice) throws IOException {
        roomService.updateRoom(roomId, roomNumber, roomType, roomPrice, photo);
        return ResponseEntity.ok(roomService.getRoomDetail(roomId));
    }

    /**
     * Associer des accessoires à une chambre (remplace la liste précédente). JSON : { "accessoryIds": [1,2,3] }.
     */
    @PutMapping(value = "/{roomId}/accessories", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RoomResponse> setRoomAccessories(
            @PathVariable Long roomId,
            @RequestBody RoomAccessoriesRequest body) {
        List<Long> ids = body.getAccessoryIds() != null ? body.getAccessoryIds() : List.of();
        return ResponseEntity.ok(roomService.setRoomAccessories(roomId, ids));
    }

    /**
     * Supprimer une chambre par identifiant. Rôle ADMIN + JWT. Réponse 204 sans corps.
     */
    @DeleteMapping("/{roomId}/delete")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

}
