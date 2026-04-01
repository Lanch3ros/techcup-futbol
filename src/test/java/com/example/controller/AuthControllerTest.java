package com.example.controller;

import com.example.controller.dto.request.LoginRequest;
import com.example.controller.dto.response.AuthResponse;
import com.example.controller.dto.response.GenericResponse;
import com.example.core.service.CustomUserDetailsService;
import com.example.core.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AuthController – POST /api/v1/auth/login")
class AuthControllerTest {

    private AuthenticationManager authenticationManager;
    private CustomUserDetailsService userDetailsService;
    private JwtService jwtService;
    private AuthController authController;

    private LoginRequest validRequest;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        userDetailsService    = mock(CustomUserDetailsService.class);
        jwtService            = mock(JwtService.class);
        authController = new AuthController(authenticationManager, userDetailsService, jwtService);

        validRequest = new LoginRequest();
        validRequest.setEmail("jose@mail.escuelaing.edu.co");
        validRequest.setPassword("password123");

        userDetails = User.withUsername("jose@mail.escuelaing.edu.co")
                .password("$2a$10$hashed")
                .authorities(new SimpleGrantedAuthority("ROLE_JUGADOR"))
                .build();
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Credenciales válidas → 200 OK con token en el cuerpo")
    void login_ValidCredentials_Returns200WithToken() {
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        when(userDetailsService.loadUserByUsername("jose@mail.escuelaing.edu.co")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("eyJhbGciOiJIUzI1NiJ9.mock.token");

        ResponseEntity<?> response = authController.login(validRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(AuthResponse.class, response.getBody());

        AuthResponse body = (AuthResponse) response.getBody();
        assertEquals("eyJhbGciOiJIUzI1NiJ9.mock.token", body.getToken());
        assertEquals("jose@mail.escuelaing.edu.co", body.getEmail());
        assertEquals("Bearer", body.getType());
    }

    @Test
    @DisplayName("Credenciales válidas → se llama a generateToken con los UserDetails correctos")
    void login_ValidCredentials_CallsGenerateToken() {
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(userDetails, null, List.of()));
        when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("token");

        authController.login(validRequest);

        verify(jwtService).generateToken(userDetails);
    }

    // ── Credenciales inválidas ────────────────────────────────────────────────

    @Test
    @DisplayName("Contraseña incorrecta → 401 Unauthorized")
    void login_BadCredentials_Returns401() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<?> response = authController.login(validRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Contraseña incorrecta → cuerpo contiene mensaje de error")
    void login_BadCredentials_ErrorBodyMessage() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<?> response = authController.login(validRequest);

        assertInstanceOf(GenericResponse.class, response.getBody());
        GenericResponse body = (GenericResponse) response.getBody();
        assertEquals("Credenciales inválidas", body.getData());
    }

    @Test
    @DisplayName("Contraseña incorrecta → NO se genera ningún token")
    void login_BadCredentials_NoTokenGenerated() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        authController.login(validRequest);

        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Error de autenticación genérico → 401 Unauthorized")
    void login_GenericAuthError_Returns401() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("DB connection failed"));

        ResponseEntity<?> response = authController.login(validRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ── Flujo de autenticación ────────────────────────────────────────────────

    @Test
    @DisplayName("El AuthenticationManager recibe exactamente el email y password del request")
    void login_PassesCorrectCredentialsToAuthManager() {
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(userDetails, null, List.of()));
        when(userDetailsService.loadUserByUsername(any())).thenReturn(userDetails);
        when(jwtService.generateToken(any())).thenReturn("token");

        authController.login(validRequest);

        verify(authenticationManager).authenticate(
                argThat(auth -> auth instanceof UsernamePasswordAuthenticationToken
                        && "jose@mail.escuelaing.edu.co".equals(auth.getPrincipal())
                        && "password123".equals(auth.getCredentials()))
        );
    }

    @Test
    @DisplayName("Login exitoso → userDetailsService es llamado con el email del request")
    void login_Success_LoadsUserByEmail() {
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(userDetails, null, List.of()));
        when(userDetailsService.loadUserByUsername("jose@mail.escuelaing.edu.co")).thenReturn(userDetails);
        when(jwtService.generateToken(any())).thenReturn("token");

        authController.login(validRequest);

        verify(userDetailsService).loadUserByUsername("jose@mail.escuelaing.edu.co");
    }
}
