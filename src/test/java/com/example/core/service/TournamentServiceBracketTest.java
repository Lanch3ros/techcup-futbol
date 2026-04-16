package com.example.core.service;

import com.example.core.exception.BusinessRuleException;
import com.example.core.model.Match;
import com.example.core.model.Team;
import com.example.core.model.Tournament;
import com.example.repository.MatchRepository;
import com.example.repository.PaymentRepository;
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

@DisplayName("TournamentService – Cuartos de Final / Brackets (6.11)")
class TournamentServiceBracketTest {

    private TournamentRepository tournamentRepository;
    private TeamRepository teamRepository;
    private MatchRepository matchRepository;
    private PaymentRepository paymentRepository;
    private TournamentService tournamentService;

    private Tournament tournament;
    private List<Team> top8;

    @BeforeEach
    void setUp() {
        tournamentRepository = mock(TournamentRepository.class);
        teamRepository       = mock(TeamRepository.class);
        matchRepository      = mock(MatchRepository.class);
        paymentRepository    = mock(PaymentRepository.class);

        tournamentService = new TournamentService(tournamentRepository, teamRepository, matchRepository, paymentRepository);

        // Torneo en estado correcto
        tournament = new Tournament();
        tournament.setId(1L);
        tournament.setName("TechCup 2026-I");
        tournament.setStatus("en progreso");
        tournament.setStartDate(LocalDate.now().minusDays(10));
        tournament.setEndDate(LocalDate.now().plusDays(30));
        tournament.setMaxTeams(10);
        tournament.setMatches(new ArrayList<>());

        // 8 equipos con puntos claramente distintos (seed 1..8)
        top8 = new ArrayList<>();
        int[] pointsPerSeed = {21, 18, 15, 12, 9, 6, 3, 0};
        for (int i = 0; i < 8; i++) {
            Team t = new Team();
            t.setId((long) (i + 1));
            t.setName("Seed" + (i + 1));
            t.setPoints(pointsPerSeed[i]);
            t.setGoalDifference(0);
            t.setGoalsFor(0);
            top8.add(t);
        }
        tournament.setRegisteredTeams(new ArrayList<>(top8));
    }

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Genera exactamente 4 partidos de Cuartos de Final")
    void generateQuarterFinals_ProducesExactlyFourMatches() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        List<Match> result = tournamentService.generateQuarterFinals(1L);

        assertEquals(4, result.size(), "Deben generarse exactamente 4 cruces");
    }

    @Test
    @DisplayName("1° (Seed1) enfrenta al 8° (Seed8) – seeding clásico")
    void seed1VsSeed8_ClassicBracket() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        List<Match> result = tournamentService.generateQuarterFinals(1L);

        Match firstMatch = result.get(0);
        assertEquals("Seed1", firstMatch.getHomeTeam().getName(), "Home del cruce 1 debe ser el 1°");
        assertEquals("Seed8", firstMatch.getAwayTeam().getName(), "Away del cruce 1 debe ser el 8°");
    }

    @Test
    @DisplayName("2° (Seed2) enfrenta al 7° (Seed7)")
    void seed2VsSeed7() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        List<Match> result = tournamentService.generateQuarterFinals(1L);

        Match secondMatch = result.get(1);
        assertEquals("Seed2", secondMatch.getHomeTeam().getName());
        assertEquals("Seed7", secondMatch.getAwayTeam().getName());
    }

    @Test
    @DisplayName("3° (Seed3) enfrenta al 6° (Seed6) y 4° (Seed4) al 5° (Seed5)")
    void seed3VsSeed6_And_Seed4VsSeed5() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        List<Match> result = tournamentService.generateQuarterFinals(1L);

        assertEquals("Seed3", result.get(2).getHomeTeam().getName());
        assertEquals("Seed6", result.get(2).getAwayTeam().getName());
        assertEquals("Seed4", result.get(3).getHomeTeam().getName());
        assertEquals("Seed5", result.get(3).getAwayTeam().getName());
    }

    @Test
    @DisplayName("Todos los partidos quedan con fase 'Cuartos de Final' y estado 'Programado'")
    void allMatchesHaveCorrectPhaseAndStatus() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        List<Match> result = tournamentService.generateQuarterFinals(1L);

        result.forEach(m -> {
            assertEquals("Cuartos de Final", m.getPhase());
            assertEquals("Programado", m.getStatus());
        });
    }

    @Test
    @DisplayName("Más de 8 equipos: solo los 8 mejores clasifican y el seeding es por puntos")
    void moreThanEightTeams_OnlyTop8Seed() {
        // Añadir 4 equipos extra con 0 puntos (quedaran fuera del top8)
        for (int i = 9; i <= 12; i++) {
            Team extra = new Team();
            extra.setId((long) i);
            extra.setName("Extra" + i);
            extra.setPoints(0);
            extra.setGoalDifference(-5);
            extra.setGoalsFor(0);
            tournament.getRegisteredTeams().add(extra);
        }

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        List<Match> result = tournamentService.generateQuarterFinals(1L);

        assertEquals(4, result.size());
        // Seed1 (21 pts) sigue siendo el 1° cabeza de serie
        assertEquals("Seed1", result.get(0).getHomeTeam().getName());
    }

    // ── Edge cases / Guards ──────────────────────────────────────────────────

    @Test
    @DisplayName("Torneo no en estado 'En progreso' → BusinessRuleException")
    void tournamentNotInProgress_ThrowsException() {
        tournament.setStatus("Creado");

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        assertThrows(BusinessRuleException.class,
                () -> tournamentService.generateQuarterFinals(1L),
                "Debe rechazar si el torneo no está 'En progreso'");
    }

    @Test
    @DisplayName("Cuartos ya generados → BusinessRuleException (idempotencia)")
    void quartersAlreadyGenerated_ThrowsException() {
        Match existing = new Match();
        existing.setPhase("CUARTOS DE FINAL");
        tournament.setMatches(new ArrayList<>(List.of(existing)));

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        assertThrows(BusinessRuleException.class,
                () -> tournamentService.generateQuarterFinals(1L));
    }

    @Test
    @DisplayName("Menos de 8 equipos registrados → BusinessRuleException")
    void fewerThanEightTeams_ThrowsException() {
        tournament.setRegisteredTeams(top8.subList(0, 6)); // solo 6 equipos

        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        assertThrows(BusinessRuleException.class,
                () -> tournamentService.generateQuarterFinals(1L));
    }

    @Test
    @DisplayName("Exactamente 8 equipos → genera los cruces sin error")
    void exactlyEightTeams_GeneratesSuccessfully() {
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        assertDoesNotThrow(() -> tournamentService.generateQuarterFinals(1L));
    }

    @Test
    @DisplayName("matches null → alreadyGenerated=false, y matches se inicializa al guardar")
    void generateQuarterFinals_NullMatches_InitializedAndGenerated() {
        tournament.setMatches(null); // rama: tournament.getMatches() == null → alreadyGenerated=false
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));
        when(matchRepository.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tournamentRepository.save(any(Tournament.class))).thenReturn(tournament);

        List<Match> result = tournamentService.generateQuarterFinals(1L);
        assertEquals(4, result.size());
        assertNotNull(tournament.getMatches()); // rama: setMatches(new ArrayList<>()) fue ejecutado
    }

    @Test
    @DisplayName("registeredTeams null → BusinessRuleException con size=0 en log (ternario)")
    void generateQuarterFinals_NullRegisteredTeams_ThrowsWithZeroInLog() {
        tournament.setRegisteredTeams(null); // fuerza rama: registered == null → ternario usa 0
        when(tournamentRepository.findById(1L)).thenReturn(Optional.of(tournament));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> tournamentService.generateQuarterFinals(1L));
        assertTrue(ex.getMessage().contains("8"));
    }
}
