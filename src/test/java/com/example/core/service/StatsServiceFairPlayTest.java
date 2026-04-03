package com.example.core.service;

import com.example.controller.dto.response.StandingDTO;
import com.example.core.model.*;
import com.example.repository.MatchEventRepository;
import com.example.repository.MatchRepository;
import com.example.repository.TournamentRepository;
import com.example.repository.UserRepository;
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
    private UserRepository userRepository;
    private TournamentRepository tournamentRepository;
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
        userRepository       = mock(UserRepository.class);
        tournamentRepository = mock(TournamentRepository.class);

        statsService = new StatsService(matchEventRepository, matchRepository,
                teamRepository, userRepository, tournamentRepository);

        team1 = new Team(); team1.setId(1L); team1.setName("Team A"); team1.setPoints(3);
        team2 = new Team(); team2.setId(2L); team2.setName("Team B"); team2.setPoints(0);

        finishedMatch = new Match();
        finishedMatch.setId(10L);
        finishedMatch.setStatus("FINALIZADO");
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
        yellowCard.setType("amarilla");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(finishedMatch));
        when(matchEventRepository.findByMatchId(10L)).thenReturn(List.of(yellowCard));
        when(userRepository.findById(99L)).thenReturn(Optional.of(playerWithCard));

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
        redCard.setType("roja");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(finishedMatch));
        when(matchEventRepository.findByMatchId(10L)).thenReturn(List.of(redCard));
        when(userRepository.findById(88L)).thenReturn(Optional.of(playerWithCard));

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
        when(userRepository.findById(77L)).thenReturn(Optional.of(rivalPlayer));

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
        match2.setStatus("finalizado");
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
    @DisplayName("Partido con homeTeam null → teamParticipated via awayTeam (rama homeTeam == null)")
    void matchWithNullHomeTeam_AwayTeamMatch_FairPlayPoint() {
        Match matchNoHome = new Match();
        matchNoHome.setId(20L);
        matchNoHome.setStatus("FINALIZADO");
        matchNoHome.setHomeTeam(null);   // homeTeam null → rama false de homeTeam != null
        matchNoHome.setAwayTeam(team1);  // team1 participa como visitante

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(matchNoHome));
        when(matchEventRepository.findByMatchId(20L)).thenReturn(List.of());

        StandingDTO result = statsService.getTeamStats(1L);
        assertEquals(4, result.getPoints(), "3 pts base + 1 FairPlay vía awayTeam");
    }

    @Test
    @DisplayName("Partido con awayTeam null y homeTeam coincide → teamParticipated via homeTeam")
    void matchWithNullAwayTeam_HomeTeamMatch_FairPlayPoint() {
        Match matchNoAway = new Match();
        matchNoAway.setId(21L);
        matchNoAway.setStatus("FINALIZADO");
        matchNoAway.setHomeTeam(team1);
        matchNoAway.setAwayTeam(null);  // awayTeam null → rama false de awayTeam != null

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(matchNoAway));
        when(matchEventRepository.findByMatchId(21L)).thenReturn(List.of());

        StandingDTO result = statsService.getTeamStats(1L);
        assertEquals(4, result.getPoints(), "3 pts base + 1 FairPlay vía homeTeam");
    }

    @Test
    @DisplayName("Evento con playerId inexistente → player null → no se cuenta tarjeta del equipo")
    void eventWithUnknownPlayer_NullPlayer_IgnoredInCardCheck() {
        MatchEvent card = new MatchEvent();
        card.setPlayerId(999L); // ID que no existe en el repo
        card.setType("AMARILLA");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(finishedMatch));
        when(matchEventRepository.findByMatchId(10L)).thenReturn(List.of(card));
        when(userRepository.findById(999L)).thenReturn(Optional.empty()); // → orElse(null) → null

        StandingDTO result = statsService.getTeamStats(1L);
        // player es null → no puede contar como tarjeta de team1 → FairPlay otorgado
        assertEquals(4, result.getPoints(), "player null no bloquea FairPlay");
    }

    @Test
    @DisplayName("Partido con awayTeam null Y homeTeam no coincide → no participa (rama awayTeam null tras homeTeam false)")
    void matchWithNullAwayTeam_HomeTeamNoMatch_NotParticipating() {
        Match noMatch = new Match();
        noMatch.setId(30L);
        noMatch.setStatus("FINALIZADO");
        noMatch.setHomeTeam(team2);  // homeTeam es team2, no team1 → homeTeam.getId()!=1L → false
        noMatch.setAwayTeam(null);  // awayTeam == null → rama false de awayTeam != null

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(noMatch));

        StandingDTO result = statsService.getTeamStats(1L);
        assertEquals(3, result.getPoints(), "team1 no participó en el partido → sin FairPlay");
        verifyNoInteractions(matchEventRepository);
    }

    @Test
    @DisplayName("Partido con homeTeam≠equipo y awayTeam≠equipo (ambos no-null) → no participa")
    void matchBothTeamsNotNull_NeitherMatchesTeam_NotParticipating() {
        // homeTeam=team2, awayTeam=team2 → A=true, B=false, C=true, D=false → false
        Match noMatch = new Match();
        noMatch.setId(40L);
        noMatch.setStatus("FINALIZADO");
        noMatch.setHomeTeam(team2);
        noMatch.setAwayTeam(team2);  // awayTeam existe pero tampoco es team1

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(noMatch));

        StandingDTO result = statsService.getTeamStats(1L);
        assertEquals(3, result.getPoints(), "team1 no participó en ningún rol → sin FairPlay");
        verifyNoInteractions(matchEventRepository);
    }

    @Test
    @DisplayName("Evento GOL en match (AMARILLA=false, ROJA=false) → filtrado fuera, sin impacto FairPlay")
    void golEvent_FilteredOutByCardCheck_FairPlayAwarded() {
        StudentPlayer goalScorer = new StudentPlayer();
        goalScorer.setId(55L);
        goalScorer.setTeamId(1L);

        MatchEvent golEvent = new MatchEvent();
        golEvent.setPlayerId(55L);
        golEvent.setType("GOL"); // AMARILLA=false, ROJA=false → filter false → evento excluido

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(finishedMatch));
        when(matchEventRepository.findByMatchId(10L)).thenReturn(List.of(golEvent));
        // playerRepo NO es llamado porque el GOL no pasa el filtro de tarjetas

        StandingDTO result = statsService.getTeamStats(1L);
        assertEquals(4, result.getPoints(), "3 pts + 1 FairPlay: GOL no es tarjeta");
        verifyNoInteractions(userRepository);
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
        cleanMatch.setStatus("Finalizado"); // case canónico intencional (coexiste con FINALIZADO del setUp)
        cleanMatch.setHomeTeam(team1);
        cleanMatch.setAwayTeam(team2);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team1));
        when(matchRepository.findAll()).thenReturn(List.of(finishedMatch, cleanMatch));
        when(matchEventRepository.findByMatchId(10L)).thenReturn(List.of(card));  // partido sucio
        when(matchEventRepository.findByMatchId(11L)).thenReturn(List.of());       // partido limpio
        when(userRepository.findById(55L)).thenReturn(Optional.of(playerWithCard));

        StandingDTO result = statsService.getTeamStats(1L);

        assertEquals(4, result.getPoints(), "3 pts base + 1 FairPlay del partido limpio = 4");
    }
}
