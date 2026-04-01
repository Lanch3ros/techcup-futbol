package com.example.controller;

import com.example.controller.dto.request.LoginRequest;
import com.example.controller.dto.response.AuthResponse;
import com.example.controller.dto.response.GenericResponse;
import com.example.core.service.CustomUserDetailsService;
import com.example.core.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoint de login y gestión de tokens JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService    = userDetailsService;
        this.jwtService            = jwtService;
    }

    @Operation(summary = "Iniciar sesión y obtener token JWT",
               description = "Valida las credenciales contra la base de datos y devuelve un token JWT " +
                             "válido por 24 horas con el rol del usuario incluido como claim.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login - email: {}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            log.warn("Credenciales inválidas para email: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new GenericResponse("Error", "Credenciales inválidas"));
        } catch (Exception e) {
            log.error("Error de autenticación para email: {} - {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new GenericResponse("Error", "Error de autenticación: " + e.getMessage()));
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateToken(userDetails);

        log.info("Login exitoso - email: {}", request.getEmail());
        return ResponseEntity.ok(new AuthResponse(token, request.getEmail()));
    }
}
