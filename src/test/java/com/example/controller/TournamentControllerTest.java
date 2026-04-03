package com.example.controller;

import com.example.controller.dto.request.TournamentCreationRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.controller.dto.response.StandingDTO;
import com.example.controller.mapper.TournamentMapper;
import com.example.core.model.Match;
import com.example.core.model.Team;
import com.example.core.model.Tournament;
import com.example.core.service.StatsService;
import com.example.core.service.TournamentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("TournamentController – /api/v1/tournaments")
class TournamentControllerTest {

    private TournamentService tournamentService;
    private TournamentMapper tournamentMapper;
    private StatsService statsService;
    private TournamentController tournamentController;

    @BeforeEach
    void setUp() {
        tournamentService  = mock(TournamentService.class);
        tournamentMapper   = mock(TournamentMapper.class);
        statsService       = mock(StatsService.class);
        tournamentController = new TournamentController(tournamentService, tournamentMapper, statsService);
    }

    private TournamentCreationRequest buildRequest() {
        TournamentCreationRequest req = new TournamentCreationRequest();
        req.setStartDate(LocalDate.now().plusDays(1));
        req.setEndDate(LocalDate.now().plusMonths(2));
        req.setTeamCost(100_000.0);
        req.setNumberOfTeams(8);
        req.setRules("Reglamento del torneo");
        return req;
    }

    private Tournament buildTournament(Long id, String status) {
        Tournament t = new Tournament();
        t.setId(id);
        t.setStatus(status);
        t.setRegulations("Reglamento del torneo");
        return t;
    }

    // ── createTournament ──────────────────────────────────────────────────────

    @Test
    @DisplayName("createTournament – éxito → 201 CREATED")
    void createTournament_Success_Returns201() {
        Tournament entity = buildTournament(null, "Borrador");
        when(tournamentMapper.toEntity(any())).thenReturn(entity);
        when(tournamentService.createTournament(entity)).thenReturn(entity);

        ResponseEntity<GenericResponse> response = tournamentController.createTournament(buildRequest());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("createTournament – excepción → 400 BAD REQUEST")
    void createTournament_Exception_Returns400() {
        when(tournamentMapper.toEntity(any())).thenReturn(new Tournament());
        when(tournamentService.createTournament(any()))
                .thenThrow(new RuntimeException("Error al crear torneo"));

        ResponseEntity<GenericResponse> response = tournamentController.createTournament(buildRequest());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Error al crear torneo", response.getBody().getData());
    }

    // ── getAllTournaments ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllTournaments → 200 OK con lista de torneos")
    void getAllTournaments_Returns200() {
        when(tournamentService.getAllTournaments())
                .thenReturn(List.of(buildTournament(1L, "En progreso"), buildTournament(2L, "Borrador")));

        ResponseEntity<List<Tournament>> response = tournamentController.getAllTournaments();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    // ── getTournamentById ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getTournamentById – encontrado → 200 OK")
    void getTournamentById_Found_Returns200() {
        when(tournamentService.getTournamentById(1L)).thenReturn(buildTournament(1L, "En progreso"));

        ResponseEntity<Tournament> response = tournamentController.getTournamentById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("En progreso", response.getBody().getStatus());
    }

    @Test
    @DisplayName("getTournamentById – no encontrado → 404 NOT FOUND")
    void getTournamentById_NotFound_Returns404() {
        when(tournamentService.getTournamentById(99L)).thenThrow(new RuntimeException("No encontrado"));

        ResponseEntity<Tournament> response = tournamentController.getTournamentById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── getTournamentRules ────────────────────────────────────────────────────

    @Test
    @DisplayName("getTournamentRules – torneo encontrado → 200 OK con reglamento")
    void getTournamentRules_Found_Returns200() {
        when(tournamentService.getTournamentById(1L)).thenReturn(buildTournament(1L, "En progreso"));

        ResponseEntity<GenericResponse> response = tournamentController.getTournamentRules(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Reglamento", response.getBody().getMessage());
        assertEquals("Reglamento del torneo", response.getBody().getData());
    }

    @Test
    @DisplayName("getTournamentRules – torneo no encontrado → 404 NOT FOUND")
    void getTournamentRules_NotFound_Returns404() {
        when(tournamentService.getTournamentById(99L)).thenThrow(new RuntimeException("No encontrado"));

        ResponseEntity<GenericResponse> response = tournamentController.getTournamentRules(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── getTournamentTeams ────────────────────────────────────────────────────

    @Test
    @DisplayName("getTournamentTeams – con equipos inscritos → 200 OK")
    void getTournamentTeams_Returns200() {
        when(tournamentService.getTournamentTeams(1L)).thenReturn(List.of(new Team(), new Team()));

        ResponseEntity<List<Team>> response = tournamentController.getTournamentTeams(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("getTournamentTeams – excepción → 400 BAD REQUEST")
    void getTournamentTeams_Exception_Returns400() {
        when(tournamentService.getTournamentTeams(99L)).thenThrow(new RuntimeException("Torneo no encontrado"));

        ResponseEntity<List<Team>> response = tournamentController.getTournamentTeams(99L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── getTournamentStandings ────────────────────────────────────────────────

    @Test
    @DisplayName("getTournamentStandings – tabla generada → 200 OK")
    void getTournamentStandings_Returns200() {
        StandingDTO dto = new StandingDTO();
        dto.setTeamId(1L);
        dto.setPoints(9);
        when(statsService.getTournamentStandings(1L)).thenReturn(List.of(dto));

        ResponseEntity<List<StandingDTO>> response = tournamentController.getTournamentStandings(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(9, response.getBody().get(0).getPoints());
    }

    @Test
    @DisplayName("getTournamentStandings – excepción → 400 BAD REQUEST")
    void getTournamentStandings_Exception_Returns400() {
        when(statsService.getTournamentStandings(99L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<List<StandingDTO>> response = tournamentController.getTournamentStandings(99L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── getTournamentBracket ──────────────────────────────────────────────────

    @Test
    @DisplayName("getTournamentBracket – con partidos → 200 OK con llaves")
    void getTournamentBracket_WithMatches_Returns200() {
        Tournament t = buildTournament(1L, "En progreso");
        t.setMatches(List.of(new Match(), new Match()));
        when(tournamentService.getTournamentById(1L)).thenReturn(t);

        ResponseEntity<GenericResponse> response = tournamentController.getTournamentBracket(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Llaves", response.getBody().getMessage());
    }

    @Test
    @DisplayName("getTournamentBracket – sin partidos generados → 200 OK con mensaje informativo")
    void getTournamentBracket_NoMatches_Returns200WithInfo() {
        Tournament t = buildTournament(1L, "Borrador");
        t.setMatches(List.of());
        when(tournamentService.getTournamentById(1L)).thenReturn(t);

        ResponseEntity<GenericResponse> response = tournamentController.getTournamentBracket(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Info", response.getBody().getMessage());
    }

    @Test
    @DisplayName("getTournamentBracket – matches null → 200 OK con mensaje informativo")
    void getTournamentBracket_NullMatches_Returns200WithInfo() {
        Tournament t = buildTournament(1L, "Borrador");
        t.setMatches(null);
        when(tournamentService.getTournamentById(1L)).thenReturn(t);

        ResponseEntity<GenericResponse> response = tournamentController.getTournamentBracket(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Info", response.getBody().getMessage());
    }

    @Test
    @DisplayName("getTournamentBracket – excepción → 400 BAD REQUEST")
    void getTournamentBracket_Exception_Returns400() {
        when(tournamentService.getTournamentById(99L)).thenThrow(new RuntimeException("No encontrado"));

        ResponseEntity<GenericResponse> response = tournamentController.getTournamentBracket(99L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── registerTeam ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerTeam – éxito → 200 OK")
    void registerTeam_Success_Returns200() {
        doNothing().when(tournamentService).registerTeamToTournament(1L, 5L);

        ResponseEntity<GenericResponse> response = tournamentController.registerTeam(1L, 5L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("registerTeam – excepción → 400 BAD REQUEST")
    void registerTeam_Exception_Returns400() {
        doThrow(new RuntimeException("Torneo lleno"))
                .when(tournamentService).registerTeamToTournament(1L, 5L);

        ResponseEntity<GenericResponse> response = tournamentController.registerTeam(1L, 5L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Torneo lleno", response.getBody().getData());
    }

    // ── generateMatches ───────────────────────────────────────────────────────

    @Test
    @DisplayName("generateMatches – éxito → 200 OK con cantidad de partidos")
    void generateMatches_Success_Returns200() {
        when(tournamentService.generateMatches(1L))
                .thenReturn(List.of(new Match(), new Match(), new Match()));

        ResponseEntity<GenericResponse> response = tournamentController.generateMatches(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().toString().contains("3"));
    }

    @Test
    @DisplayName("generateMatches – excepción → 400 BAD REQUEST")
    void generateMatches_Exception_Returns400() {
        when(tournamentService.generateMatches(99L)).thenThrow(new RuntimeException("Torneo no encontrado"));

        ResponseEntity<GenericResponse> response = tournamentController.generateMatches(99L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── generateQuarterFinals ─────────────────────────────────────────────────

    @Test
    @DisplayName("generateQuarterFinals – éxito → 200 OK con cuartos")
    void generateQuarterFinals_Success_Returns200() {
        List<Match> qf = List.of(new Match(), new Match(), new Match(), new Match());
        when(tournamentService.generateQuarterFinals(1L)).thenReturn(qf);

        ResponseEntity<GenericResponse> response = tournamentController.generateQuarterFinals(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(qf, response.getBody().getData());
    }

    @Test
    @DisplayName("generateQuarterFinals – excepción → 400 BAD REQUEST")
    void generateQuarterFinals_Exception_Returns400() {
        when(tournamentService.generateQuarterFinals(1L)).thenThrow(new RuntimeException("Menos de 8 equipos"));

        ResponseEntity<GenericResponse> response = tournamentController.generateQuarterFinals(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── startTournament ───────────────────────────────────────────────────────

    @Test
    @DisplayName("startTournament – éxito → 200 OK")
    void startTournament_Success_Returns200() {
        Tournament t = buildTournament(1L, "En progreso");
        when(tournamentService.startTournament(1L)).thenReturn(t);

        ResponseEntity<GenericResponse> response = tournamentController.startTournament(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("startTournament – excepción → 400 BAD REQUEST")
    void startTournament_Exception_Returns400() {
        when(tournamentService.startTournament(1L))
                .thenThrow(new RuntimeException("Estado no permitido"));

        ResponseEntity<GenericResponse> response = tournamentController.startTournament(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Estado no permitido", response.getBody().getData());
    }

    // ── finishTournament ──────────────────────────────────────────────────────

    @Test
    @DisplayName("finishTournament – éxito → 200 OK")
    void finishTournament_Success_Returns200() {
        Tournament t = buildTournament(1L, "Finalizado");
        when(tournamentService.finishTournament(1L)).thenReturn(t);

        ResponseEntity<GenericResponse> response = tournamentController.finishTournament(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("finishTournament – excepción → 400 BAD REQUEST")
    void finishTournament_Exception_Returns400() {
        when(tournamentService.finishTournament(1L))
                .thenThrow(new RuntimeException("No está en progreso"));

        ResponseEntity<GenericResponse> response = tournamentController.finishTournament(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("No está en progreso", response.getBody().getData());
    }

    // ── updateTournamentStatus ────────────────────────────────────────────────

    @Test
    @DisplayName("updateTournamentStatus – status válido → 200 OK")
    void updateTournamentStatus_Valid_Returns200() {
        Map<String, String> payload = Map.of("status", "En progreso");
        doNothing().when(tournamentService).updateTournamentStatus(1L, "En progreso");

        ResponseEntity<GenericResponse> response = tournamentController.updateTournamentStatus(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().toString().contains("En progreso"));
    }

    @Test
    @DisplayName("updateTournamentStatus – status null → 400 BAD REQUEST")
    void updateTournamentStatus_NullStatus_Returns400() {
        Map<String, String> payload = new HashMap<>();
        payload.put("status", null);

        ResponseEntity<GenericResponse> response = tournamentController.updateTournamentStatus(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El campo 'status' es obligatorio", response.getBody().getData());
    }

    @Test
    @DisplayName("updateTournamentStatus – status en blanco → 400 BAD REQUEST")
    void updateTournamentStatus_BlankStatus_Returns400() {
        Map<String, String> payload = Map.of("status", "   ");

        ResponseEntity<GenericResponse> response = tournamentController.updateTournamentStatus(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updateTournamentStatus – excepción de servicio → 400 BAD REQUEST")
    void updateTournamentStatus_ServiceException_Returns400() {
        Map<String, String> payload = Map.of("status", "Finalizado");
        doThrow(new RuntimeException("Transición no permitida"))
                .when(tournamentService).updateTournamentStatus(1L, "Finalizado");

        ResponseEntity<GenericResponse> response = tournamentController.updateTournamentStatus(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
