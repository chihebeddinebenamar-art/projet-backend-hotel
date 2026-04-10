package com.Pfa.projectPfa_hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomReviewResponse {
    private Long id;
    private String authorLabel;
    private int rating;
    private String comment;
    private Instant createdAt;
}
