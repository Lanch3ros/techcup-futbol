package com.example.controller;

import com.example.controller.dto.request.GoogleAuthRequest;
import com.example.controller.dto.response.AuthResponse;
import com.example.controller.dto.response.GenericResponse;
import com.example.core.model.RelativePlayer;
import com.example.core.model.User;
import com.example.core.service.CustomUserDetailsService;
import com.example.core.service.GoogleTokenVerifierService;
import com.example.core.service.JwtService;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("OAuth2Controller – POST /api/v1/auth/google")
class OAuth2ControllerTest {

    private GoogleTokenVerifierService googleTokenVerifier;
    private UserRepository userRepository;
    private CustomUserDetailsService userDetailsService;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private OAuth2Controller controller;

    private static final String VALID_TOKEN  = "valid-google-id-token";
    private static final String TEST_EMAIL   = "jugador@gmail.com";
    private static final String TEST_NAME    = "Jugador Google";
    private static final String INTERNAL_JWT = "eyJhbGciOiJIUzI1NiJ9.test";

    @BeforeEach
    void setUp() {
        googleTokenVerifier = mock(GoogleTokenVerifierService.class);
        userRepository      = mock(UserRepository.class);
        userDetailsService  = mock(CustomUserDetailsService.class);
        jwtService          = mock(JwtService.class);
        passwordEncoder     = mock(PasswordEncoder.class);

        controller = new OAuth2Controller(
                googleTokenVerifier, userRepository,
                userDetailsService, jwtService, passwordEncoder);

        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashed");
    }

    private GoogleAuthRequest buildRequest(String idToken) {
        GoogleAuthRequest req = new GoogleAuthRequest();
        req.setIdToken(idToken);
        return req;
    }

    private UserDetails stubUserDetails(String email) {
        return new org.springframework.security.core.userdetails.User(
                email, "$2a$10$hashed",
                List.of(new SimpleGrantedAuthority("ROLE_JUGADOR")));
    }

    // ── Token inválido ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Token de Google inválido → 401 Unauthorized")
    void loginWithGoogle_InvalidToken_Returns401() {
        when(googleTokenVerifier.verify(anyString()))
                .thenThrow(new IllegalArgumentException("Token de Google inválido o expirado."));

        ResponseEntity<?> response = controller.loginWithGoogle(buildRequest("bad-token"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertInstanceOf(GenericResponse.class, response.getBody());
        assertEquals("Error", ((GenericResponse) response.getBody()).getMessage());
        assertEquals("Token de Google inválido o expirado.",
                ((GenericResponse) response.getBody()).getData());
    }

    // ── Email ausente en claims ───────────────────────────────────────────────

    @Test
    @DisplayName("Token válido pero sin email en claims → 400 Bad Request")
    void loginWithGoogle_NoEmailInClaims_Returns400() {
        when(googleTokenVerifier.verify(anyString())).thenReturn(Map.of("sub", "123456"));

        ResponseEntity<?> response = controller.loginWithGoogle(buildRequest(VALID_TOKEN));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(GenericResponse.class, response.getBody());
    }

    // ── Usuario nuevo (primer login) ──────────────────────────────────────────

    @Test
    @DisplayName("Primer login con Google → crea usuario RelativePlayer con rol JUGADOR y retorna JWT")
    void loginWithGoogle_NewUser_CreatesAndReturnsJwt() {
        when(googleTokenVerifier.verify(VALID_TOKEN))
                .thenReturn(Map.of("email", TEST_EMAIL, "name", TEST_NAME));
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        RelativePlayer savedUser = new RelativePlayer();
        savedUser.setId(1L);
        savedUser.setEmail(TEST_EMAIL);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDetails ud = stubUserDetails(TEST_EMAIL);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(ud);
        when(jwtService.generateToken(ud)).thenReturn(INTERNAL_JWT);

        ResponseEntity<?> response = controller.loginWithGoogle(buildRequest(VALID_TOKEN));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body);
        assertEquals(INTERNAL_JWT, body.getToken());
        assertEquals(TEST_EMAIL, body.getEmail());

        // Verifica que se persistió el nuevo usuario
        verify(userRepository).save(argThat(u ->
                TEST_EMAIL.equals(u.getEmail()) &&
                TEST_NAME.equals(u.getFullName()) &&
                "JUGADOR".equals(u.getRole())));
    }

    // ── Usuario existente (logins posteriores) ────────────────────────────────

    @Test
    @DisplayName("Login repetido con Google → no crea usuario y retorna JWT del existente")
    void loginWithGoogle_ExistingUser_DoesNotCreateAndReturnsJwt() {
        RelativePlayer existing = new RelativePlayer();
        existing.setId(5L);
        existing.setEmail(TEST_EMAIL);
        existing.setRole("JUGADOR");

        when(googleTokenVerifier.verify(VALID_TOKEN))
                .thenReturn(Map.of("email", TEST_EMAIL, "name", TEST_NAME));
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existing));

        UserDetails ud = stubUserDetails(TEST_EMAIL);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(ud);
        when(jwtService.generateToken(ud)).thenReturn(INTERNAL_JWT);

        ResponseEntity<?> response = controller.loginWithGoogle(buildRequest(VALID_TOKEN));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(INTERNAL_JWT, ((AuthResponse) response.getBody()).getToken());

        // No debe llamar a save (usuario ya existe)
        verify(userRepository, never()).save(any());
    }

    // ── Nombre ausente en claims ──────────────────────────────────────────────

    @Test
    @DisplayName("Claims sin 'name' → usa el email como fullName")
    void loginWithGoogle_NoNameInClaims_UsesEmailAsName() {
        when(googleTokenVerifier.verify(VALID_TOKEN))
                .thenReturn(Map.of("email", TEST_EMAIL));   // sin "name"
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        RelativePlayer saved = new RelativePlayer();
        saved.setEmail(TEST_EMAIL);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserDetails ud = stubUserDetails(TEST_EMAIL);
        when(userDetailsService.loadUserByUsername(TEST_EMAIL)).thenReturn(ud);
        when(jwtService.generateToken(ud)).thenReturn(INTERNAL_JWT);

        controller.loginWithGoogle(buildRequest(VALID_TOKEN));

        // fullName debe ser el email cuando 'name' no viene en claims
        verify(userRepository).save(argThat(u -> TEST_EMAIL.equals(u.getFullName())));
    }
}
