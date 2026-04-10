package com.Pfa.projectPfa_hotel.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String nom;
    private String prenom;
    private String telephone;
    /** Nouvel email (si différent de l’actuel, le mot de passe actuel est requis). */
    private String email;
    private String currentPassword;
    private String newPassword;
}
