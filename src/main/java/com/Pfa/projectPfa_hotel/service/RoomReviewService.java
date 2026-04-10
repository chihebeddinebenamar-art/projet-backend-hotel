package com.Pfa.projectPfa_hotel.service;

import com.Pfa.projectPfa_hotel.dto.CreateRoomReviewRequest;
import com.Pfa.projectPfa_hotel.dto.RoomReviewResponse;
import com.Pfa.projectPfa_hotel.dto.RoomReviewsPageResponse;
import com.Pfa.projectPfa_hotel.exception.BadRequestException;
import com.Pfa.projectPfa_hotel.exception.ConflictException;
import com.Pfa.projectPfa_hotel.exception.RessourceNotFoundException;
import com.Pfa.projectPfa_hotel.model.AppUser;
import com.Pfa.projectPfa_hotel.model.Room;
import com.Pfa.projectPfa_hotel.model.RoomReview;
import com.Pfa.projectPfa_hotel.repository.AppUserRepository;
import com.Pfa.projectPfa_hotel.repository.RoomRepository;
import com.Pfa.projectPfa_hotel.repository.RoomReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomReviewService implements IRoomReviewService {

    private final RoomReviewRepository roomReviewRepository;
    private final RoomRepository roomRepository;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional(readOnly = true)
    public RoomReviewsPageResponse getForRoom(Long roomId, String viewerEmailOrNull) {
        ensureRoomExists(roomId);
        long count = roomReviewRepository.countByRoom_Id(roomId);
        Double avg = count == 0 ? null : roomReviewRepository.averageRatingByRoomId(roomId);
        if (avg != null) {
            avg = Math.round(avg * 10.0) / 10.0;
        }
        Boolean hasReviewed = null;
        if (viewerEmailOrNull != null && !viewerEmailOrNull.isBlank()) {
            hasReviewed = roomReviewRepository.existsByRoom_IdAndAuthorEmail(
                    roomId, viewerEmailOrNull.trim().toLowerCase());
        }
        List<RoomReviewResponse> list = roomReviewRepository.findByRoom_IdOrderByCreatedAtDesc(roomId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return RoomReviewsPageResponse.builder()
                .averageRating(avg)
                .count(count)
                .currentUserHasReviewed(hasReviewed)
                .reviews(list)
                .build();
    }

    @Override
    @Transactional
    public RoomReviewResponse create(Long roomId, String authorEmail, CreateRoomReviewRequest request) {
        if (request == null) {
            throw new BadRequestException("Corps de requête manquant.");
        }
        int rating = request.getRating();
        if (rating < 1 || rating > 5) {
            throw new BadRequestException("La note doit être entre 1 et 5.");
        }
        String comment = request.getComment();
        if (comment != null && comment.length() > 2000) {
            throw new BadRequestException("Le commentaire ne peut pas dépasser 2000 caractères.");
        }
        if (comment != null) {
            comment = comment.trim();
            if (comment.isEmpty()) {
                comment = null;
            }
        }
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RessourceNotFoundException("Chambre introuvable."));
        if (authorEmail == null || authorEmail.isBlank()) {
            throw new BadRequestException("Email utilisateur manquant.");
        }
        AppUser user = appUserRepository.findByEmailIgnoreCase(authorEmail.trim())
                .orElseThrow(() -> new BadRequestException("Compte introuvable."));
        String canonical = user.getEmail().toLowerCase();
        if (roomReviewRepository.existsByRoom_IdAndAuthorEmail(roomId, canonical)) {
            throw new ConflictException("Vous avez déjà publié un avis pour cette chambre.");
        }
        RoomReview entity = new RoomReview();
        entity.setRoom(room);
        entity.setAuthorEmail(canonical);
        entity.setAuthorDisplayName(buildAuthorLabel(user));
        entity.setRating(rating);
        entity.setComment(comment);
        RoomReview saved = roomReviewRepository.save(entity);
        return toResponse(saved);
    }

    private void ensureRoomExists(Long roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new RessourceNotFoundException("Chambre introuvable.");
        }
    }

    private RoomReviewResponse toResponse(RoomReview r) {
        String label = r.getAuthorDisplayName();
        if (label == null || label.isBlank()) {
            label = "Client";
        }
        return RoomReviewResponse.builder()
                .id(r.getId())
                .authorLabel(label)
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private String buildAuthorLabel(AppUser user) {
        String prenom = user.getPrenom();
        String nom = user.getNom();
        if (prenom != null && !prenom.isBlank()) {
            String n = (nom != null && !nom.isBlank()) ? nom.trim().substring(0, 1) + "." : "";
            return (prenom.trim() + " " + n).trim();
        }
        return maskEmail(user.getEmail());
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "Client";
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return "***";
        }
        return email.substring(0, Math.min(2, at)) + "***";
    }
}
