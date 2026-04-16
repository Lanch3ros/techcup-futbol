package com.example.core.service;

import com.example.core.model.User;
import com.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("Autenticando usuario con email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado para autenticación - email: {}", email);
                    return new UsernameNotFoundException("Usuario no encontrado: " + email);
                });

        String role = resolveRole(user);
        log.info("Usuario autenticado - email: {}, rol: {}", email, role);

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    /**
     * Determina el rol de seguridad del usuario.
     * Usa el campo {@code role} si está definido; si no, deriva uno del tipo de usuario:
     * ADMIN → ADMIN, REFEREE → ARBITRO, cualquier otro tipo → JUGADOR.
     */
    private String resolveRole(User user) {
        if (user.getRole() != null && !user.getRole().isBlank()) {
            return user.getRole().toUpperCase();
        }
        return switch (user.getUserType().toUpperCase()) {
            case "ADMIN"    -> "ADMIN";
            case "ORGANIZER"-> "ORGANIZADOR";
            case "REFEREE"  -> "ARBITRO";
            default         -> "JUGADOR";
        };
    }
}
