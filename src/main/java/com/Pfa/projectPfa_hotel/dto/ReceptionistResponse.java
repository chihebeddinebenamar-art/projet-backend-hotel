package com.Pfa.projectPfa_hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionistResponse {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
}
