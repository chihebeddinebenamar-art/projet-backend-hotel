package com.Pfa.projectPfa_hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomReviewsPageResponse {
    /** Moyenne sur 5, ou null si aucun avis */
    private Double averageRating;
    private long count;
    /** Présent si JWT envoyé : l’utilisateur a déjà un avis pour cette chambre */
    private Boolean currentUserHasReviewed;
    private List<RoomReviewResponse> reviews;
}
