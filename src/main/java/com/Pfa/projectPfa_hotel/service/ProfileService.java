package com.Pfa.projectPfa_hotel.service;

import com.Pfa.projectPfa_hotel.dto.UpdateProfileRequest;
import com.Pfa.projectPfa_hotel.dto.UserProfileResponse;
import com.Pfa.projectPfa_hotel.exception.BadRequestException;
import com.Pfa.projectPfa_hotel.exception.RessourceNotFoundException;
import com.Pfa.projectPfa_hotel.model.AppUser;
import com.Pfa.projectPfa_hotel.repository.AppUserRepository;
import com.Pfa.projectPfa_hotel.security.JwtService;
import com.Pfa.projectPfa_hotel.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public UserProfileResponse getProfile(String email) {
        AppUser u = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable."));
        return toResponse(u, null, null);
    }

    @Transactional
    public UserProfileResponse updateProfile(String currentEmail, UpdateProfileRequest req) {
        AppUser u = appUserRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RessourceNotFoundException("Utilisateur introuvable."));

        if (req.getNom() != null) {
            u.setNom(StringUtils.hasText(req.getNom()) ? req.getNom().trim() : null);
        }
        if (req.getPrenom() != null) {
            u.setPrenom(StringUtils.hasText(req.getPrenom()) ? req.getPrenom().trim() : null);
        }
        if (req.getTelephone() != null) {
            u.setTelephone(StringUtils.hasText(req.getTelephone()) ? req.getTelephone().trim() : null);
        }

        String newEmail = req.getEmail() != null ? req.getEmail().trim().toLowerCase() : null;
        boolean emailChange = StringUtils.hasText(newEmail) && !newEmail.equals(u.getEmail());
        boolean passwordChange = StringUtils.hasText(req.getNewPassword());

        if (emailChange || passwordChange) {
            if (!StringUtils.hasText(req.getCurrentPassword())
                    || !passwordEncoder.matches(req.getCurrentPassword(), u.getPasswordHash())) {
                throw new BadRequestException("Mot de passe actuel incorrect ou manquant.");
            }
        }

        if (emailChange) {
            if (appUserRepository.existsByEmail(newEmail)) {
                throw new BadRequestException("Cet email est déjà utilisé.");
            }
            u.setEmail(newEmail);
        }

        if (passwordChange) {
            if (req.getNewPassword().length() < 6) {
                throw new BadRequestException("Le nouveau mot de passe doit contenir au moins 6 caractères.");
            }
            u.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        }

        appUserRepository.save(u);

        String token = null;
        if (emailChange) {
            UserDetails details = userDetailsService.loadUserByUsername(u.getEmail());
            token = jwtService.generateToken(details);
        }

        String message = "Profil mis à jour.";
        return toResponse(u, token, message);
    }

    private UserProfileResponse toResponse(AppUser u, String token, String message) {
        return new UserProfileResponse(
                u.getId(),
                u.getEmail(),
                u.getNom(),
                u.getPrenom(),
                u.getTelephone(),
                u.getRole(),
                token,
                message);
    }
}
