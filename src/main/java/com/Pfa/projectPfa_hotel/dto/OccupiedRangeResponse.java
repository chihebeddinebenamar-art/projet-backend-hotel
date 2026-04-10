package com.Pfa.projectPfa_hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Plage réservée pour affichage calendrier (sans données personnelles). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OccupiedRangeResponse {
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
