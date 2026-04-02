package com.example.core.service;

import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Player;
import com.example.core.model.StudentPlayer;
import com.example.core.model.User;
import com.example.repository.InvitationRepository;
import com.example.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("PlayerService – Métodos extendidos (búsqueda, actualización, respuesta directa)")
class PlayerServiceExtendedTest {

    private PlayerRepository playerRepository;
    private InvitationRepository invitationRepository;
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerRepository     = mock(PlayerRepository.class);
        invitationRepository = mock(InvitationRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(anyString())).thenReturn("$hashed");
        playerService = new PlayerService(playerRepository, encoder, invitationRepository);
    }

    private StudentPlayer player(long id, boolean available, Long teamId) {
        StudentPlayer p = new StudentPlayer();
        p.setId(id);
        p.setFullName("Jugador " + id);
        p.setPosition("Delantero");
        p.setAvailable(available);
        p.setTeamId(teamId);
        return p;
    }

    // ── getAvailablePlayers ───────────────────────────────────────────────────

    @Test
    @DisplayName("getAvailablePlayers – solo devuelve jugadores disponibles")
    void getAvailablePlayers_FiltersCorrectly() {
        StudentPlayer available = player(1L, true, null);
        StudentPlayer unavailable = player(2L, false, 5L);
        when(playerRepository.findAll()).thenReturn(List.of(available, unavailable));

        List<Player> result = playerService.getAvailablePlayers();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    @DisplayName("getAvailablePlayers – sin jugadores disponibles → lista vacía")
    void getAvailablePlayers_NoneAvailable_Empty() {
        when(playerRepository.findAll()).thenReturn(List.of(player(1L, false, 1L)));
        assertTrue(playerService.getAvailablePlayers().isEmpty());
    }

    // ── searchPlayers ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchPlayers – filtro por posición (case-insensitive)")
    void searchPlayers_ByPosition() {
        StudentPlayer p1 = player(1L, true, null);
        p1.setPosition("Delantero");
        StudentPlayer p2 = player(2L, true, null);
        p2.setPosition("Portero");
        when(playerRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Player> result = playerService.searchPlayers("DELANTERO", null);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    @DisplayName("searchPlayers – filtro por nombre (substring, case-insensitive)")
    void searchPlayers_ByName() {
        StudentPlayer p1 = player(1L, true, null);
        p1.setFullName("Carlos Garcia");
        StudentPlayer p2 = player(2L, true, null);
        p2.setFullName("Maria Lopez");
        when(playerRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Player> result = playerService.searchPlayers(null, "GARCIA");
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    @DisplayName("searchPlayers – ambos filtros nulos → devuelve todos")
    void searchPlayers_NullFilters_ReturnsAll() {
        when(playerRepository.findAll()).thenReturn(List.of(player(1L, true, null), player(2L, false, 1L)));

        List<Player> result = playerService.searchPlayers(null, null);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("searchPlayers – ambos filtros en blanco → devuelve todos")
    void searchPlayers_BlankFilters_ReturnsAll() {
        when(playerRepository.findAll()).thenReturn(List.of(player(1L, true, null)));

        List<Player> result = playerService.searchPlayers("", "");
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchPlayers – posición y nombre combinados")
    void searchPlayers_BothFilters() {
        StudentPlayer p1 = player(1L, true, null);
        p1.setPosition("Mediocampista");
        p1.setFullName("Luis Torres");
        StudentPlayer p2 = player(2L, true, null);
        p2.setPosition("Mediocampista");
        p2.setFullName("Ana Ruiz");
        when(playerRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Player> result = playerService.searchPlayers("mediocampista", "torres");
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    // ── updatePosition ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePosition – jugador encontrado → actualiza y persiste")
    void updatePosition_Success() {
        StudentPlayer p = player(1L, true, null);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(p));
        when(playerRepository.save(any())).thenReturn(p);

        assertDoesNotThrow(() -> playerService.updatePosition(1L, "Portero"));
        assertEquals("Portero", p.getPosition());
        verify(playerRepository).save(p);
    }

    @Test
    @DisplayName("updatePosition – jugador no encontrado → ResourceNotFoundException")
    void updatePosition_NotFound() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> playerService.updatePosition(99L, "Portero"));
    }

    // ── updateAvailability ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateAvailability – marca disponible → persiste")
    void updateAvailability_SetTrue_Success() {
        StudentPlayer p = player(1L, false, 1L);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(p));
        when(playerRepository.save(any())).thenReturn(p);

        assertDoesNotThrow(() -> playerService.updateAvailability(1L, true));
        assertTrue(p.isAvailable());
    }

    @Test
    @DisplayName("updateAvailability – jugador no encontrado → ResourceNotFoundException")
    void updateAvailability_NotFound() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> playerService.updateAvailability(99L, false));
    }

    // ── updateJerseyNumber ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateJerseyNumber – actualiza y persiste dorsal")
    void updateJerseyNumber_Success() {
        StudentPlayer p = player(1L, true, null);
        p.setJerseyNumber(10);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(p));
        when(playerRepository.save(any())).thenReturn(p);

        assertDoesNotThrow(() -> playerService.updateJerseyNumber(1L, 9));
        assertEquals(9, p.getJerseyNumber());
    }

    @Test
    @DisplayName("updateJerseyNumber – jugador no encontrado → ResourceNotFoundException")
    void updateJerseyNumber_NotFound() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> playerService.updateJerseyNumber(99L, 7));
    }

    // ── respondToInvitation ───────────────────────────────────────────────────

    @Test
    @DisplayName("respondToInvitation – ACCEPT → jugador vinculado al equipo")
    void respondToInvitation_Accept() {
        StudentPlayer p = player(1L, true, null);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(p));
        when(playerRepository.save(any())).thenReturn(p);

        assertDoesNotThrow(() -> playerService.respondToInvitation(1L, 5L, "ACCEPT"));
        assertFalse(p.isAvailable());
        assertEquals(5L, p.getTeamId());
    }

    @Test
    @DisplayName("respondToInvitation – REJECT → jugador no vinculado")
    void respondToInvitation_Reject() {
        StudentPlayer p = player(1L, true, null);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(p));
        when(playerRepository.save(any())).thenReturn(p);

        assertDoesNotThrow(() -> playerService.respondToInvitation(1L, 5L, "REJECT"));
        assertNull(p.getTeamId());
    }

    @Test
    @DisplayName("respondToInvitation – jugador no encontrado → ResourceNotFoundException")
    void respondToInvitation_NotFound() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> playerService.respondToInvitation(99L, 1L, "ACCEPT"));
    }

    @Test
    @DisplayName("respondToInvitation – acción desconocida → no vincula ni rechaza (else branch)")
    void respondToInvitation_UnknownAction_NoOp() {
        StudentPlayer p = player(1L, true, null);
        when(playerRepository.findById(1L)).thenReturn(Optional.of(p));
        when(playerRepository.save(any())).thenReturn(p);

        assertDoesNotThrow(() -> playerService.respondToInvitation(1L, 5L, "IGNORE"));
        assertNull(p.getTeamId());   // no fue vinculado
        assertTrue(p.isAvailable()); // no fue modificado
    }
}
