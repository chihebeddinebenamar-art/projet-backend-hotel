package com.Pfa.projectPfa_hotel.controller;

import com.Pfa.projectPfa_hotel.dto.CreateRoomReviewRequest;
import com.Pfa.projectPfa_hotel.dto.RoomReviewResponse;
import com.Pfa.projectPfa_hotel.dto.RoomReviewsPageResponse;
import com.Pfa.projectPfa_hotel.service.IRoomReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/room-reviews")
@RequiredArgsConstructor
public class RoomReviewController {

    private final IRoomReviewService roomReviewService;

    /**
     * Avis d’une chambre (public). Avec en-tête Authorization, indique si l’utilisateur a déjà noté.
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<RoomReviewsPageResponse> listForRoom(
            @PathVariable Long roomId,
            Principal principal) {
        String email = principal != null ? principal.getName() : null;
        return ResponseEntity.ok(roomReviewService.getForRoom(roomId, email));
    }

    /**
     * Publier un avis (comptes CLIENT ou STAFF — même règle que l’espace client).
     * Un seul avis par email et par chambre.
     */
    @PostMapping("/room/{roomId}")
    @PreAuthorize("hasAnyRole('CLIENT','STAFF')")
    public ResponseEntity<RoomReviewResponse> create(
            @PathVariable Long roomId,
            @RequestBody CreateRoomReviewRequest body,
            Principal principal) {
        RoomReviewResponse created = roomReviewService.create(roomId, principal.getName(), body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
