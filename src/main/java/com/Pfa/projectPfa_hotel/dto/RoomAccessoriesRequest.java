package com.Pfa.projectPfa_hotel.dto;

import lombok.Data;

import java.util.List;

@Data
public class RoomAccessoriesRequest {
    /** Liste des identifiants d’accessoires à associer à la chambre (remplace l’association précédente). */
    private List<Long> accessoryIds;
}
