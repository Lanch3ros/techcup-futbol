package com.example.controller;

import com.example.controller.dto.request.RefereeRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.core.model.Match;
import com.example.core.model.RefereeUser;
import com.example.core.service.RefereeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("RefereeController – /api/v1/referees")
class RefereeControllerTest {

    private RefereeService refereeService;
    private RefereeController refereeController;

    @BeforeEach
    void setUp() {
        refereeService = mock(RefereeService.class);
        refereeController = new RefereeController(refereeService);
    }

    private RefereeRequest buildRequest() {
        RefereeRequest req = new RefereeRequest();
        req.setFullName("Carlos Árbitro");
        req.setEmail("arbitro@eci.edu.co");
        req.setLicenseNumber("LIC-001");
        req.setCertificationLevel("FIFA");
        return req;
    }

    // ── createReferee ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("createReferee – éxito → 201 CREATED con árbitro en el cuerpo")
    void createReferee_Success_Returns201() {
        RefereeUser referee = new RefereeUser();
        referee.setId(1L);
        referee.setFullName("Carlos Árbitro");
        when(refereeService.createReferee(any())).thenReturn(referee);

        ResponseEntity<GenericResponse> response = refereeController.createReferee(buildRequest());

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Éxito", response.getBody().getMessage());
        assertEquals(referee, response.getBody().getData());
    }

    @Test
    @DisplayName("createReferee – excepción → 400 BAD REQUEST")
    void createReferee_Exception_Returns400() {
        when(refereeService.createReferee(any()))
                .thenThrow(new RuntimeException("Licencia duplicada"));

        ResponseEntity<GenericResponse> response = refereeController.createReferee(buildRequest());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Licencia duplicada", response.getBody().getData());
    }

    // ── getAllReferees ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllReferees → 200 OK con lista de árbitros")
    void getAllReferees_Returns200() {
        when(refereeService.getAllReferees()).thenReturn(List.of(new RefereeUser(), new RefereeUser()));

        ResponseEntity<List<RefereeUser>> response = refereeController.getAllReferees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("getAllReferees → lista vacía cuando no hay árbitros")
    void getAllReferees_EmptyList() {
        when(refereeService.getAllReferees()).thenReturn(List.of());

        ResponseEntity<List<RefereeUser>> response = refereeController.getAllReferees();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ── getRefereeById ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getRefereeById – encontrado → 200 OK con árbitro")
    void getRefereeById_Found_Returns200() {
        RefereeUser referee = new RefereeUser();
        referee.setId(1L);
        referee.setFullName("Carlos Árbitro");
        when(refereeService.getRefereeById(1L)).thenReturn(referee);

        ResponseEntity<RefereeUser> response = refereeController.getRefereeById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Carlos Árbitro", response.getBody().getFullName());
    }

    @Test
    @DisplayName("getRefereeById – no encontrado → 404 NOT FOUND")
    void getRefereeById_NotFound_Returns404() {
        when(refereeService.getRefereeById(99L))
                .thenThrow(new RuntimeException("Árbitro no encontrado"));

        ResponseEntity<RefereeUser> response = refereeController.getRefereeById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ── getRefereeMatches ─────────────────────────────────────────────────────

    @Test
    @DisplayName("getRefereeMatches – éxito → 200 OK con lista de partidos")
    void getRefereeMatches_Success_Returns200() {
        when(refereeService.getRefereeMatches(1L)).thenReturn(List.of(new Match(), new Match()));

        ResponseEntity<List<Match>> response = refereeController.getRefereeMatches(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    @Test
    @DisplayName("getRefereeMatches – excepción → 400 BAD REQUEST")
    void getRefereeMatches_Exception_Returns400() {
        when(refereeService.getRefereeMatches(99L))
                .thenThrow(new RuntimeException("Árbitro no encontrado"));

        ResponseEntity<List<Match>> response = refereeController.getRefereeMatches(99L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
