package com.example.core.service;

import com.example.controller.dto.response.StandingDTO;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.*;
import com.example.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("StatsService – Estadísticas generales")
class StatsServiceTest {

    private MatchEventRepository matchEventRepository;
    private MatchRepository matchRepository;
    private TeamRepository teamRepository;
    private PlayerRepository playerRepository;
    private StatsService statsService;

    @BeforeEach
    void setUp() {
        matchEventRepository = mock(MatchEventRepository.class);
        matchRepository      = mock(MatchRepository.class);
        teamRepository       = mock(TeamRepository.class);
        playerRepository     = mock(PlayerRepository.class);
        statsService = new StatsService(matchEventRepository, matchRepository, teamRepository, playerRepository);
    }

    // ── getTopScorers ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTopScorers – sin eventos → lista vacía")
    void getTopScorers_NoEvents_Empty() {
        when(matchEventRepository.findAll()).thenReturn(List.of());
        assertTrue(statsService.getTopScorers().isEmpty());
    }

    @Test
    @DisplayName("getTopScorers – mezcla de GOL, AMARILLA, ROJA → acumula correctamente")
    void getTopScorers_MixedEvents_Aggregated() {
        MatchEvent gol1  = event(1L, "Jugador A", "GOL");
        MatchEvent gol2  = event(1L, "Jugador A", "GOL");
        MatchEvent ama   = event(1L, "Jugador A", "AMARILLA");
        MatchEvent gol3  = event(2L, "Jugador B", "GOL");

        when(matchEventRepository.findAll()).thenReturn(List.of(gol1, gol2, ama, gol3));

        List<PlayerStats> result = statsService.getTopScorers();

        assertEquals(2, result.size());
        PlayerStats a = result.get(0); // mayor goleador primero
        assertEquals(2, a.getGoals());
        assertEquals(1, a.getYellowCards());
    }

    @Test
    @DisplayName("getTopScorers – eventos ROJA acumulados correctamente")
    void getTopScorers_RedCards_Accumulated() {
        MatchEvent roja = event(3L, "Jugador C", "ROJA");
        when(matchEventRepository.findAll()).thenReturn(List.of(roja));

        List<PlayerStats> result = statsService.getTopScorers();
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getRedCards());
    }

    // ── getTopScorersByTournament ─────────────────────────────────────────────

    @Test
    @DisplayName("getTopScorersByTournament – solo filtra GOL, ignora AMARILLA/ROJA")
    void getTopScorersByTournament_FiltersGolOnly() {
        MatchEvent gol  = event(1L, "Jugador A", "GOL");
        MatchEvent ama  = event(2L, "Jugador B", "AMARILLA");
        MatchEvent roja = event(3L, "Jugador C", "ROJA");

        when(matchEventRepository.findAll()).thenReturn(List.of(gol, ama, roja));

        List<PlayerStats> result = statsService.getTopScorersByTournament(1L);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getPlayerId());
    }

    @Test
    @DisplayName("getTopScorersByTournament – case-insensitive 'gol' → también cuenta")
    void getTopScorersByTournament_CaseInsensitiveGol() {
        MatchEvent gol = event(1L, "Jugador A", "gol");
        when(matchEventRepository.findAll()).thenReturn(List.of(gol));

        List<PlayerStats> result = statsService.getTopScorersByTournament(99L);
        assertEquals(1, result.size());
    }

    // ── getPlayerStats ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getPlayerStats – jugador no encontrado → ResourceNotFoundException")
    void getPlayerStats_NotFound() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> statsService.getPlayerStats(99L));
    }

    @Test
    @DisplayName("getPlayerStats – jugador sin eventos → todo en cero")
    void getPlayerStats_NoEvents_ZeroStats() {
        StudentPlayer p = new StudentPlayer();
        p.setId(1L); p.setFullName("Juan Jugador");
        when(playerRepository.findById(1L)).thenReturn(Optional.of(p));
        when(matchEventRepository.findAll()).thenReturn(List.of());

        PlayerStats stats = statsService.getPlayerStats(1L);

        assertEquals(0, stats.getGoals());
        assertEquals(0, stats.getYellowCards());
        assertEquals(0, stats.getRedCards());
    }

    @Test
    @DisplayName("getPlayerStats – suma GOL, AMARILLA y ROJA correctamente")
    void getPlayerStats_WithEvents_AllTypes() {
        StudentPlayer p = new StudentPlayer();
        p.setId(1L); p.setFullName("Carlos Golea");
        when(playerRepository.findById(1L)).thenReturn(Optional.of(p));

        MatchEvent g1  = event(1L, "Carlos Golea", "GOL");
        MatchEvent g2  = event(1L, "Carlos Golea", "GOL");
        MatchEvent ama = event(1L, "Carlos Golea", "AMARILLA");
        MatchEvent roj = event(1L, "Carlos Golea", "ROJA");
        MatchEvent other = event(2L, "Otro", "GOL"); // de otro jugador

        when(matchEventRepository.findAll()).thenReturn(List.of(g1, g2, ama, roj, other));

        PlayerStats stats = statsService.getPlayerStats(1L);

        assertEquals(2, stats.getGoals());
        assertEquals(1, stats.getYellowCards());
        assertEquals(1, stats.getRedCards());
    }

    // ── getTeamStats ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTeamStats – equipo no encontrado → ResourceNotFoundException")
    void getTeamStats_TeamNotFound() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> statsService.getTeamStats(99L));
    }

    // ── getTournamentStandings ────────────────────────────────────────────────

    @Test
    @DisplayName("getTournamentStandings – sin equipos → lista vacía")
    void getTournamentStandings_NoTeams_Empty() {
        when(teamRepository.findAll()).thenReturn(List.of());
        when(matchRepository.findAll()).thenReturn(List.of());

        assertTrue(statsService.getTournamentStandings(1L).isEmpty());
    }

    @Test
    @DisplayName("getTournamentStandings – ordenado por puntos DESC incluyendo FairPlay")
    void getTournamentStandings_OrderedByPoints() {
        Team t1 = new Team(); t1.setId(1L); t1.setName("Team A"); t1.setPoints(9);
        Team t2 = new Team(); t2.setId(2L); t2.setName("Team B"); t2.setPoints(3);
        when(teamRepository.findAll()).thenReturn(List.of(t2, t1)); // desordenado
        when(matchRepository.findAll()).thenReturn(List.of());

        List<StandingDTO> standings = statsService.getTournamentStandings(1L);

        assertEquals(2, standings.size());
        assertEquals("Team A", standings.get(0).getTeamName()); // primero por puntos
        assertEquals("Team B", standings.get(1).getTeamName());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private MatchEvent event(Long playerId, String playerName, String type) {
        MatchEvent e = new MatchEvent();
        e.setPlayerId(playerId);
        e.setPlayerName(playerName);
        e.setType(type);
        return e;
    }
}
