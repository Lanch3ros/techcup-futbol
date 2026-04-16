package com.example.controller;

import com.example.controller.dto.request.LineupRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.controller.dto.response.ProfileDTO;
import com.example.controller.mapper.PlayerMapper;
import com.example.controller.mapper.TeamMapper;
import com.example.core.model.Player;
import com.example.core.model.StudentPlayer;
import com.example.core.model.Team;
import com.example.core.service.TeamService;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TeamController – Métodos extendidos (players, lineup, remove, invite, payment, configureLineup)")
class TeamControllerExtendedTest {

    private TeamService teamService;
    private TeamMapper teamMapper;
    private PlayerMapper playerMapper;
    private TeamController teamController;

    @BeforeEach
    void setUp() {
        teamService  = mock(TeamService.class);
        teamMapper   = mock(TeamMapper.class);
        playerMapper = mock(PlayerMapper.class);
        teamController = new TeamController(teamService, teamMapper, playerMapper);
    }

    private ProfileDTO profile(String name) {
        return new ProfileDTO(null, name, name.toLowerCase() + "@eci.edu.co", "STUDENT", null, null, null, null, null, null, null, null, null);
    }

    // ── getTeamPlayers ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTeamPlayers – equipo con jugadores → 200 OK con lista de perfiles")
    void getTeamPlayers_WithPlayers_Returns200() {
        StudentPlayer p = new StudentPlayer();
        when(teamService.getTeamPlayers(1L)).thenReturn(List.of(p));
        when(playerMapper.toDto(any(Player.class))).thenReturn(profile("Ana"));

        ResponseEntity<List<ProfileDTO>> response = teamController.getTeamPlayers(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("getTeamPlayers – lista null → 200 OK con lista vacía")
    void getTeamPlayers_NullList_Returns200Empty() {
        when(teamService.getTeamPlayers(1L)).thenReturn(null);

        ResponseEntity<List<ProfileDTO>> response = teamController.getTeamPlayers(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("getTeamPlayers – excepción → 400 BAD REQUEST")
    void getTeamPlayers_Exception_Returns400() {
        when(teamService.getTeamPlayers(99L)).thenThrow(new RuntimeException("Equipo no encontrado"));

        ResponseEntity<List<ProfileDTO>> response = teamController.getTeamPlayers(99L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── getTeamLineup ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTeamLineup – alineación configurada → 200 OK con alineación")
    void getTeamLineup_WithLineup_Returns200() {
        LineupRequest lineup = new LineupRequest();
        lineup.setFormation("4-3-3");
        lineup.setStartingPlayersIds(new ArrayList<>(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L)));
        when(teamService.getTeamLineup(1L)).thenReturn(lineup);

        ResponseEntity<GenericResponse> response = teamController.getTeamLineup(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
        assertEquals(lineup, response.getBody().getData());

    }

    @Test
    @DisplayName("getTeamLineup – sin alineación (null) → 200 OK con mensaje informativo")
    void getTeamLineup_NullLineup_Returns200WithInfo() {
        when(teamService.getTeamLineup(1L)).thenReturn(null);

        ResponseEntity<GenericResponse> response = teamController.getTeamLineup(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Info", response.getBody().getMessage());
    }

    @Test
    @DisplayName("getTeamLineup – excepción → 400 BAD REQUEST")
    void getTeamLineup_Exception_Returns400() {
        when(teamService.getTeamLineup(99L)).thenThrow(new RuntimeException("Equipo no encontrado"));

        ResponseEntity<GenericResponse> response = teamController.getTeamLineup(99L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── removePlayer ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("removePlayer – éxito → 200 OK")
    void removePlayer_Success_Returns200() {
        doNothing().when(teamService).removePlayer(1L, 5L);

        ResponseEntity<GenericResponse> response = teamController.removePlayer(1L, 5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("removePlayer – excepción → 400 BAD REQUEST")
    void removePlayer_Exception_Returns400() {
        doThrow(new RuntimeException("Jugador no en el equipo"))
                .when(teamService).removePlayer(1L, 99L);

        ResponseEntity<GenericResponse> response = teamController.removePlayer(1L, 99L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Jugador no en el equipo", response.getBody().getData());
    }

    // ── sendInvitation ────────────────────────────────────────────────────────

    @Test
    @DisplayName("sendInvitation – playerId presente → 200 OK")
    void sendInvitation_Valid_Returns200() {
        Map<String, Long> payload = Map.of("playerId", 5L);
        doNothing().when(teamService).sendInvitation(1L, 5L);

        ResponseEntity<GenericResponse> response = teamController.sendInvitation(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("sendInvitation – playerId null → 400 BAD REQUEST")
    void sendInvitation_NullPlayerId_Returns400() {
        Map<String, Long> payload = new HashMap<>();
        payload.put("playerId", null);

        ResponseEntity<GenericResponse> response = teamController.sendInvitation(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El campo 'playerId' es obligatorio", response.getBody().getData());
    }

    @Test
    @DisplayName("sendInvitation – excepción de servicio → 400 BAD REQUEST")
    void sendInvitation_Exception_Returns400() {
        Map<String, Long> payload = Map.of("playerId", 5L);
        doThrow(new RuntimeException("Ya existe invitación pendiente"))
                .when(teamService).sendInvitation(1L, 5L);

        ResponseEntity<GenericResponse> response = teamController.sendInvitation(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── updatePaymentStatus ───────────────────────────────────────────────────

    @Test
    @DisplayName("updatePaymentStatus – status válido → 200 OK")
    void updatePaymentStatus_Valid_Returns200() {
        Team team = new Team();
        team.setId(1L);
        when(teamService.getTeamById(1L)).thenReturn(team);
        when(teamService.createTeam(any())).thenReturn(team);

        Map<String, String> payload = Map.of("status", "Aprobado");

        ResponseEntity<GenericResponse> response = teamController.updatePaymentStatus(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updatePaymentStatus – status null → 400 BAD REQUEST")
    void updatePaymentStatus_NullStatus_Returns400() {
        Map<String, String> payload = new HashMap<>();
        payload.put("status", null);

        ResponseEntity<GenericResponse> response = teamController.updatePaymentStatus(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El campo 'status' es obligatorio", response.getBody().getData());
    }

    @Test
    @DisplayName("updatePaymentStatus – status en blanco → 400 BAD REQUEST")
    void updatePaymentStatus_BlankStatus_Returns400() {
        Map<String, String> payload = Map.of("status", "  ");

        ResponseEntity<GenericResponse> response = teamController.updatePaymentStatus(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updatePaymentStatus – excepción de servicio → 400 BAD REQUEST")
    void updatePaymentStatus_Exception_Returns400() {
        when(teamService.getTeamById(99L)).thenThrow(new RuntimeException("Equipo no encontrado"));
        Map<String, String> payload = Map.of("status", "Aprobado");

        ResponseEntity<GenericResponse> response = teamController.updatePaymentStatus(99L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── configureLineup ───────────────────────────────────────────────────────

    @Test
    @DisplayName("configureLineup – alineación válida → 200 OK")
    void configureLineup_Valid_Returns200() {
        LineupRequest req = new LineupRequest();
        req.setStartingPlayersIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L));
        req.setFormation("4-3-3");
        doNothing().when(teamService).configureLineup(1L, req);

        ResponseEntity<GenericResponse> response = teamController.configureLineup(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("configureLineup – excepción por regla de negocio → 400 BAD REQUEST")
    void configureLineup_BusinessException_Returns400() {
        LineupRequest req = new LineupRequest();
        req.setStartingPlayersIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L));
        req.setFormation("4-3-3");
        doThrow(new RuntimeException("Menos del 50% de ingeniería"))
                .when(teamService).configureLineup(1L, req);

        ResponseEntity<GenericResponse> response = teamController.configureLineup(1L, req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Menos del 50% de ingeniería", response.getBody().getData());
    }

    // ── updateShieldUrl ───────────────────────────────────────────────────────

    @Test
    @DisplayName("updateShieldUrl – URL válida → 200 OK")
    void updateShieldUrl_Valid_Returns200() {
        Team team = new Team();
        team.setId(1L);
        when(teamService.updateShieldUrl(1L, "https://cdn.example.com/shield.png")).thenReturn(team);

        Map<String, String> payload = Map.of("shieldUrl", "https://cdn.example.com/shield.png");

        ResponseEntity<GenericResponse> response = teamController.updateShieldUrl(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updateShieldUrl – shieldUrl null → 400 BAD REQUEST")
    void updateShieldUrl_NullUrl_Returns400() {
        Map<String, String> payload = new HashMap<>();
        payload.put("shieldUrl", null);

        ResponseEntity<GenericResponse> response = teamController.updateShieldUrl(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El campo 'shieldUrl' es obligatorio", response.getBody().getData());
    }

    @Test
    @DisplayName("updateShieldUrl – shieldUrl en blanco → 400 BAD REQUEST")
    void updateShieldUrl_BlankUrl_Returns400() {
        Map<String, String> payload = Map.of("shieldUrl", "   ");

        ResponseEntity<GenericResponse> response = teamController.updateShieldUrl(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updateShieldUrl – equipo no encontrado → 400 BAD REQUEST")
    void updateShieldUrl_TeamNotFound_Returns400() {
        doThrow(new RuntimeException("Equipo no encontrado"))
                .when(teamService).updateShieldUrl(99L, "https://cdn.example.com/shield.png");

        Map<String, String> payload = Map.of("shieldUrl", "https://cdn.example.com/shield.png");

        ResponseEntity<GenericResponse> response = teamController.updateShieldUrl(99L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
