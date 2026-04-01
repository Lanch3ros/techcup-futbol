package com.example.core.service;

import com.example.controller.dto.response.StandingDTO;
import com.example.core.model.*;
import com.example.repository.MatchEventRepository;
import com.example.repository.MatchRepository;
import com.example.repository.PlayerRepository;
import com.example.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@DisplayName("StatsService – Punto FairPlay (RN-09-2)")
class StatsServiceFairPlayTest {

    private MatchEventRepository matchEventRepository;
    private MatchRepository matchRepository;
    private TeamRepository teamRepository;
    private PlayerRepository playerRepository;
    private StatsService statsService;

    // Fixtures reutilizables
    private Team team1;
    private Team team2;
    private Match finishedMatch;

    @BeforeEach
    void setUp() {
        matchEventRepository = mock(MatchEventRepository.class);
        matchRepository      = mock(MatchRepository.class);
        teamRepository       = mock(TeamRepository.class);
        playerRepository     = mock(PlayerRepository.class);

        statsService = new StatsService(matchEventRepository, matchRepository,
                teamRepository, playerRepository);

        team1 = new Team(); team1.setId(1L); team1.setName("Team A"); team1.setPoints(3);
        team2 = new Team(); team2.setId(2L); team2.setName("Team B"); team2.setPoints(0);

        finishedMatch = new Match();
        finishedMatch.setId(10L);
        finishedMatch.setStatus("Finalizado");
        finishedMatch.setHomeTeam(team1);
        finishedMatch.setAwayTeam(team2);
    }

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Partido sin tarjetas → equipo local recibe +1 punto FairPlay")
    void noCards_HomeTeamGetsFairPlayPoint() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(finishedMatch));
        when(matchEventRepository.findByMatchId(10L)).thenReturn(List.of()); // 0 tarjetas

        StandingDTO result = statsService.getTeamStats(1L);

        assertEquals(4, result.getPoints(), "3 pts base + 1 FairPlay = 4");
    }

    @Test
    @DisplayName("Partido sin tarjetas → equipo visitante también recibe +1 punto FairPlay")
    void noCards_AwayTeamGetsFairPlayPoint() {
        when(teamRepository.findById(2L)).thenReturn(Optional.of(team2));
        when(matchRepository.findAll()).thenReturn(List.of(finishedMatch));
        when(matchEventRepository.findByMatchId(10L)).thenReturn(List.of());

        StandingDTO result = statsService.getTeamStats(2L);

        assertEquals(1, result.getPoints(), "0 pts base + 1 FairPlay = 1");
    }

    @Test
    @DisplayName("Partido con tarjeta amarilla propia → NO suma punto FairPlay")
    void yellowCard_NoFairPlayPoint() {
        StudentPlayer playerWithCard = new StudentPlayer();
        playerWithCard.setId(99L);
        playerWithCard.setTeamId(1L);

        MatchEvent yellowCard = new MatchEvent();
        yellowCard.setPlayerId(99L);
        yellowCard.setType("AMARILLA");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(finishedMatch));
        when(matchEventRepository.findByMatchId(10L)).thenReturn(List.of(yellowCard));
        when(playerRepository.findById(99L)).thenReturn(Optional.of(playerWithCard));

        StandingDTO result = statsService.getTeamStats(1L);

        assertEquals(3, result.getPoints(), "3 pts base, sin FairPlay por tarjeta propia");
    }

    @Test
    @DisplayName("Partido con tarjeta roja propia → NO suma punto FairPlay")
    void redCard_NoFairPlayPoint() {
        StudentPlayer playerWithCard = new StudentPlayer();
        playerWithCard.setId(88L);
        playerWithCard.setTeamId(1L);

        MatchEvent redCard = new MatchEvent();
        redCard.setPlayerId(88L);
        redCard.setType("ROJA");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(finishedMatch));
        when(matchEventRepository.findByMatchId(10L)).thenReturn(List.of(redCard));
        when(playerRepository.findById(88L)).thenReturn(Optional.of(playerWithCard));

        StandingDTO result = statsService.getTeamStats(1L);

        assertEquals(3, result.getPoints(), "3 pts base, sin FairPlay por tarjeta roja propia");
    }

    @Test
    @DisplayName("Tarjeta del RIVAL → el equipo propio SÍ recibe el punto FairPlay")
    void rivalCard_OwnTeamGetsFairPlayPoint() {
        StudentPlayer rivalPlayer = new StudentPlayer();
        rivalPlayer.setId(77L);
        rivalPlayer.setTeamId(2L); // la tarjeta es del team2

        MatchEvent yellowCard = new MatchEvent();
        yellowCard.setPlayerId(77L);
        yellowCard.setType("AMARILLA");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(finishedMatch));
        when(matchEventRepository.findByMatchId(10L)).thenReturn(List.of(yellowCard));
        when(playerRepository.findById(77L)).thenReturn(Optional.of(rivalPlayer));

        StandingDTO result = statsService.getTeamStats(1L);

        assertEquals(4, result.getPoints(), "3 pts + 1 FairPlay: la tarjeta era del rival, no nuestra");
    }

    // ── Edge cases ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Partido en estado 'Programado' → no cuenta para FairPlay")
    void matchNotFinished_NoFairPlayPoint() {
        finishedMatch.setStatus("Programado");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(finishedMatch));

        StandingDTO result = statsService.getTeamStats(1L);

        assertEquals(3, result.getPoints(), "Partido no finalizado: sin FairPlay");
        verifyNoInteractions(matchEventRepository);
    }

    @Test
    @DisplayName("Dos partidos sin tarjetas → +2 puntos FairPlay acumulados")
    void twoMatchesNoCards_TwoFairPlayPoints() {
        Match match2 = new Match();
        match2.setId(11L);
        match2.setStatus("Finalizado");
        match2.setHomeTeam(team1);
        match2.setAwayTeam(team2);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(finishedMatch, match2));
        when(matchEventRepository.findByMatchId(10L)).thenReturn(List.of());
        when(matchEventRepository.findByMatchId(11L)).thenReturn(List.of());

        StandingDTO result = statsService.getTeamStats(1L);

        assertEquals(5, result.getPoints(), "3 pts base + 2 FairPlay = 5");
    }

    @Test
    @DisplayName("Un partido con tarjeta y otro sin → solo +1 punto FairPlay")
    void oneMatchWithCard_OneWithout_OneFairPlayPoint() {
        StudentPlayer playerWithCard = new StudentPlayer();
        playerWithCard.setId(55L);
        playerWithCard.setTeamId(1L);

        MatchEvent card = new MatchEvent();
        card.setPlayerId(55L);
        card.setType("AMARILLA");

        Match cleanMatch = new Match();
        cleanMatch.setId(11L);
        cleanMatch.setStatus("Finalizado");
        cleanMatch.setHomeTeam(team1);
        cleanMatch.setAwayTeam(team2);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(finishedMatch, cleanMatch));
        when(matchEventRepository.findByMatchId(10L)).thenReturn(List.of(card));  // partido sucio
        when(matchEventRepository.findByMatchId(11L)).thenReturn(List.of());       // partido limpio
        when(playerRepository.findById(55L)).thenReturn(Optional.of(playerWithCard));

        StandingDTO result = statsService.getTeamStats(1L);

        assertEquals(4, result.getPoints(), "3 pts base + 1 FairPlay del partido limpio = 4");
    }
}
