package com.example.core.service;

import com.example.core.exception.BusinessRuleException;
import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Invitation;
import com.example.core.model.Player;
import com.example.core.model.StudentPlayer;
import com.example.core.model.Team;
import com.example.core.model.Tournament;
import com.example.core.model.User;
import com.example.repository.InvitationRepository;
import com.example.repository.TournamentRepository;
import com.example.repository.UserRepository;
import com.example.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TeamService – Métodos extendidos (jugadores, alineación, invitaciones, removePlayer)")
class TeamServiceExtendedTest {

    private TeamRepository teamRepository;
    private UserRepository userRepository;
    private InvitationRepository invitationRepository;
    private TournamentRepository tournamentRepository;
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamRepository       = mock(TeamRepository.class);
        userRepository     = mock(UserRepository.class);
        invitationRepository = mock(InvitationRepository.class);
        tournamentRepository = mock(TournamentRepository.class);
        teamService = new TeamService(teamRepository, userRepository, invitationRepository, tournamentRepository);
    }

    private StudentPlayer player(long id, boolean available, Long teamId) {
        StudentPlayer p = new StudentPlayer();
        p.setId(id);
        p.setFullName("Jugador " + id);
        p.setAvailable(available);
        p.setTeamId(teamId);
        return p;
    }

    private Team team(long id) {
        Team t = new Team();
        t.setId(id);
        t.setName("Team " + id);
        return t;
    }

    // ── getTeamPlayers ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTeamPlayers – devuelve jugadores del equipo")
    void getTeamPlayers_ReturnsList() {
        Team t = team(1L);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(t));
        when(userRepository.findByTeamId(1L)).thenReturn(
                List.of(player(1L, false, 1L), player(2L, false, 1L)));

        List<Player> result = teamService.getTeamPlayers(1L);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("getTeamPlayers – equipo no encontrado → ResourceNotFoundException")
    void getTeamPlayers_TeamNotFound() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> teamService.getTeamPlayers(99L));
    }

    // ── configureLineup – equipo no encontrado ────────────────────────────────

    @Test
    @DisplayName("configureLineup – equipo no encontrado → ResourceNotFoundException")
    void configureLineup_TeamNotFound() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        com.example.controller.dto.request.LineupRequest req = new com.example.controller.dto.request.LineupRequest();
        req.setStartingPlayersIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L));
        req.setFormation("4-3-3");

        assertThrows(ResourceNotFoundException.class, () -> teamService.configureLineup(99L, req));
    }

    // ── getTeamLineup ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTeamLineup – equipo sin alineación configurada → retorna null")
    void getTeamLineup_NoLineup_ReturnsNull() {
        Team t = team(1L);
        t.setStartingPlayerIds(null);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(t));
        assertNull(teamService.getTeamLineup(1L));
    }

    @Test
    @DisplayName("getTeamLineup – lista de titulares no-null pero vacía → retorna null (rama isEmpty=true)")
    void getTeamLineup_EmptyStartingList_ReturnsNull() {
        Team t = team(1L);
        t.setStartingPlayerIds(new java.util.ArrayList<>()); // no-null pero vacía
        when(teamRepository.findById(1L)).thenReturn(Optional.of(t));
        assertNull(teamService.getTeamLineup(1L));
    }

    @Test
    @DisplayName("getTeamLineup – alineación configurada con reservePlayerIds null → retorna lista vacía de suplentes")
    void getTeamLineup_WithLineup_NullReserves_ReturnsEmptyList() {
        Team t = team(1L);
        t.setStartingPlayerIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L));
        t.setReservePlayerIds(null); // fuerza la rama null de reservePlayerIds
        t.setFormation("4-4-2");
        when(teamRepository.findById(1L)).thenReturn(Optional.of(t));

        com.example.controller.dto.request.LineupRequest result = teamService.getTeamLineup(1L);

        assertNotNull(result);
        assertNotNull(result.getReservePlayerIds()); // usa new ArrayList<>() como fallback
        assertTrue(result.getReservePlayerIds().isEmpty());
    }

    @Test
    @DisplayName("getTeamLineup – alineación configurada → retorna LineupRequest con titulares y suplentes")
    void getTeamLineup_WithLineup_ReturnsRequest() {
        Team t = team(1L);
        t.setStartingPlayerIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L));
        t.setReservePlayerIds(List.of(8L, 9L));
        t.setFormation("4-3-3");
        when(teamRepository.findById(1L)).thenReturn(Optional.of(t));

        com.example.controller.dto.request.LineupRequest result = teamService.getTeamLineup(1L);

        assertNotNull(result);
        assertEquals(7, result.getStartingPlayersIds().size());
        assertEquals(2, result.getReservePlayerIds().size());
        assertEquals("4-3-3", result.getFormation());
    }

    // ── removePlayer ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("removePlayer – jugador no encontrado → ResourceNotFoundException")
    void removePlayer_PlayerNotFound() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team(1L)));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamService.removePlayer(1L, 99L));
    }

    @Test
    @DisplayName("removePlayer – jugador no pertenece al equipo → ResourceNotFoundException")
    void removePlayer_PlayerNotInTeam() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team(1L)));
        StudentPlayer p = player(5L, false, 99L); // pertenece al equipo 99, no al 1
        when(userRepository.findById(5L)).thenReturn(Optional.of(p));

        assertThrows(ResourceNotFoundException.class, () -> teamService.removePlayer(1L, 5L));
    }

    @Test
    @DisplayName("removePlayer – éxito → jugador disponible y sin equipo")
    void removePlayer_Success() {
        Team t = team(1L);
        StudentPlayer p = player(5L, false, 1L);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(t));
        when(userRepository.findById(5L)).thenReturn(Optional.of(p));
        when(userRepository.save(any())).thenReturn(p);

        assertDoesNotThrow(() -> teamService.removePlayer(1L, 5L));
        assertTrue(p.isAvailable());
        assertNull(p.getTeamId());
        verify(userRepository).save(p);
    }

    // ── sendInvitation ────────────────────────────────────────────────────────

    @Test
    @DisplayName("sendInvitation – equipo no encontrado → ResourceNotFoundException")
    void sendInvitation_TeamNotFound() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> teamService.sendInvitation(99L, 1L));
    }

    @Test
    @DisplayName("sendInvitation – jugador no encontrado → ResourceNotFoundException")
    void sendInvitation_PlayerNotFound() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team(1L)));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> teamService.sendInvitation(1L, 99L));
    }

    @Test
    @DisplayName("sendInvitation – jugador ya tiene equipo (teamId != null) → BusinessRuleException")
    void sendInvitation_PlayerAlreadyInTeam() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team(1L)));
        StudentPlayer p = player(2L, false, 7L); // ya tiene equipo ID 7
        when(userRepository.findById(2L)).thenReturn(Optional.of(p));

        assertThrows(BusinessRuleException.class, () -> teamService.sendInvitation(1L, 2L));
    }

    @Test
    @DisplayName("sendInvitation – equipo lleno (12 jugadores) → BusinessRuleException")
    void sendInvitation_TeamFull() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team(1L)));
        StudentPlayer p = player(3L, true, null);
        when(userRepository.findById(3L)).thenReturn(Optional.of(p));
        when(userRepository.countByTeamId(1L)).thenReturn(12L);

        assertThrows(BusinessRuleException.class, () -> teamService.sendInvitation(1L, 3L));
    }

    @Test
    @DisplayName("sendInvitation – invitación pendiente ya existente → BusinessRuleException")
    void sendInvitation_PendingAlreadyExists() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team(1L)));
        StudentPlayer p = player(3L, true, null);
        when(userRepository.findById(3L)).thenReturn(Optional.of(p));
        when(userRepository.countByTeamId(1L)).thenReturn(5L);
        when(invitationRepository.existsByPlayerIdAndTeamIdAndStatusIgnoreCase(3L, 1L, Invitation.PENDING))
                .thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> teamService.sendInvitation(1L, 3L));
    }

    @Test
    @DisplayName("sendInvitation – éxito → invitación persistida")
    void sendInvitation_Success() {
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team(1L)));
        StudentPlayer p = player(3L, true, null);
        when(userRepository.findById(3L)).thenReturn(Optional.of(p));
        when(userRepository.countByTeamId(1L)).thenReturn(3L);
        when(invitationRepository.existsByPlayerIdAndTeamIdAndStatusIgnoreCase(3L, 1L, Invitation.PENDING))
                .thenReturn(false);
        when(invitationRepository.save(any())).thenReturn(new Invitation());

        assertDoesNotThrow(() -> teamService.sendInvitation(1L, 3L));
        verify(invitationRepository).save(any(Invitation.class));
    }

    // ── GAP-12: Congelamiento de Plantilla ────────────────────────────────────

    private Team teamInTournament(long teamId, long tournamentId) {
        Team t = team(teamId);
        t.setTournamentId(tournamentId);
        return t;
    }

    private Tournament tournament(long id, String status) {
        Tournament t = new Tournament();
        t.setId(id);
        t.setStatus(status);
        return t;
    }

    @Test
    @DisplayName("removePlayer – torneo En progreso → BusinessRuleException (plantilla congelada)")
    void removePlayer_TournamentInProgress_Throws() {
        Team t = teamInTournament(1L, 10L);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(t));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament(10L, "En progreso")));

        assertThrows(BusinessRuleException.class, () -> teamService.removePlayer(1L, 5L));
    }

    @Test
    @DisplayName("removePlayer – torneo no En progreso → permite modificar plantilla")
    void removePlayer_TournamentNotInProgress_Allowed() {
        Team t = teamInTournament(1L, 10L);
        StudentPlayer p = player(5L, false, 1L);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(t));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament(10L, "Inscripciones")));
        when(userRepository.findById(5L)).thenReturn(Optional.of(p));
        when(userRepository.save(any())).thenReturn(p);

        assertDoesNotThrow(() -> teamService.removePlayer(1L, 5L));
    }

    @Test
    @DisplayName("removePlayer – equipo sin torneo asignado → permite modificar plantilla")
    void removePlayer_NoTournament_Allowed() {
        Team t = team(1L); // tournamentId = null
        StudentPlayer p = player(5L, false, 1L);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(t));
        when(userRepository.findById(5L)).thenReturn(Optional.of(p));
        when(userRepository.save(any())).thenReturn(p);

        assertDoesNotThrow(() -> teamService.removePlayer(1L, 5L));
    }

    @Test
    @DisplayName("sendInvitation – torneo En progreso → BusinessRuleException (plantilla congelada)")
    void sendInvitation_TournamentInProgress_Throws() {
        Team t = teamInTournament(1L, 10L);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(t));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament(10L, "en progreso")));

        assertThrows(BusinessRuleException.class, () -> teamService.sendInvitation(1L, 3L));
    }

    @Test
    @DisplayName("sendInvitation – torneo terminado → permite enviar invitación")
    void sendInvitation_TournamentFinished_Allowed() {
        Team t = teamInTournament(1L, 10L);
        StudentPlayer p = player(3L, true, null);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(t));
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament(10L, "Finalizado")));
        when(userRepository.findById(3L)).thenReturn(Optional.of(p));
        when(userRepository.countByTeamId(1L)).thenReturn(3L);
        when(invitationRepository.existsByPlayerIdAndTeamIdAndStatusIgnoreCase(3L, 1L, Invitation.PENDING))
                .thenReturn(false);
        when(invitationRepository.save(any())).thenReturn(new Invitation());

        assertDoesNotThrow(() -> teamService.sendInvitation(1L, 3L));
    }
}
