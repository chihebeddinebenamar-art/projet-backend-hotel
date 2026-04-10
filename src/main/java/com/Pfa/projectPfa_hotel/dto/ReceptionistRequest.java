package com.Pfa.projectPfa_hotel.dto;

import lombok.Data;

@Data
public class ReceptionistRequest {
    private String nom;
    private String prenom;
    private String email;
    /** Obligatoire à la création ; optionnel à la mise à jour (vide = conserver l’ancien mot de passe). */
    private String motDePasse;
    private String telephone;
}
