package com.Pfa.projectPfa_hotel.controller;

import com.Pfa.projectPfa_hotel.dto.AuthResponse;
import com.Pfa.projectPfa_hotel.dto.LoginRequest;
import com.Pfa.projectPfa_hotel.dto.RegisterRequest;
import com.Pfa.projectPfa_hotel.model.AppUser;
import com.Pfa.projectPfa_hotel.repository.AppUserRepository;
import com.Pfa.projectPfa_hotel.security.CustomUserDetailsService;
import com.Pfa.projectPfa_hotel.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().length() < 6) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, "Email et mot de passe (6 caractères min.) requis."));
        }
        if (appUserRepository.existsByEmail(request.getEmail().trim())) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, "Cet email est déjà enregistré."));
        }
        AppUser user = new AppUser(
                request.getEmail().trim().toLowerCase(),
                passwordEncoder.encode(request.getPassword()),
                "CLIENT");
        if (StringUtils.hasText(request.getNom())) {
            user.setNom(request.getNom().trim());
        }
        if (StringUtils.hasText(request.getPrenom())) {
            user.setPrenom(request.getPrenom().trim());
        }
        if (StringUtils.hasText(request.getTelephone())) {
            user.setTelephone(request.getTelephone().trim());
        }
        appUserRepository.save(user);
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(details);
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getRole(), "Inscription réussie."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, null, null, "Email et mot de passe requis."));
        }
        String email = request.getEmail().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable après authentification."));
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(details);
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getRole(), "Connexion réussie."));
    }
}
