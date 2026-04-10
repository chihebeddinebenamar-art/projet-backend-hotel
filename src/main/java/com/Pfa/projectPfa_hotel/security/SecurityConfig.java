package com.Pfa.projectPfa_hotel.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/room-types").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/room-types").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/room-types/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/room-types/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/accessories").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/accessories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/accessories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/accessories/**").hasRole("ADMIN")
                        .requestMatchers("/api/receptionists", "/api/receptionists/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/room-reviews/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/rooms/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/create-intent").permitAll()
                        .requestMatchers(HttpMethod.POST, "/bookings/room/*/booking").permitAll()
                        .requestMatchers(HttpMethod.GET, "/bookings/room/*/occupied-ranges").permitAll()
                        .requestMatchers(HttpMethod.GET, "/bookings/room/*/availability").permitAll()
                        .requestMatchers(HttpMethod.GET, "/bookings/confirmation/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/bookings/all-bookings").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/bookings/my").authenticated()
                        .requestMatchers(HttpMethod.GET, "/bookings/today").hasAnyRole("RECEPTIONIST", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/bookings/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stats/reception-dashboard").hasAnyRole("RECEPTIONIST", "ADMIN")
                        .requestMatchers("/api/stats/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/rooms/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/bookings/room/*/bookings").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
