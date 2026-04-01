package com.example.core.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService – Generación y validación de tokens")
class JwtServiceTest {

    // Misma clave que el default de application.yaml (≥256 bits en Base64)
    private static final String SECRET = "dGVjaGN1cC1mdXRib2wtc2VjcmV0LWtleS1mb3ItZGV2ZWxvcG1lbnQ=";
    private static final long   EXPIRY = 86_400_000L; // 24 h

    private JwtService jwtService;
    private UserDetails adminUser;
    private UserDetails jugadorUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey",    SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", EXPIRY);

        adminUser   = buildUser("admin@escuelaing.edu.co",   "ROLE_ADMIN");
        jugadorUser = buildUser("jose@mail.escuelaing.edu.co", "ROLE_JUGADOR");
    }

    private UserDetails buildUser(String email, String role) {
        return User.withUsername(email)
                .password("$2a$10$hashed")
                .authorities(new SimpleGrantedAuthority(role))
                .build();
    }

    // ── generateToken ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateToken produce un token no nulo y no vacío")
    void generateToken_NotBlank() {
        String token = jwtService.generateToken(adminUser);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    @DisplayName("generateToken incluye el subject correcto (email del usuario)")
    void generateToken_SubjectIsEmail() {
        String token = jwtService.generateToken(adminUser);
        String username = jwtService.extractUsername(token);
        assertEquals("admin@escuelaing.edu.co", username);
    }

    @Test
    @DisplayName("generateToken incluye el claim 'roles' con la authority del usuario")
    void generateToken_ContainsRolesClaim() {
        String token = jwtService.generateToken(adminUser);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        assertNotNull(roles, "El claim 'roles' no debe ser null");
        assertTrue(roles.contains("ROLE_ADMIN"), "Debe contener 'ROLE_ADMIN'");
    }

    @Test
    @DisplayName("generateToken para JUGADOR incluye 'ROLE_JUGADOR' en el claim roles")
    void generateToken_JugadorRole() {
        String token = jwtService.generateToken(jugadorUser);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        assertTrue(roles.contains("ROLE_JUGADOR"));
    }

    @Test
    @DisplayName("generateToken fija expiración en ~24 horas")
    void generateToken_ExpirationIsApprox24Hours() {
        long before = System.currentTimeMillis();
        String token = jwtService.generateToken(adminUser);
        long after  = System.currentTimeMillis();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        long expMs = claims.getExpiration().getTime();
        assertTrue(expMs >= before + EXPIRY - 1000, "La expiración debe ser al menos ahora + 24h");
        assertTrue(expMs <= after  + EXPIRY + 1000, "La expiración no debe superar ahora + 24h + margen");
    }

    // ── extractUsername ───────────────────────────────────────────────────────

    @Test
    @DisplayName("extractUsername devuelve el email correcto")
    void extractUsername_ReturnsCorrectEmail() {
        String token = jwtService.generateToken(jugadorUser);
        assertEquals("jose@mail.escuelaing.edu.co", jwtService.extractUsername(token));
    }

    // ── isTokenValid ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("isTokenValid → true para token recién generado con el mismo usuario")
    void isTokenValid_FreshToken_ReturnsTrue() {
        String token = jwtService.generateToken(adminUser);
        assertTrue(jwtService.isTokenValid(token, adminUser));
    }

    @Test
    @DisplayName("isTokenValid → false cuando el username no coincide")
    void isTokenValid_WrongUser_ReturnsFalse() {
        String token = jwtService.generateToken(adminUser);
        assertFalse(jwtService.isTokenValid(token, jugadorUser));
    }

    @Test
    @DisplayName("isTokenValid → false para token expirado")
    void isTokenValid_ExpiredToken_ReturnsFalse() {
        // Forzar expiración en el pasado
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1000L);
        String expiredToken = jwtService.generateToken(adminUser);

        // Restaurar expiración normal para la validación
        ReflectionTestUtils.setField(jwtService, "expirationMs", EXPIRY);

        assertFalse(jwtService.isTokenValid(expiredToken, adminUser));
    }

    @Test
    @DisplayName("Token de usuarios distintos no se valida entre sí")
    void isTokenValid_CrossUserToken_ReturnsFalse() {
        String adminToken   = jwtService.generateToken(adminUser);
        String jugadorToken = jwtService.generateToken(jugadorUser);

        assertFalse(jwtService.isTokenValid(adminToken,   jugadorUser));
        assertFalse(jwtService.isTokenValid(jugadorToken, adminUser));
    }
}
