package com.example.core.service;

import com.example.controller.dto.request.MatchCreationRequest;
import com.example.controller.dto.request.MatchEventRequest;
import com.example.controller.dto.request.MatchResultRequest;
import com.example.core.exception.BusinessRuleException;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.*;
import com.example.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("MatchService – Gestión de partidos")
class MatchServiceTest {

    private MatchRepository matchRepository;
    private MatchEventRepository matchEventRepository;
    private TeamRepository teamRepository;
    private RefereeRepository refereeRepository;
    private MatchService matchService;

    private Team homeTeam;
    private Team awayTeam;

    @BeforeEach
    void setUp() {
        matchRepository      = mock(MatchRepository.class);
        matchEventRepository = mock(MatchEventRepository.class);
        teamRepository       = mock(TeamRepository.class);
        refereeRepository    = mock(RefereeRepository.class);
        matchService = new MatchService(matchRepository, matchEventRepository, teamRepository, refereeRepository);

        homeTeam = new Team(); homeTeam.setId(1L); homeTeam.setName("Local FC");
        awayTeam = new Team(); awayTeam.setId(2L); awayTeam.setName("Visitante FC");
    }

    // ── createMatch ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createMatch – happy path crea y persiste el partido")
    void createMatch_Success() {
        MatchCreationRequest req = new MatchCreationRequest();
        req.setHomeTeamId(1L);
        req.setAwayTeamId(2L);
        req.setMatchDate(LocalDateTime.now().plusDays(3));
        req.setField("Campo 1");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(awayTeam));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> {
            Match m = inv.getArgument(0);
            m.setId(10L);
            return m;
        });

        Match result = matchService.createMatch(req);

        assertNotNull(result);
        assertEquals("Programado", result.getStatus());
        assertEquals(homeTeam, result.getHomeTeam());
        assertEquals(awayTeam, result.getAwayTeam());
        verify(matchRepository).save(any(Match.class));
    }

    @Test
    @DisplayName("createMatch – equipo local no encontrado → ResourceNotFoundException")
    void createMatch_HomeTeamNotFound() {
        MatchCreationRequest req = new MatchCreationRequest();
        req.setHomeTeamId(99L);
        req.setAwayTeamId(2L);
        req.setMatchDate(LocalDateTime.now());

        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> matchService.createMatch(req));
    }

    @Test
    @DisplayName("createMatch – equipo visitante no encontrado → ResourceNotFoundException")
    void createMatch_AwayTeamNotFound() {
        MatchCreationRequest req = new MatchCreationRequest();
        req.setHomeTeamId(1L);
        req.setAwayTeamId(99L);
        req.setMatchDate(LocalDateTime.now());

        when(teamRepository.findById(1L)).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> matchService.createMatch(req));
    }

    @Test
    @DisplayName("createMatch – mismo equipo local y visitante → BusinessRuleException")
    void createMatch_SameTeam_Throws() {
        MatchCreationRequest req = new MatchCreationRequest();
        req.setHomeTeamId(1L);
        req.setAwayTeamId(1L);
        req.setMatchDate(LocalDateTime.now());

        when(teamRepository.findById(1L)).thenReturn(Optional.of(homeTeam));

        assertThrows(BusinessRuleException.class, () -> matchService.createMatch(req));
    }

    // ── getAllMatches ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllMatches – devuelve lista completa")
    void getAllMatches_ReturnsList() {
        Match m = new Match(); m.setId(1L);
        when(matchRepository.findAll()).thenReturn(List.of(m));

        List<Match> result = matchService.getAllMatches();
        assertEquals(1, result.size());
    }

    // ── getMatchById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMatchById – encontrado correctamente")
    void getMatchById_Found() {
        Match m = new Match(); m.setId(5L);
        when(matchRepository.findById(5L)).thenReturn(Optional.of(m));

        Match result = matchService.getMatchById(5L);
        assertEquals(5L, result.getId());
    }

    @Test
    @DisplayName("getMatchById – no encontrado → ResourceNotFoundException")
    void getMatchById_NotFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> matchService.getMatchById(99L));
    }

    // ── updateMatchStatus ─────────────────────────────────────────────────────

    @Test
    @DisplayName("updateMatchStatus – estado válido 'En Curso'")
    void updateMatchStatus_ValidStatusEnCurso() {
        Match m = new Match(); m.setId(1L); m.setStatus("Programado");
        when(matchRepository.findById(1L)).thenReturn(Optional.of(m));
        when(matchRepository.save(any())).thenReturn(m);

        assertDoesNotThrow(() -> matchService.updateMatchStatus(1L, "En Curso"));
        assertEquals("En Curso", m.getStatus());
    }

    @Test
    @DisplayName("updateMatchStatus – estado válido 'Finalizado'")
    void updateMatchStatus_ValidStatusFinalizado() {
        Match m = new Match(); m.setId(1L); m.setStatus("En Curso");
        when(matchRepository.findById(1L)).thenReturn(Optional.of(m));
        when(matchRepository.save(any())).thenReturn(m);

        assertDoesNotThrow(() -> matchService.updateMatchStatus(1L, "Finalizado"));
    }

    @Test
    @DisplayName("updateMatchStatus – estado válido 'Programado'")
    void updateMatchStatus_ValidStatusProgramado() {
        Match m = new Match(); m.setId(1L); m.setStatus("En Curso");
        when(matchRepository.findById(1L)).thenReturn(Optional.of(m));
        when(matchRepository.save(any())).thenReturn(m);

        assertDoesNotThrow(() -> matchService.updateMatchStatus(1L, "Programado"));
    }

    @Test
    @DisplayName("updateMatchStatus – estado inválido → BusinessRuleException")
    void updateMatchStatus_InvalidStatus_Throws() {
        Match m = new Match(); m.setId(1L); m.setStatus("Programado");
        when(matchRepository.findById(1L)).thenReturn(Optional.of(m));

        assertThrows(BusinessRuleException.class, () -> matchService.updateMatchStatus(1L, "INVALIDO"));
    }

    // ── registerResult ────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerResult – partido finalizado → persiste resultado (RN-08-1)")
    void registerResult_Success() {
        Match m = new Match(); m.setId(1L); m.setStatus("Finalizado");
        when(matchRepository.findById(1L)).thenReturn(Optional.of(m));
        when(matchRepository.save(any())).thenReturn(m);

        MatchResultRequest req = new MatchResultRequest();
        req.setHomeGoals(2);
        req.setAwayGoals(1);

        assertDoesNotThrow(() -> matchService.registerResult(1L, req));
        assertEquals(2, m.getHomeGoals());
        assertEquals(1, m.getAwayGoals());
    }

    @Test
    @DisplayName("registerResult – status case-insensitive 'FINALIZADO' → acepta (RN-08-1)")
    void registerResult_CaseInsensitiveStatus() {
        Match m = new Match(); m.setId(1L); m.setStatus("FINALIZADO");
        when(matchRepository.findById(1L)).thenReturn(Optional.of(m));
        when(matchRepository.save(any())).thenReturn(m);

        MatchResultRequest req = new MatchResultRequest();
        req.setHomeGoals(3); req.setAwayGoals(0);

        assertDoesNotThrow(() -> matchService.registerResult(1L, req));
    }

    @Test
    @DisplayName("registerResult – partido no finalizado → BusinessRuleException (RN-08-1)")
    void registerResult_NotFinalizado_Throws() {
        Match m = new Match(); m.setId(1L); m.setStatus("Programado");
        when(matchRepository.findById(1L)).thenReturn(Optional.of(m));

        MatchResultRequest req = new MatchResultRequest();
        req.setHomeGoals(1); req.setAwayGoals(0);

        assertThrows(BusinessRuleException.class, () -> matchService.registerResult(1L, req));
    }

    // ── registerEvent ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerEvent – persiste el evento correctamente")
    void registerEvent_Success() {
        Match m = new Match(); m.setId(1L);
        when(matchRepository.findById(1L)).thenReturn(Optional.of(m));

        MatchEvent savedEvent = new MatchEvent();
        savedEvent.setId(100L);
        when(matchEventRepository.save(any())).thenReturn(savedEvent);

        MatchEventRequest req = new MatchEventRequest();
        req.setPlayerId(5L);
        req.setPlayerName("Juan García");
        req.setType("GOL");
        req.setMinute(30);

        MatchEvent result = matchService.registerEvent(1L, req);
        assertNotNull(result);
        assertEquals(100L, result.getId());
    }

    @Test
    @DisplayName("registerEvent – partido no encontrado → ResourceNotFoundException")
    void registerEvent_MatchNotFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        MatchEventRequest req = new MatchEventRequest();
        req.setPlayerId(1L); req.setType("GOL"); req.setMinute(10);

        assertThrows(ResourceNotFoundException.class, () -> matchService.registerEvent(99L, req));
    }

    // ── getMatchEvents ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMatchEvents – devuelve eventos del partido")
    void getMatchEvents_ReturnsList() {
        Match m = new Match(); m.setId(1L);
        when(matchRepository.findById(1L)).thenReturn(Optional.of(m));

        MatchEvent e = new MatchEvent(); e.setId(1L);
        when(matchEventRepository.findByMatchId(1L)).thenReturn(List.of(e));

        List<MatchEvent> result = matchService.getMatchEvents(1L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("getMatchEvents – partido no encontrado → ResourceNotFoundException")
    void getMatchEvents_MatchNotFound() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> matchService.getMatchEvents(99L));
    }

    // ── assignReferee ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("assignReferee – asigna árbitro correctamente")
    void assignReferee_Success() {
        Match m = new Match(); m.setId(1L); m.setStatus("Programado");
        RefereeUser referee = new RefereeUser();
        referee.setId(1L); referee.setFullName("Carlos Pérez");
        referee.setAssignedMatchIds(new ArrayList<>());

        when(matchRepository.findById(1L)).thenReturn(Optional.of(m));
        when(refereeRepository.findById(1L)).thenReturn(Optional.of(referee));
        when(matchRepository.save(any())).thenReturn(m);
        when(refereeRepository.save(any())).thenReturn(referee);

        assertDoesNotThrow(() -> matchService.assignReferee(1L, 1L));
        assertEquals("Carlos Pérez", m.getReferee());
        assertTrue(referee.getAssignedMatchIds().contains(1L));
    }

    @Test
    @DisplayName("assignReferee – árbitro no encontrado → ResourceNotFoundException")
    void assignReferee_RefereeNotFound() {
        Match m = new Match(); m.setId(1L);
        when(matchRepository.findById(1L)).thenReturn(Optional.of(m));
        when(refereeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> matchService.assignReferee(1L, 99L));
    }
}
