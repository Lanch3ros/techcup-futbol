package com.example.core.service;

import com.example.core.model.AdminPlayer;
import com.example.core.model.StudentPlayer;
import com.example.core.model.User;
import com.example.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("CustomUserDetailsService – Carga de usuario y resolución de roles")
class CustomUserDetailsServiceTest {

    private PlayerRepository playerRepository;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        playerRepository   = mock(PlayerRepository.class);
        userDetailsService = new CustomUserDetailsService(playerRepository);
    }

    // ── loadUserByUsername ────────────────────────────────────────────────────

    @Test
    @DisplayName("usuario con rol explícito 'CAPITAN' → authority ROLE_CAPITAN")
    void loadUserByUsername_ExplicitRole_Capitan() {
        StudentPlayer u = new StudentPlayer();
        u.setEmail("capitan@mail.com");
        u.setRole("CAPITAN");
        u.setPassword("$hashed");
        when(playerRepository.findByEmail("capitan@mail.com")).thenReturn(Optional.of(u));

        UserDetails result = userDetailsService.loadUserByUsername("capitan@mail.com");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CAPITAN")));
        assertEquals("capitan@mail.com", result.getUsername());
    }

    @Test
    @DisplayName("usuario con rol explícito en minúscula → normalizado a mayúscula")
    void loadUserByUsername_ExplicitRole_LowerCase_Normalized() {
        StudentPlayer u = new StudentPlayer();
        u.setEmail("ref@mail.com");
        u.setRole("arbitro");
        u.setPassword("$hashed");
        when(playerRepository.findByEmail("ref@mail.com")).thenReturn(Optional.of(u));

        UserDetails result = userDetailsService.loadUserByUsername("ref@mail.com");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ARBITRO")));
    }

    @Test
    @DisplayName("AdminPlayer sin rol explícito → ROLE_ADMIN por tipo de usuario")
    void loadUserByUsername_AdminType_NoRole_ResolvesAdmin() {
        AdminPlayer u = new AdminPlayer();
        u.setEmail("admin@mail.com");
        u.setRole(null);
        u.setPassword("$hashed");
        when(playerRepository.findByEmail("admin@mail.com")).thenReturn(Optional.of(u));

        UserDetails result = userDetailsService.loadUserByUsername("admin@mail.com");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("StudentPlayer sin rol explícito → ROLE_JUGADOR por defecto")
    void loadUserByUsername_StudentType_NoRole_ResolvesJugador() {
        StudentPlayer u = new StudentPlayer();
        u.setEmail("jose@mail.com");
        u.setRole(null);
        u.setPassword("$hashed");
        when(playerRepository.findByEmail("jose@mail.com")).thenReturn(Optional.of(u));

        UserDetails result = userDetailsService.loadUserByUsername("jose@mail.com");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_JUGADOR")));
    }

    @Test
    @DisplayName("usuario con rol en blanco (\"\") → trata como sin rol → ROLE_JUGADOR")
    void loadUserByUsername_BlankRole_ResolvesJugador() {
        StudentPlayer u = new StudentPlayer();
        u.setEmail("jose2@mail.com");
        u.setRole("   ");
        u.setPassword("$hashed");
        when(playerRepository.findByEmail("jose2@mail.com")).thenReturn(Optional.of(u));

        UserDetails result = userDetailsService.loadUserByUsername("jose2@mail.com");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_JUGADOR")));
    }

    @Test
    @DisplayName("usuario no encontrado → UsernameNotFoundException")
    void loadUserByUsername_NotFound_Throws() {
        when(playerRepository.findByEmail("noexiste@mail.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("noexiste@mail.com"));
    }
}
