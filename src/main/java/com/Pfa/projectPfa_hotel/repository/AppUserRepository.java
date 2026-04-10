package com.Pfa.projectPfa_hotel.repository;

import com.Pfa.projectPfa_hotel.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    long countByRole(String role);

    List<AppUser> findByRoleOrderByNomAscPrenomAsc(String role);
}
