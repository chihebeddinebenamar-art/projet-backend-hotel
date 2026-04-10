package com.Pfa.projectPfa_hotel.dto;

import lombok.Data;

/** Note 1–5 et commentaire optionnel. */
@Data
public class CreateRoomReviewRequest {
    private int rating;
    private String comment;
}
