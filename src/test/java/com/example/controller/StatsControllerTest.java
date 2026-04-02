package com.example.controller;

import com.example.controller.dto.response.StandingDTO;
import com.example.core.model.PlayerStats;
import com.example.core.service.StatsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("StatsController – /api/v1/stats")
class StatsControllerTest {

    private StatsService statsService;
    private StatsController statsController;

    @BeforeEach
    void setUp() {
        statsService = mock(StatsService.class);
        statsController = new StatsController(statsService);
    }

    // ── getTopScorers (global) ────────────────────────────────────────────────

    @Test
    @DisplayName("getTopScorers → 200 OK con lista de goleadores")
    void getTopScorers_Returns200() {
        PlayerStats ps1 = new PlayerStats(1L, "Carlos", "Sistemas FC", 5, 1, 0, 3);
        PlayerStats ps2 = new PlayerStats(2L, "Maria", "IA FC", 3, 0, 0, 2);
        when(statsService.getTopScorers()).thenReturn(List.of(ps1, ps2));

        ResponseEntity<List<PlayerStats>> response = statsController.getTopScorers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("getTopScorers → lista vacía cuando no hay goles registrados")
    void getTopScorers_EmptyList() {
        when(statsService.getTopScorers()).thenReturn(List.of());

        ResponseEntity<List<PlayerStats>> response = statsController.getTopScorers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ── getTopScorersByTournament ─────────────────────────────────────────────

    @Test
    @DisplayName("getTopScorersByTournament – torneo con goleadores → 200 OK")
    void getTopScorersByTournament_Returns200() {
        PlayerStats ps = new PlayerStats(1L, "Luis", "Torneo FC", 7, 2, 0, 4);
        when(statsService.getTopScorersByTournament(1L)).thenReturn(List.of(ps));

        ResponseEntity<List<PlayerStats>> response = statsController.getTopScorersByTournament(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Luis", response.getBody().get(0).getPlayerName());
    }

    @Test
    @DisplayName("getTopScorersByTournament – torneo sin goles → lista vacía 200 OK")
    void getTopScorersByTournament_Empty() {
        when(statsService.getTopScorersByTournament(2L)).thenReturn(List.of());

        ResponseEntity<List<PlayerStats>> response = statsController.getTopScorersByTournament(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ── getPlayerStats ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getPlayerStats – jugador encontrado → 200 OK con estadísticas")
    void getPlayerStats_Found_Returns200() {
        PlayerStats stats = new PlayerStats(1L, "Ana", "Equipo X", 3, 1, 0, 2);
        when(statsService.getPlayerStats(1L)).thenReturn(stats);

        ResponseEntity<PlayerStats> response = statsController.getPlayerStats(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(3, response.getBody().getGoals());
        assertEquals(1, response.getBody().getYellowCards());
    }

    @Test
    @DisplayName("getPlayerStats – jugador no encontrado → 404 NOT FOUND")
    void getPlayerStats_NotFound_Returns404() {
        when(statsService.getPlayerStats(99L)).thenThrow(new RuntimeException("Jugador no encontrado"));

        ResponseEntity<PlayerStats> response = statsController.getPlayerStats(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── getTeamStats ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTeamStats – equipo encontrado → 200 OK con StandingDTO")
    void getTeamStats_Found_Returns200() {
        StandingDTO dto = new StandingDTO();
        dto.setTeamId(1L);
        dto.setTeamName("Sistemas FC");
        dto.setPoints(9);
        dto.setMatchesPlayed(3);
        dto.setMatchesWon(3);
        dto.setMatchesDrawn(0);
        dto.setMatchesLost(0);
        when(statsService.getTeamStats(1L)).thenReturn(dto);

        ResponseEntity<StandingDTO> response = statsController.getTeamStats(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(9, response.getBody().getPoints());
        assertEquals("Sistemas FC", response.getBody().getTeamName());
    }

    @Test
    @DisplayName("getTeamStats – equipo no encontrado → 404 NOT FOUND")
    void getTeamStats_NotFound_Returns404() {
        when(statsService.getTeamStats(99L)).thenThrow(new RuntimeException("Equipo no encontrado"));

        ResponseEntity<StandingDTO> response = statsController.getTeamStats(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
