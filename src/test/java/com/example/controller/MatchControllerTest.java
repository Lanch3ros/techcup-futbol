package com.example.controller;

import com.example.controller.dto.request.MatchCreationRequest;
import com.example.controller.dto.request.MatchEventRequest;
import com.example.controller.dto.request.MatchResultRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.core.model.Match;
import com.example.core.model.MatchEvent;
import com.example.core.service.MatchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("MatchController – /api/v1/matches")
class MatchControllerTest {

    private MatchService matchService;
    private MatchController matchController;

    @BeforeEach
    void setUp() {
        matchService = mock(MatchService.class);
        matchController = new MatchController(matchService);
    }

    // ── createMatch ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("createMatch – éxito → 201 CREATED con partido en el cuerpo")
    void createMatch_Success_Returns201() {
        MatchCreationRequest req = new MatchCreationRequest();
        req.setHomeTeamId(1L);
        req.setAwayTeamId(2L);
        req.setMatchDate(LocalDateTime.now().plusDays(1));

        Match match = new Match();
        match.setId(10L);
        when(matchService.createMatch(any())).thenReturn(match);

        ResponseEntity<GenericResponse> response = matchController.createMatch(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Éxito", response.getBody().getMessage());
        assertEquals(match, response.getBody().getData());
    }

    @Test
    @DisplayName("createMatch – excepción → 400 BAD REQUEST")
    void createMatch_Exception_Returns400() {
        MatchCreationRequest req = new MatchCreationRequest();
        req.setHomeTeamId(1L);
        req.setAwayTeamId(2L);
        req.setMatchDate(LocalDateTime.now().plusDays(1));

        when(matchService.createMatch(any())).thenThrow(new RuntimeException("Error al crear partido"));

        ResponseEntity<GenericResponse> response = matchController.createMatch(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error", response.getBody().getMessage());
        assertEquals("Error al crear partido", response.getBody().getData());
    }

    // ── getAllMatches ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllMatches → 200 OK con lista completa")
    void getAllMatches_Returns200() {
        when(matchService.getAllMatches()).thenReturn(List.of(new Match(), new Match()));

        ResponseEntity<List<Match>> response = matchController.getAllMatches();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    // ── getMatchById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMatchById – encontrado → 200 OK")
    void getMatchById_Found_Returns200() {
        Match match = new Match();
        match.setId(1L);
        match.setStatus("Programado");
        when(matchService.getMatchById(1L)).thenReturn(match);

        ResponseEntity<Match> response = matchController.getMatchById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Programado", response.getBody().getStatus());
    }

    @Test
    @DisplayName("getMatchById – no encontrado → 404 NOT FOUND")
    void getMatchById_NotFound_Returns404() {
        when(matchService.getMatchById(99L)).thenThrow(new RuntimeException("No encontrado"));

        ResponseEntity<Match> response = matchController.getMatchById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── updateMatchStatus ─────────────────────────────────────────────────────

    @Test
    @DisplayName("updateMatchStatus – status válido → 200 OK")
    void updateMatchStatus_Valid_Returns200() {
        Map<String, String> payload = Map.of("status", "En Curso");
        doNothing().when(matchService).updateMatchStatus(1L, "En Curso");

        ResponseEntity<GenericResponse> response = matchController.updateMatchStatus(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updateMatchStatus – status null → 400 BAD REQUEST")
    void updateMatchStatus_NullStatus_Returns400() {
        Map<String, String> payload = new HashMap<>();
        payload.put("status", null);

        ResponseEntity<GenericResponse> response = matchController.updateMatchStatus(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El campo 'status' es obligatorio", response.getBody().getData());
    }

    @Test
    @DisplayName("updateMatchStatus – status en blanco → 400 BAD REQUEST")
    void updateMatchStatus_BlankStatus_Returns400() {
        Map<String, String> payload = Map.of("status", "   ");

        ResponseEntity<GenericResponse> response = matchController.updateMatchStatus(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updateMatchStatus – excepción de servicio → 400 BAD REQUEST")
    void updateMatchStatus_ServiceException_Returns400() {
        Map<String, String> payload = Map.of("status", "Finalizado");
        doThrow(new RuntimeException("Transición inválida"))
                .when(matchService).updateMatchStatus(1L, "Finalizado");

        ResponseEntity<GenericResponse> response = matchController.updateMatchStatus(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Transición inválida", response.getBody().getData());
    }

    // ── registerResult ────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerResult – éxito → 200 OK")
    void registerResult_Success_Returns200() {
        MatchResultRequest req = new MatchResultRequest();
        req.setHomeGoals(2);
        req.setAwayGoals(1);
        doNothing().when(matchService).registerResult(eq(1L), any());

        ResponseEntity<GenericResponse> response = matchController.registerResult(1L, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("registerResult – excepción (partido no finalizado) → 400")
    void registerResult_Exception_Returns400() {
        MatchResultRequest req = new MatchResultRequest();
        req.setHomeGoals(1);
        req.setAwayGoals(0);
        doThrow(new RuntimeException("El partido no está finalizado"))
                .when(matchService).registerResult(eq(1L), any());

        ResponseEntity<GenericResponse> response = matchController.registerResult(1L, req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El partido no está finalizado", response.getBody().getData());
    }

    // ── registerEvent ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerEvent – éxito → 201 CREATED con evento")
    void registerEvent_Success_Returns201() {
        MatchEventRequest req = new MatchEventRequest();
        req.setPlayerId(5L);
        req.setType("GOL");
        req.setMinute(30);

        MatchEvent event = new MatchEvent();
        event.setId(1L);
        when(matchService.registerEvent(eq(1L), any())).thenReturn(event);

        ResponseEntity<GenericResponse> response = matchController.registerEvent(1L, req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(event, response.getBody().getData());
    }

    @Test
    @DisplayName("registerEvent – excepción → 400 BAD REQUEST")
    void registerEvent_Exception_Returns400() {
        MatchEventRequest req = new MatchEventRequest();
        req.setPlayerId(5L);
        req.setType("GOL");
        req.setMinute(90);
        when(matchService.registerEvent(eq(1L), any())).thenThrow(new RuntimeException("Partido inválido"));

        ResponseEntity<GenericResponse> response = matchController.registerEvent(1L, req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── getMatchEvents ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMatchEvents – éxito → 200 OK con lista")
    void getMatchEvents_Success_Returns200() {
        List<MatchEvent> events = List.of(new MatchEvent(), new MatchEvent());
        when(matchService.getMatchEvents(1L)).thenReturn(events);

        ResponseEntity<List<MatchEvent>> response = matchController.getMatchEvents(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("getMatchEvents – excepción → 400 BAD REQUEST")
    void getMatchEvents_Exception_Returns400() {
        when(matchService.getMatchEvents(99L)).thenThrow(new RuntimeException("Partido no encontrado"));

        ResponseEntity<List<MatchEvent>> response = matchController.getMatchEvents(99L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── getMatchLineups ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getMatchLineups – éxito → 200 OK")
    void getMatchLineups_Success_Returns200() {
        Match match = new Match();
        match.setId(1L);
        match.setLineups(List.of());
        when(matchService.getMatchById(1L)).thenReturn(match);

        ResponseEntity<GenericResponse> response = matchController.getMatchLineups(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Alineaciones", response.getBody().getMessage());
    }

    @Test
    @DisplayName("getMatchLineups – excepción → 400 BAD REQUEST")
    void getMatchLineups_Exception_Returns400() {
        when(matchService.getMatchById(99L)).thenThrow(new RuntimeException("No encontrado"));

        ResponseEntity<GenericResponse> response = matchController.getMatchLineups(99L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── assignReferee ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("assignReferee – refereeId presente → 200 OK")
    void assignReferee_Valid_Returns200() {
        Map<String, Long> payload = Map.of("refereeId", 3L);
        doNothing().when(matchService).assignReferee(1L, 3L);

        ResponseEntity<GenericResponse> response = matchController.assignReferee(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("assignReferee – refereeId null → 400 BAD REQUEST")
    void assignReferee_NullRefereeId_Returns400() {
        Map<String, Long> payload = new HashMap<>();
        payload.put("refereeId", null);

        ResponseEntity<GenericResponse> response = matchController.assignReferee(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El campo 'refereeId' es obligatorio", response.getBody().getData());
    }

    @Test
    @DisplayName("assignReferee – excepción de servicio → 400 BAD REQUEST")
    void assignReferee_Exception_Returns400() {
        Map<String, Long> payload = Map.of("refereeId", 99L);
        doThrow(new RuntimeException("Árbitro no encontrado"))
                .when(matchService).assignReferee(1L, 99L);

        ResponseEntity<GenericResponse> response = matchController.assignReferee(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Árbitro no encontrado", response.getBody().getData());
    }
}
