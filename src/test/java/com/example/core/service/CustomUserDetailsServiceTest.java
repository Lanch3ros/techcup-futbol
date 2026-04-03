package com.example.core.service;

import com.example.core.model.AdminUser;
import com.example.core.model.OrganizerUser;
import com.example.core.model.RefereeUser;
import com.example.core.model.StudentPlayer;
import com.example.repository.UserRepository;
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

    private UserRepository userRepository;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userRepository     = mock(UserRepository.class);
        userDetailsService = new CustomUserDetailsService(userRepository);
    }

    // ── loadUserByUsername ────────────────────────────────────────────────────

    @Test
    @DisplayName("usuario con rol explícito 'CAPITAN' → authority ROLE_CAPITAN")
    void loadUserByUsername_ExplicitRole_Capitan() {
        StudentPlayer u = new StudentPlayer();
        u.setEmail("capitan@mail.com");
        u.setRole("CAPITAN");
        u.setPassword("$hashed");
        when(userRepository.findByEmail("capitan@mail.com")).thenReturn(Optional.of(u));

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
        when(userRepository.findByEmail("ref@mail.com")).thenReturn(Optional.of(u));

        UserDetails result = userDetailsService.loadUserByUsername("ref@mail.com");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ARBITRO")));
    }

    @Test
    @DisplayName("AdminUser sin rol explícito → ROLE_ADMIN por tipo de usuario")
    void loadUserByUsername_AdminType_NoRole_ResolvesAdmin() {
        AdminUser u = new AdminUser();
        u.setEmail("admin@mail.com");
        u.setRole(null);
        u.setPassword("$hashed");
        when(userRepository.findByEmail("admin@mail.com")).thenReturn(Optional.of(u));

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
        when(userRepository.findByEmail("jose@mail.com")).thenReturn(Optional.of(u));

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
        when(userRepository.findByEmail("jose2@mail.com")).thenReturn(Optional.of(u));

        UserDetails result = userDetailsService.loadUserByUsername("jose2@mail.com");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_JUGADOR")));
    }

    @Test
    @DisplayName("OrganizerUser sin rol explícito → ROLE_ORGANIZADOR por tipo")
    void loadUserByUsername_OrganizerType_NoRole_ResolvesOrganizador() {
        OrganizerUser u = new OrganizerUser();
        u.setEmail("org@mail.com");
        u.setRole(null);
        u.setPassword("$hashed");
        when(userRepository.findByEmail("org@mail.com")).thenReturn(Optional.of(u));

        UserDetails result = userDetailsService.loadUserByUsername("org@mail.com");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ORGANIZADOR")));
    }

    @Test
    @DisplayName("RefereeUser sin rol explícito → ROLE_ARBITRO por tipo")
    void loadUserByUsername_RefereeType_NoRole_ResolvesArbitro() {
        RefereeUser u = new RefereeUser();
        u.setEmail("ref2@mail.com");
        u.setRole(null);
        u.setPassword("$hashed");
        when(userRepository.findByEmail("ref2@mail.com")).thenReturn(Optional.of(u));

        UserDetails result = userDetailsService.loadUserByUsername("ref2@mail.com");

        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ARBITRO")));
    }

    @Test
    @DisplayName("usuario no encontrado → UsernameNotFoundException")
    void loadUserByUsername_NotFound_Throws() {
        when(userRepository.findByEmail("noexiste@mail.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("noexiste@mail.com"));
    }
}
