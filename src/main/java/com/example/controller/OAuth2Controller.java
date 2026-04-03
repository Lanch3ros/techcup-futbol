package com.example.controller;

import com.example.controller.dto.request.GoogleAuthRequest;
import com.example.controller.dto.response.AuthResponse;
import com.example.controller.dto.response.GenericResponse;
import com.example.core.model.RelativePlayer;
import com.example.core.service.CustomUserDetailsService;
import com.example.core.service.GoogleTokenVerifierService;
import com.example.core.service.JwtService;
import com.example.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoint de login y gestión de tokens JWT")
public class OAuth2Controller {

    private final GoogleTokenVerifierService googleTokenVerifier;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public OAuth2Controller(GoogleTokenVerifierService googleTokenVerifier,
                            UserRepository userRepository,
                            CustomUserDetailsService userDetailsService,
                            JwtService jwtService,
                            PasswordEncoder passwordEncoder) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.userRepository      = userRepository;
        this.userDetailsService  = userDetailsService;
        this.jwtService          = jwtService;
        this.passwordEncoder     = passwordEncoder;
    }

    @Operation(
        summary = "Login con Google OAuth2",
        description = "Recibe el ID Token emitido por Google Sign-In, lo verifica contra Google, " +
                      "crea el usuario si no existe (rol JUGADOR por defecto) y retorna un JWT interno válido por 1 hora."
    )
    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@Valid @RequestBody GoogleAuthRequest request) {
        log.info("POST /api/v1/auth/google - verificando ID Token de Google");

        Map<String, String> claims;
        try {
            claims = googleTokenVerifier.verify(request.getIdToken());
        } catch (IllegalArgumentException e) {
            log.warn("Token de Google rechazado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new GenericResponse("Error", e.getMessage()));
        }

        String email = claims.get("email");
        String name  = claims.getOrDefault("name", email);

        if (email == null || email.isBlank()) {
            log.warn("El token de Google no contiene un email válido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericResponse("Error", "El token de Google no contiene un email válido."));
        }

        // Encuentra o crea el usuario en la BD
        userRepository.findByEmail(email).orElseGet(() -> {
            log.info("Usuario de Google no encontrado en BD, creando nuevo - email: {}", email);
            RelativePlayer newUser = new RelativePlayer();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setRole("JUGADOR");
            newUser.setAvailable(true);
            // Contraseña aleatoria: el usuario solo podrá autenticarse vía Google
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            return userRepository.save(newUser);
        });

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(userDetails);

        log.info("Login con Google exitoso - email: {}", email);
        return ResponseEntity.ok(new AuthResponse(token, email));
    }
}
