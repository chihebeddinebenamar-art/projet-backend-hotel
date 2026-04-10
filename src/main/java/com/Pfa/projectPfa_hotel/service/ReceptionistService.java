package com.Pfa.projectPfa_hotel.service;

import com.Pfa.projectPfa_hotel.dto.ReceptionistRequest;
import com.Pfa.projectPfa_hotel.dto.ReceptionistResponse;
import com.Pfa.projectPfa_hotel.exception.BadRequestException;
import com.Pfa.projectPfa_hotel.exception.RessourceNotFoundException;
import com.Pfa.projectPfa_hotel.model.AppUser;
import com.Pfa.projectPfa_hotel.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceptionistService {

    private static final String ROLE_RECEPTIONIST = "RECEPTIONIST";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public List<ReceptionistResponse> findAll() {
        return appUserRepository.findByRoleOrderByNomAscPrenomAsc(ROLE_RECEPTIONIST).stream()
                .map(this::toResponse)
                .toList();
    }

    public ReceptionistResponse findById(Long id) {
        AppUser u = appUserRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Réceptionniste introuvable."));
        if (!ROLE_RECEPTIONIST.equals(u.getRole())) {
            throw new BadRequestException("Cet utilisateur n’est pas un réceptionniste.");
        }
        return toResponse(u);
    }

    @Transactional
    public ReceptionistResponse create(ReceptionistRequest req) {
        validateRequired(req, true);
        String email = req.getEmail().trim().toLowerCase();
        if (appUserRepository.existsByEmail(email)) {
            throw new BadRequestException("Cet email est déjà utilisé.");
        }
        if (req.getMotDePasse() == null || req.getMotDePasse().length() < 6) {
            throw new BadRequestException("Le mot de passe doit contenir au moins 6 caractères.");
        }
        AppUser u = new AppUser(email, passwordEncoder.encode(req.getMotDePasse()), ROLE_RECEPTIONIST);
        u.setNom(req.getNom().trim());
        u.setPrenom(req.getPrenom().trim());
        u.setTelephone(StringUtils.hasText(req.getTelephone()) ? req.getTelephone().trim() : null);
        return toResponse(appUserRepository.save(u));
    }

    @Transactional
    public ReceptionistResponse update(Long id, ReceptionistRequest req) {
        validateRequired(req, false);
        AppUser u = appUserRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Réceptionniste introuvable."));
        if (!ROLE_RECEPTIONIST.equals(u.getRole())) {
            throw new BadRequestException("Cet utilisateur n’est pas un réceptionniste.");
        }
        String email = req.getEmail().trim().toLowerCase();
        if (!email.equals(u.getEmail()) && appUserRepository.existsByEmail(email)) {
            throw new BadRequestException("Cet email est déjà utilisé.");
        }
        u.setEmail(email);
        u.setNom(req.getNom().trim());
        u.setPrenom(req.getPrenom().trim());
        u.setTelephone(StringUtils.hasText(req.getTelephone()) ? req.getTelephone().trim() : null);
        if (StringUtils.hasText(req.getMotDePasse())) {
            if (req.getMotDePasse().length() < 6) {
                throw new BadRequestException("Le mot de passe doit contenir au moins 6 caractères.");
            }
            u.setPasswordHash(passwordEncoder.encode(req.getMotDePasse()));
        }
        return toResponse(appUserRepository.save(u));
    }

    @Transactional
    public void delete(Long id) {
        AppUser u = appUserRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Réceptionniste introuvable."));
        if (!ROLE_RECEPTIONIST.equals(u.getRole())) {
            throw new BadRequestException("Suppression impossible : ce n’est pas un réceptionniste.");
        }
        appUserRepository.delete(u);
    }

    private void validateRequired(ReceptionistRequest req, boolean create) {
        if (req.getNom() == null || req.getNom().isBlank()) {
            throw new BadRequestException("Le nom est obligatoire.");
        }
        if (req.getPrenom() == null || req.getPrenom().isBlank()) {
            throw new BadRequestException("Le prénom est obligatoire.");
        }
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new BadRequestException("L’email est obligatoire.");
        }
        if (create && (req.getMotDePasse() == null || req.getMotDePasse().isBlank())) {
            throw new BadRequestException("Le mot de passe est obligatoire à la création.");
        }
    }

    private ReceptionistResponse toResponse(AppUser u) {
        return new ReceptionistResponse(
                u.getId(),
                u.getNom(),
                u.getPrenom(),
                u.getEmail(),
                u.getTelephone());
    }
}
