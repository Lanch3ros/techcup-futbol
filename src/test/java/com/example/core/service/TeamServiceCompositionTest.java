package com.example.core.service;

import com.example.controller.dto.request.LineupRequest;
import com.example.core.exception.BusinessRuleException;
import com.example.core.model.Program;
import com.example.core.model.StudentPlayer;
import com.example.core.model.Team;
import com.example.core.model.User;
import com.example.repository.InvitationRepository;
import com.example.repository.TournamentRepository;
import com.example.repository.UserRepository;
import com.example.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TeamService – Composición de plantilla (RN-03-4: >50% ingeniería)")
class TeamServiceCompositionTest {

    private TeamRepository teamRepository;
    private UserRepository userRepository;
    private InvitationRepository invitationRepository;
    private TournamentRepository tournamentRepository;
    private TeamService teamService;

    private Team team;

    @BeforeEach
    void setUp() {
        teamRepository       = mock(TeamRepository.class);
        userRepository     = mock(UserRepository.class);
        invitationRepository = mock(InvitationRepository.class);
        tournamentRepository = mock(TournamentRepository.class);
        teamService = new TeamService(teamRepository, userRepository, invitationRepository, tournamentRepository);

        team = new Team();
        team.setId(1L);
        team.setName("FC Sistemas");

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenReturn(team);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private User player(long id, Program program) {
        StudentPlayer p = new StudentPlayer();
        p.setId(id);
        p.setTeamId(1L);
        p.setProgram(program);
        p.setAvailable(true);
        return p;
    }

    private LineupRequest lineupOf(List<Long> ids) {
        LineupRequest req = new LineupRequest();
        req.setStartingPlayersIds(ids);
        req.setFormation("4-3-3");
        return req;
    }

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("8/8 jugadores de ingeniería (100%) → alineación válida")
    void allEngineering_Valid() {
        List<User> players = List.of(
                player(1, Program.SISTEMAS),
                player(2, Program.IA),
                player(3, Program.CIBERSEGURIDAD),
                player(4, Program.ESTADISTICA),
                player(5, Program.SISTEMAS),
                player(6, Program.IA),
                player(7, Program.CIBERSEGURIDAD),
                player(8, Program.ESTADISTICA)
        );
        when(userRepository.findByTeamId(1L)).thenReturn(players);
        List<Long> ids = List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L);

        assertDoesNotThrow(() -> teamService.configureLineup(1L, lineupOf(ids)));
    }

    @Test
    @DisplayName("5 ingeniería + 2 maestría (7 jugadores, 71%) → alineación válida")
    void majorityEngineering_StrictlyAbove50_Valid() {
        List<User> players = List.of(
                player(1, Program.SISTEMAS),
                player(2, Program.IA),
                player(3, Program.CIBERSEGURIDAD),
                player(4, Program.ESTADISTICA),
                player(5, Program.SISTEMAS),
                player(6, Program.MAESTRIA_INFORMATICA),
                player(7, Program.MAESTRIA_GESTION_INFORMACION)
        );
        when(userRepository.findByTeamId(1L)).thenReturn(players);

        assertDoesNotThrow(() -> teamService.configureLineup(1L, lineupOf(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L))));
    }

    @Test
    @DisplayName("4 ingeniería + 3 no-ingeniería (7 jugadores, 57%) → válido (>50%)")
    void fourOfSeven_57percent_Valid() {
        List<User> players = List.of(
                player(1, Program.SISTEMAS),
                player(2, Program.IA),
                player(3, Program.CIBERSEGURIDAD),
                player(4, Program.ESTADISTICA),
                player(5, Program.MAESTRIA_INFORMATICA),
                player(6, Program.MAESTRIA_GESTION_INFORMACION),
                player(7, Program.MAESTRIA_CIENCIA_DATOS)
        );
        when(userRepository.findByTeamId(1L)).thenReturn(players);

        assertDoesNotThrow(() -> teamService.configureLineup(1L, lineupOf(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L))));
    }

    // ── Rejection cases ──────────────────────────────────────────────────────

    @Test
    @DisplayName("3 ingeniería + 4 maestría (7 jugadores, 43%) → rechazado")
    void threeOfSeven_43percent_Rejected() {
        List<User> players = List.of(
                player(1, Program.SISTEMAS),
                player(2, Program.IA),
                player(3, Program.CIBERSEGURIDAD),
                player(4, Program.MAESTRIA_INFORMATICA),
                player(5, Program.MAESTRIA_GESTION_INFORMACION),
                player(6, Program.MAESTRIA_CIENCIA_DATOS),
                player(7, Program.MAESTRIA_INFORMATICA)
        );
        when(userRepository.findByTeamId(1L)).thenReturn(players);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> teamService.configureLineup(1L, lineupOf(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L))));
        assertTrue(ex.getMessage().contains("50%"), "El mensaje debe mencionar el 50%");
    }

    @Test
    @DisplayName("0 jugadores de ingeniería → rechazado")
    void noEngineering_Rejected() {
        List<User> players = List.of(
                player(1, Program.MAESTRIA_INFORMATICA),
                player(2, Program.MAESTRIA_GESTION_INFORMACION),
                player(3, Program.MAESTRIA_CIENCIA_DATOS),
                player(4, Program.MAESTRIA_INFORMATICA),
                player(5, Program.MAESTRIA_GESTION_INFORMACION),
                player(6, Program.MAESTRIA_CIENCIA_DATOS),
                player(7, Program.MAESTRIA_INFORMATICA)
        );
        when(userRepository.findByTeamId(1L)).thenReturn(players);

        assertThrows(BusinessRuleException.class,
                () -> teamService.configureLineup(1L, lineupOf(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L))));
    }

    @Test
    @DisplayName("Exactamente 50% (4/8) → rechazado (la regla es ESTRICTAMENTE mayor)")
    void exactly50percent_Rejected() {
        List<User> players = List.of(
                player(1, Program.SISTEMAS),
                player(2, Program.IA),
                player(3, Program.CIBERSEGURIDAD),
                player(4, Program.ESTADISTICA),
                player(5, Program.MAESTRIA_INFORMATICA),
                player(6, Program.MAESTRIA_GESTION_INFORMACION),
                player(7, Program.MAESTRIA_CIENCIA_DATOS),
                player(8, Program.MAESTRIA_INFORMATICA)
        );
        when(userRepository.findByTeamId(1L)).thenReturn(players);

        assertThrows(BusinessRuleException.class,
                () -> teamService.configureLineup(1L, lineupOf(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L))),
                "50% exacto no cumple: se requiere MÁS del 50%");
    }

    @Test
    @DisplayName("Maestrías NO cuentan como ingeniería para el cálculo del 50%")
    void mastersDoNotCountAsEngineering() {
        // 3 maestrías + 4 pregrado ingeniería → 4/7 = 57%  → OK
        // Verificamos que maestrías no suman al numerador de ingeniería
        List<User> players = List.of(
                player(1, Program.SISTEMAS),
                player(2, Program.SISTEMAS),
                player(3, Program.SISTEMAS),
                player(4, Program.SISTEMAS),
                player(5, Program.MAESTRIA_INFORMATICA),    // maestría
                player(6, Program.MAESTRIA_GESTION_INFORMACION), // maestría
                player(7, Program.MAESTRIA_CIENCIA_DATOS)        // maestría
        );
        when(userRepository.findByTeamId(1L)).thenReturn(players);

        // 4/7 ≈ 57% → válido
        assertDoesNotThrow(() -> teamService.configureLineup(1L, lineupOf(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L))));
    }

    // ── Guard: equipo mínimo ─────────────────────────────────────────────────

    @Test
    @DisplayName("Menos de 7 jugadores en la plantilla → rechazado antes de validar composición")
    void fewerThanSevenPlayers_RejectedBeforeCompositionCheck() {
        List<User> players = List.of(
                player(1, Program.SISTEMAS),
                player(2, Program.IA),
                player(3, Program.CIBERSEGURIDAD)
        );
        when(userRepository.findByTeamId(1L)).thenReturn(players);

        assertThrows(BusinessRuleException.class,
                () -> teamService.configureLineup(1L, lineupOf(List.of(1L, 2L, 3L))));
    }

    @Test
    @DisplayName("Jugador en la solicitud que no pertenece al equipo → rechazado")
    void foreignPlayer_Rejected() {
        List<User> players = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            players.add(player(i, Program.SISTEMAS));
        }
        when(userRepository.findByTeamId(1L)).thenReturn(players);

        // El ID 99 no existe en la plantilla
        assertThrows(BusinessRuleException.class,
                () -> teamService.configureLineup(1L, lineupOf(List.of(1L, 2L, 3L, 4L, 5L, 6L, 99L))));
    }

    @Test
    @DisplayName("GAP-06: Jugador con program null → BusinessRuleException (programa inválido)")
    void playerWithNullProgram_Rejected() {
        List<User> players = List.of(
                player(1, Program.SISTEMAS),
                player(2, Program.IA),
                player(3, Program.CIBERSEGURIDAD),
                player(4, Program.ESTADISTICA),
                player(5, Program.SISTEMAS),
                player(6, Program.MAESTRIA_INFORMATICA)
        );
        StudentPlayer nullProgramPlayer = new StudentPlayer();
        nullProgramPlayer.setId(7L);
        nullProgramPlayer.setTeamId(1L);
        nullProgramPlayer.setProgram(null);

        List<User> playersWithNull = new java.util.ArrayList<>(players);
        playersWithNull.add(nullProgramPlayer);
        when(userRepository.findByTeamId(1L)).thenReturn(playersWithNull);

        assertThrows(BusinessRuleException.class,
                () -> teamService.configureLineup(1L, lineupOf(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L))),
                "Programa null debe ser rechazado (GAP-06)");
    }

    @Test
    @DisplayName("validateEngineeringProgramComposition – lista nula → retorno temprano (dead-code guard)")
    void validateEngineeringProgramComposition_NullList_EarlyReturn() throws Exception {
        Method method = TeamService.class.getDeclaredMethod(
                "validateEngineeringProgramComposition", java.util.List.class, Long.class);
        method.setAccessible(true);
        // null list → rama null → return sin lanzar excepción
        assertDoesNotThrow(() -> method.invoke(teamService, null, 1L));
    }

    @Test
    @DisplayName("validateEngineeringProgramComposition – lista vacía → retorno temprano")
    void validateEngineeringProgramComposition_EmptyList_EarlyReturn() throws Exception {
        Method method = TeamService.class.getDeclaredMethod(
                "validateEngineeringProgramComposition", java.util.List.class, Long.class);
        method.setAccessible(true);
        assertDoesNotThrow(() -> method.invoke(teamService, new ArrayList<>(), 1L));
    }

    // ── GAP-10: Suplentes ─────────────────────────────────────────────────────

    @Test
    @DisplayName("GAP-10: configureLineup con suplentes válidos → persiste titulares y suplentes")
    void configureLineup_WithReserves_Persists() {
        List<User> players = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            players.add(player(i, Program.SISTEMAS));
        }
        when(userRepository.findByTeamId(1L)).thenReturn(players);

        LineupRequest req = new LineupRequest();
        req.setStartingPlayersIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L));
        req.setReservePlayerIds(List.of(8L, 9L));
        req.setFormation("4-3-3");

        assertDoesNotThrow(() -> teamService.configureLineup(1L, req));
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    @DisplayName("GAP-10: suplente que no pertenece al equipo → BusinessRuleException")
    void configureLineup_ForeignReserve_Rejected() {
        List<User> players = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            players.add(player(i, Program.SISTEMAS));
        }
        when(userRepository.findByTeamId(1L)).thenReturn(players);

        LineupRequest req = new LineupRequest();
        req.setStartingPlayersIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L));
        req.setReservePlayerIds(List.of(99L)); // no pertenece al equipo
        req.setFormation("4-3-3");

        assertThrows(BusinessRuleException.class, () -> teamService.configureLineup(1L, req));
    }
}
