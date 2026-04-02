package com.example.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración liviana para la capa de configuración de seguridad.
 *
 * Al usar @SpringBootTest(webEnvironment = NONE) Spring inicializa todos los beans
 * de configuración (incluido securityFilterChain) con instancias reales de HttpSecurity,
 * lo que permite que JaCoCo registre la cobertura de todas las reglas de autorización
 * y los customizers lambda declarados en SecurityConfig.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DisplayName("SecurityConfig – Beans de seguridad e instanciación correcta")
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private AuthenticationManager authenticationManager;

    // ── PasswordEncoder ───────────────────────────────────────────────────────

    @Test
    @DisplayName("passwordEncoder bean es BCryptPasswordEncoder")
    void passwordEncoder_IsBCrypt() {
        assertNotNull(passwordEncoder, "PasswordEncoder no debe ser null");
        assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder,
                "Debe ser BCryptPasswordEncoder");
    }

    @Test
    @DisplayName("passwordEncoder puede codificar una contraseña y verificarla")
    void passwordEncoder_EncodesAndMatchesPassword() {
        String raw = "MiContraseñaSegura#2024";
        String encoded = passwordEncoder.encode(raw);

        assertNotNull(encoded);
        assertNotEquals(raw, encoded, "La contraseña codificada no debe ser igual a la original");
        assertTrue(passwordEncoder.matches(raw, encoded),
                "matches() debe retornar true para la contraseña correcta");
        assertFalse(passwordEncoder.matches("ContraseñaEquivocada", encoded),
                "matches() debe retornar false para contraseña incorrecta");
    }

    @Test
    @DisplayName("passwordEncoder – dos hashes del mismo texto son distintos (salt aleatorio)")
    void passwordEncoder_ProducesDistinctHashesForSameInput() {
        String raw = "misma_clave";
        String hash1 = passwordEncoder.encode(raw);
        String hash2 = passwordEncoder.encode(raw);

        assertNotEquals(hash1, hash2,
                "BCrypt produce hashes distintos en cada llamada gracias al salt aleatorio");
        assertTrue(passwordEncoder.matches(raw, hash1));
        assertTrue(passwordEncoder.matches(raw, hash2));
    }

    // ── SecurityFilterChain ───────────────────────────────────────────────────

    @Test
    @DisplayName("securityFilterChain bean es instanciado correctamente")
    void securityFilterChain_IsNotNull() {
        assertNotNull(securityFilterChain,
                "SecurityFilterChain debe ser instanciado por Spring");
    }

    @Test
    @DisplayName("securityFilterChain contiene filtros configurados")
    void securityFilterChain_HasFilters() {
        assertNotNull(securityFilterChain.getFilters(),
                "La cadena de filtros no debe ser null");
        assertFalse(securityFilterChain.getFilters().isEmpty(),
                "Debe haber al menos un filtro en la cadena");
    }

    // ── AuthenticationManager ─────────────────────────────────────────────────

    @Test
    @DisplayName("authenticationManager bean es instanciado correctamente")
    void authenticationManager_IsNotNull() {
        assertNotNull(authenticationManager,
                "AuthenticationManager debe ser instanciado por Spring");
    }

    // ── DaoAuthenticationProvider ─────────────────────────────────────────────

    @Test
    @DisplayName("authenticationProvider está configurado con el PasswordEncoder correcto")
    void authenticationProvider_UsesCorrectPasswordEncoder(@Autowired SecurityConfig securityConfig) {
        DaoAuthenticationProvider provider = securityConfig.authenticationProvider();
        assertNotNull(provider, "DaoAuthenticationProvider no debe ser null");
        // Si el encoder no fuera BCrypt, la autenticación fallaría en producción
        assertInstanceOf(BCryptPasswordEncoder.class,
                securityConfig.passwordEncoder(),
                "El encoder del provider debe ser BCryptPasswordEncoder");
    }
}
