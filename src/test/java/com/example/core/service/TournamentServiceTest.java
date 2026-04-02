package com.example.core.service;

import com.example.core.exception.BusinessRuleException;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Match;
import com.example.core.model.Team;
import com.example.core.model.Tournament;
import com.example.repository.MatchRepository;
import com.example.repository.TeamRepository;
import com.example.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TournamentService – Gestión general de torneos")
class TournamentServiceTest {

    private TournamentRepository tournamentRepository;
    private TeamRepository teamRepository;
    private MatchRepository matchRepository;
    private TournamentService tournamentService;

    @BeforeEach
    void setUp() {
        tournamentRepository = mock(TournamentRepository.class);
        teamRepository       = mock(TeamRepository.class);
        matchRepository      = mock(MatchRepository.class);
        tournamentService    = new TournamentService(tournamentRepository, teamRepository, matchRepository);
    }

    private Tournament activeTournament() {
        Tournament t = new Tournament();
        t.setId(1L);
        t.setName("TechCup 2026-I");
        t.setStatus("Activo");
        t.setStartDate(LocalDate.now().minusDays(1));
        t.setEndDate(LocalDate.now().plusDays(30));
        t.setMaxTeams(10);
        t.setRegisteredTeams(new ArrayList<>());
        t.setMatches(new ArrayList<>());
        return t;
    }

    // ── createTournament ──────────────────────────────────────────────────────

    @Test
    @DisplayName("createTournament – fechas válidas → persiste torneo")
    void createTournament_ValidDates_Success() {
        Tournament t = activeTournament();
        when(tournamentRepository.save(any())).thenReturn(t);

        Tournament result = tournamentService.createTournament(t);
        assertNotNull(result);
        assertEquals("TechCup 2026-I", result.getName());
        verify(tournamentRepository).save(t);
    }

    @Test
    @DisplayName("createTournament – fecha fin antes de inicio → BusinessRuleException")
    void createTournament_EndBeforeStart_Throws() {
        Tournament t = new Tournament();
        t.setStartDate(LocalDate.now().plusDays(10));
        t.setEndDate(LocalDate.now());

        assertThrows(BusinessRuleException.class, () -> tournamentService.createTournament(t));
    }

    // ── getAllTournaments ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllTournaments – devuelve lista completa")
    void getAllTournaments_ReturnsList() {
        when(tournamentRepository.findAll()).thenReturn(List.of(activeTournament(), activeTournament()));
        assertEquals(2, tournamentService.getAllTournaments().size());
    }

    // ── getTournamentById ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getTournamentById – encontrado")
    void getTournamentById_Found() {
        Tournament t = activeTournament();
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));
        assertNotNull(tournamentService.getTournamentById(1L));
    }

    @Test
    @DisplayName("getTournamentById – no encontrado → ResourceNotFoundException")
    void getTournamentById_NotFound() {
        when(tournamentRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> tournamentService.getTournamentById(99L));
    }

    // ── updateTournamentStatus ────────────────────────────────────────────────

    @Test
    @DisplayName("updateTournamentStatus – 'Borrador' es válido")
    void updateTournamentStatus_Borrador_Valid() {
        Tournament t = activeTournament();
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(tournamentRepository.save(any())).thenReturn(t);

        assertDoesNotThrow(() -> tournamentService.updateTournamentStatus(1L, "Borrador"));
        assertEquals("Borrador", t.getStatus());
    }

    @Test
    @DisplayName("updateTournamentStatus – 'En progreso' es válido")
    void updateTournamentStatus_EnProgreso_Valid() {
        Tournament t = activeTournament();
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(tournamentRepository.save(any())).thenReturn(t);

        assertDoesNotThrow(() -> tournamentService.updateTournamentStatus(1L, "En progreso"));
    }

    @Test
    @DisplayName("updateTournamentStatus – 'Finalizado' es válido")
    void updateTournamentStatus_Finalizado_Valid() {
        Tournament t = activeTournament();
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(tournamentRepository.save(any())).thenReturn(t);

        assertDoesNotThrow(() -> tournamentService.updateTournamentStatus(1L, "Finalizado"));
    }

    @Test
    @DisplayName("updateTournamentStatus – estado inválido → BusinessRuleException")
    void updateTournamentStatus_InvalidStatus_Throws() {
        Tournament t = activeTournament();
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThrows(BusinessRuleException.class,
                () -> tournamentService.updateTournamentStatus(1L, "Suspendido"));
    }

    // ── getTournamentTeams ────────────────────────────────────────────────────

    @Test
    @DisplayName("getTournamentTeams – devuelve equipos inscritos")
    void getTournamentTeams_WithTeams() {
        Tournament t = activeTournament();
        Team team = new Team(); team.setId(1L); team.setName("FC Test");
        t.getRegisteredTeams().add(team);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));

        List<Team> result = tournamentService.getTournamentTeams(1L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getTournamentTeams – sin equipos registrados (null) → lista vacía")
    void getTournamentTeams_NullTeams_ReturnsEmpty() {
        Tournament t = activeTournament();
        t.setRegisteredTeams(null);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));

        List<Team> result = tournamentService.getTournamentTeams(1L);
        assertTrue(result.isEmpty());
    }

    // ── registerTeamToTournament ──────────────────────────────────────────────

    @Test
    @DisplayName("registerTeamToTournament – equipo no encontrado → ResourceNotFoundException")
    void registerTeamToTournament_TeamNotFound() {
        Tournament t = activeTournament();
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tournamentService.registerTeamToTournament(1L, 99L));
    }

    @Test
    @DisplayName("registerTeamToTournament – torneo no 'Activo' → BusinessRuleException")
    void registerTeamToTournament_TournamentNotActivo() {
        Tournament t = activeTournament();
        t.setStatus("Borrador");
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));

        Team team = new Team(); team.setId(1L);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        assertThrows(BusinessRuleException.class,
                () -> tournamentService.registerTeamToTournament(1L, 1L));
    }

    @Test
    @DisplayName("registerTeamToTournament – equipo ya inscrito → BusinessRuleException")
    void registerTeamToTournament_AlreadyRegistered() {
        Team team = new Team(); team.setId(5L); team.setName("FC Doble");
        Tournament t = activeTournament();
        t.getRegisteredTeams().add(team);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(teamRepository.findById(5L)).thenReturn(Optional.of(team));

        assertThrows(BusinessRuleException.class,
                () -> tournamentService.registerTeamToTournament(1L, 5L));
    }

    @Test
    @DisplayName("registerTeamToTournament – cupo máximo alcanzado → BusinessRuleException")
    void registerTeamToTournament_MaxTeamsReached() {
        Tournament t = activeTournament();
        t.setMaxTeams(1);
        Team existing = new Team(); existing.setId(3L);
        t.getRegisteredTeams().add(existing);

        Team newTeam = new Team(); newTeam.setId(4L);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(teamRepository.findById(4L)).thenReturn(Optional.of(newTeam));

        assertThrows(BusinessRuleException.class,
                () -> tournamentService.registerTeamToTournament(1L, 4L));
    }

    @Test
    @DisplayName("registerTeamToTournament – éxito → equipo inscrito y persistido")
    void registerTeamToTournament_Success() {
        Tournament t = activeTournament();
        Team team = new Team(); team.setId(7L); team.setName("FC Nuevo");

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(teamRepository.findById(7L)).thenReturn(Optional.of(team));
        when(tournamentRepository.save(any())).thenReturn(t);
        when(teamRepository.save(any())).thenReturn(team);

        assertDoesNotThrow(() -> tournamentService.registerTeamToTournament(1L, 7L));
        assertTrue(t.getRegisteredTeams().contains(team));
        assertEquals(1L, team.getTournamentId());
    }

    @Test
    @DisplayName("registerTeamToTournament – registeredTeams null → se inicializa antes de inscribir")
    void registerTeamToTournament_NullRegisteredTeams_Initialized() {
        Tournament t = activeTournament();
        t.setRegisteredTeams(null); // fuerza rama null
        Team team = new Team(); team.setId(8L); team.setName("FC Null");

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(teamRepository.findById(8L)).thenReturn(Optional.of(team));
        when(tournamentRepository.save(any())).thenReturn(t);
        when(teamRepository.save(any())).thenReturn(team);

        assertDoesNotThrow(() -> tournamentService.registerTeamToTournament(1L, 8L));
        assertNotNull(t.getRegisteredTeams());
        assertTrue(t.getRegisteredTeams().contains(team));
    }

    // ── generateMatches ───────────────────────────────────────────────────────

    @Test
    @DisplayName("generateMatches – menos de 2 equipos → BusinessRuleException")
    void generateMatches_LessThanTwoTeams_Throws() {
        Tournament t = activeTournament();
        Team only = new Team(); only.setId(1L);
        t.getRegisteredTeams().add(only);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThrows(BusinessRuleException.class, () -> tournamentService.generateMatches(1L));
    }

    @Test
    @DisplayName("generateMatches – sin equipos (null) → BusinessRuleException")
    void generateMatches_NullTeams_Throws() {
        Tournament t = activeTournament();
        t.setRegisteredTeams(null);
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));

        assertThrows(BusinessRuleException.class, () -> tournamentService.generateMatches(1L));
    }

    @Test
    @DisplayName("generateMatches – 4 equipos → 2 partidos generados")
    void generateMatches_FourTeams_TwoMatches() {
        Tournament t = activeTournament();
        for (int i = 1; i <= 4; i++) {
            Team team = new Team(); team.setId((long) i); team.setName("Team" + i);
            t.getRegisteredTeams().add(team);
        }
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tournamentRepository.save(any())).thenReturn(t);

        List<Match> result = tournamentService.generateMatches(1L);
        assertEquals(2, result.size());
        result.forEach(m -> assertEquals("Programado", m.getStatus()));
    }

    @Test
    @DisplayName("generateMatches – torneo con matches null inicializados correctamente")
    void generateMatches_NullMatches_InitializedCorrectly() {
        Tournament t = activeTournament();
        t.setMatches(null);
        Team a = new Team(); a.setId(1L);
        Team b = new Team(); b.setId(2L);
        t.getRegisteredTeams().add(a);
        t.getRegisteredTeams().add(b);

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(t));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tournamentRepository.save(any())).thenReturn(t);

        assertDoesNotThrow(() -> tournamentService.generateMatches(1L));
        assertNotNull(t.getMatches());
    }
}
