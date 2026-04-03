package com.example.core.service;

import com.example.controller.dto.response.StandingDTO;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.*;
import com.example.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("StatsService – Estadísticas generales")
class StatsServiceTest {

    private MatchEventRepository matchEventRepository;
    private MatchRepository matchRepository;
    private TeamRepository teamRepository;
    private UserRepository userRepository;
    private TournamentRepository tournamentRepository;
    private StatsService statsService;

    @BeforeEach
    void setUp() {
        matchEventRepository = mock(MatchEventRepository.class);
        matchRepository      = mock(MatchRepository.class);
        teamRepository       = mock(TeamRepository.class);
        userRepository       = mock(UserRepository.class);
        tournamentRepository = mock(TournamentRepository.class);
        statsService = new StatsService(matchEventRepository, matchRepository, teamRepository, userRepository, tournamentRepository);
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

    // ── getTopScorersByTournament (GAP-15) ────────────────────────────────────

    @Test
    @DisplayName("GAP-15: getTopScorersByTournament – solo GOL en partidos del torneo")
    void getTopScorersByTournament_FiltersGolOnly() {
        Match match = matchWithId(10L);
        Tournament t = tournamentWithMatches(1L, List.of(match));
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));

        MatchEvent gol  = eventWithMatch(1L, "Jugador A", "GOL", 10L);
        MatchEvent ama  = eventWithMatch(2L, "Jugador B", "AMARILLA", 10L);
        MatchEvent roja = eventWithMatch(3L, "Jugador C", "ROJA", 10L);

        when(matchEventRepository.findByMatchIdIn(List.of(10L))).thenReturn(List.of(gol, ama, roja));

        List<PlayerStats> result = statsService.getTopScorersByTournament(1L);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getPlayerId());
    }

    @Test
    @DisplayName("GAP-15: getTopScorersByTournament – case-insensitive 'gol' → también cuenta")
    void getTopScorersByTournament_CaseInsensitiveGol() {
        Match match = matchWithId(20L);
        Tournament t = tournamentWithMatches(99L, List.of(match));
        when(tournamentRepository.findById(99L)).thenReturn(Optional.of(t));

        MatchEvent gol = eventWithMatch(1L, "Jugador A", "gol", 20L);
        when(matchEventRepository.findByMatchIdIn(List.of(20L))).thenReturn(List.of(gol));

        List<PlayerStats> result = statsService.getTopScorersByTournament(99L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("GAP-15: getTopScorersByTournament – torneo sin partidos → lista vacía")
    void getTopScorersByTournament_NoMatches_Empty() {
        Tournament t = tournamentWithMatches(1L, List.of());
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));

        assertTrue(statsService.getTopScorersByTournament(1L).isEmpty());
        verify(matchEventRepository, never()).findByMatchIdIn(any());
    }

    @Test
    @DisplayName("GAP-15: getTopScorersByTournament – torneo no encontrado → lista vacía")
    void getTopScorersByTournament_TournamentNotFound_Empty() {
        when(tournamentRepository.findById(99L)).thenReturn(Optional.empty());
        assertTrue(statsService.getTopScorersByTournament(99L).isEmpty());
    }

    @Test
    @DisplayName("GAP-15: getTopScorersByTournament – torneo encontrado pero matches == null → lista vacía")
    void getTopScorersByTournament_NullMatches_Empty() {
        Tournament t = new Tournament(); t.setId(1L); t.setMatches(null);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));

        assertTrue(statsService.getTopScorersByTournament(1L).isEmpty());
        verify(matchEventRepository, never()).findByMatchIdIn(any());
    }

    // ── getPlayerStats (GAP-16) ───────────────────────────────────────────────

    @Test
    @DisplayName("getPlayerStats – jugador no encontrado → ResourceNotFoundException")
    void getPlayerStats_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> statsService.getPlayerStats(99L));
    }

    @Test
    @DisplayName("GAP-16: getPlayerStats – sin eventos ni partidos → matchesPlayed=0, teamName vacío")
    void getPlayerStats_NoEvents_ZeroStats() {
        StudentPlayer p = new StudentPlayer();
        p.setId(1L); p.setFullName("Juan Jugador");
        when(userRepository.findById(1L)).thenReturn(Optional.of(p));
        when(matchEventRepository.findAll()).thenReturn(List.of());
        when(matchRepository.findAll()).thenReturn(List.of());

        PlayerStats stats = statsService.getPlayerStats(1L);

        assertEquals(0, stats.getGoals());
        assertEquals(0, stats.getYellowCards());
        assertEquals(0, stats.getRedCards());
        assertEquals(0, stats.getMatchesPlayed());
        assertEquals("", stats.getTeamName());
    }

    @Test
    @DisplayName("GAP-16: getPlayerStats – suma GOL, AMARILLA, ROJA correctamente")
    void getPlayerStats_WithEvents_AllTypes() {
        StudentPlayer p = new StudentPlayer();
        p.setId(1L); p.setFullName("Carlos Golea");
        when(userRepository.findById(1L)).thenReturn(Optional.of(p));

        MatchEvent g1  = eventWithMatch(1L, "Carlos Golea", "GOL", null);
        MatchEvent g2  = eventWithMatch(1L, "Carlos Golea", "GOL", null);
        MatchEvent ama = eventWithMatch(1L, "Carlos Golea", "AMARILLA", null);
        MatchEvent roj = eventWithMatch(1L, "Carlos Golea", "ROJA", null);
        MatchEvent other = eventWithMatch(2L, "Otro", "GOL", null);

        when(matchEventRepository.findAll()).thenReturn(List.of(g1, g2, ama, roj, other));
        when(matchRepository.findAll()).thenReturn(List.of());

        PlayerStats stats = statsService.getPlayerStats(1L);

        assertEquals(2, stats.getGoals());
        assertEquals(1, stats.getYellowCards());
        assertEquals(1, stats.getRedCards());
    }

    @Test
    @DisplayName("GAP-16: getPlayerStats – matchesPlayed cuenta partidos Finalizados únicos del jugador")
    void getPlayerStats_MatchesPlayed_CountsFinishedMatches() {
        StudentPlayer p = new StudentPlayer();
        p.setId(1L); p.setFullName("Ana Jugadora");
        when(userRepository.findById(1L)).thenReturn(Optional.of(p));

        // Player has events in 2 finished matches and 1 non-finished match
        MatchEvent e1 = eventWithMatch(1L, "Ana", "GOL", 10L);
        MatchEvent e2 = eventWithMatch(1L, "Ana", "AMARILLA", 10L); // same match
        MatchEvent e3 = eventWithMatch(1L, "Ana", "GOL", 11L);       // another finished match
        MatchEvent e4 = eventWithMatch(1L, "Ana", "GOL", 12L);       // non-finished match

        when(matchEventRepository.findAll()).thenReturn(List.of(e1, e2, e3, e4));

        Match fin1 = matchWithIdAndStatus(10L, "Finalizado");
        Match fin2 = matchWithIdAndStatus(11L, "Finalizado");
        Match prog = matchWithIdAndStatus(12L, "Programado");
        when(matchRepository.findAll()).thenReturn(List.of(fin1, fin2, prog));

        PlayerStats stats = statsService.getPlayerStats(1L);
        assertEquals(2, stats.getMatchesPlayed());
    }

    @Test
    @DisplayName("GAP-16: getPlayerStats – teamName resuelto desde el equipo del jugador")
    void getPlayerStats_TeamName_Resolved() {
        StudentPlayer p = new StudentPlayer();
        p.setId(1L); p.setFullName("Pedro Jugador");
        p.setTeamId(5L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(p));
        when(matchEventRepository.findAll()).thenReturn(List.of());
        when(matchRepository.findAll()).thenReturn(List.of());

        Team team = new Team(); team.setId(5L); team.setName("FC Ingeniería");
        when(teamRepository.findById(5L)).thenReturn(Optional.of(team));

        PlayerStats stats = statsService.getPlayerStats(1L);
        assertEquals("FC Ingeniería", stats.getTeamName());
    }

    @Test
    @DisplayName("GAP-16: getPlayerStats – teamId apunta a equipo inexistente → teamName vacío")
    void getPlayerStats_TeamNotFound_EmptyTeamName() {
        StudentPlayer p = new StudentPlayer();
        p.setId(1L); p.setFullName("Pedro"); p.setTeamId(99L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(p));
        when(matchEventRepository.findAll()).thenReturn(List.of());
        when(matchRepository.findAll()).thenReturn(List.of());
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        PlayerStats stats = statsService.getPlayerStats(1L);
        assertEquals("", stats.getTeamName());
    }

    // ── getTeamStats ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTeamStats – equipo no encontrado → ResourceNotFoundException")
    void getTeamStats_TeamNotFound() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> statsService.getTeamStats(99L));
    }

    // ── getTournamentStandings (GAP-14) ───────────────────────────────────────

    @Test
    @DisplayName("GAP-14: getTournamentStandings – torneo no encontrado → lista vacía")
    void getTournamentStandings_TournamentNotFound_Empty() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.empty());
        when(matchRepository.findAll()).thenReturn(List.of());

        assertTrue(statsService.getTournamentStandings(1L).isEmpty());
    }

    @Test
    @DisplayName("GAP-14: getTournamentStandings – torneo sin equipos → lista vacía")
    void getTournamentStandings_NoTeams_Empty() {
        Tournament t = tournamentWithTeams(1L, List.of());
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(matchRepository.findAll()).thenReturn(List.of());

        assertTrue(statsService.getTournamentStandings(1L).isEmpty());
    }

    @Test
    @DisplayName("GAP-14: getTournamentStandings – torneo encontrado pero registeredTeams == null → lista vacía")
    void getTournamentStandings_NullRegisteredTeams_Empty() {
        Tournament t = new Tournament(); t.setId(1L); t.setRegisteredTeams(null);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(matchRepository.findAll()).thenReturn(List.of());

        assertTrue(statsService.getTournamentStandings(1L).isEmpty());
    }

    @Test
    @DisplayName("GAP-14: getTournamentStandings – solo equipos del torneo, ordenados por puntos DESC")
    void getTournamentStandings_OnlyTournamentTeams_OrderedByPoints() {
        Team t1 = new Team(); t1.setId(1L); t1.setName("Team A"); t1.setPoints(9);
        Team t2 = new Team(); t2.setId(2L); t2.setName("Team B"); t2.setPoints(3);
        // t3 belongs to another tournament — should NOT appear
        Team t3 = new Team(); t3.setId(3L); t3.setName("Team C"); t3.setPoints(6);

        Tournament tour = tournamentWithTeams(1L, List.of(t2, t1)); // desordenado
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tour));
        when(matchRepository.findAll()).thenReturn(List.of());

        List<StandingDTO> standings = statsService.getTournamentStandings(1L);

        assertEquals(2, standings.size());
        assertEquals("Team A", standings.get(0).getTeamName()); // primero por puntos
        assertEquals("Team B", standings.get(1).getTeamName());
        // t3 not present
        assertTrue(standings.stream().noneMatch(s -> "Team C".equals(s.getTeamName())));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MatchEvent event(Long playerId, String playerName, String type) {
        MatchEvent e = new MatchEvent();
        e.setPlayerId(playerId);
        e.setPlayerName(playerName);
        e.setType(type);
        return e;
    }

    private MatchEvent eventWithMatch(Long playerId, String playerName, String type, Long matchId) {
        MatchEvent e = event(playerId, playerName, type);
        e.setMatchId(matchId);
        return e;
    }

    private Match matchWithId(Long id) {
        Match m = new Match();
        m.setId(id);
        return m;
    }

    private Match matchWithIdAndStatus(Long id, String status) {
        Match m = new Match();
        m.setId(id);
        m.setStatus(status);
        return m;
    }

    private Tournament tournamentWithMatches(Long id, List<Match> matches) {
        Tournament t = new Tournament();
        t.setId(id);
        t.setMatches(new ArrayList<>(matches));
        return t;
    }

    private Tournament tournamentWithTeams(Long id, List<Team> teams) {
        Tournament t = new Tournament();
        t.setId(id);
        t.setRegisteredTeams(new ArrayList<>(teams));
        return t;
    }
}
