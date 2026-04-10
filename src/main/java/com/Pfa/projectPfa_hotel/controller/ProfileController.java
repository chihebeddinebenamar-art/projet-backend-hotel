package com.Pfa.projectPfa_hotel.controller;

import com.Pfa.projectPfa_hotel.dto.UpdateProfileRequest;
import com.Pfa.projectPfa_hotel.dto.UserProfileResponse;
import com.Pfa.projectPfa_hotel.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile(Principal principal) {
        return ResponseEntity.ok(profileService.getProfile(principal.getName()));
    }

    @PutMapping
    public ResponseEntity<UserProfileResponse> updateProfile(
            Principal principal,
            @RequestBody UpdateProfileRequest body) {
        return ResponseEntity.ok(profileService.updateProfile(principal.getName(), body));
    }
}
