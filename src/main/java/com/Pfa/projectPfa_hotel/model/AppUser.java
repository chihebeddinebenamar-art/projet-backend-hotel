package com.Pfa.projectPfa_hotel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, name = "password_hash")
    private String passwordHash;

    /**
     * Valeurs : ADMIN, RECEPTIONIST, CLIENT, STAFF (Spring Security ajoute le préfixe ROLE_)
     */
    @Column(nullable = false)
    private String role = "STAFF";

    @Column(length = 120)
    private String nom;

    @Column(length = 120)
    private String prenom;

    @Column(length = 40)
    private String telephone;

    public AppUser(String email, String passwordHash, String role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }
}
