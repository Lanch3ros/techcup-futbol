package com.example.controller;

import com.example.controller.dto.request.TeamCreationRequest;
import com.example.controller.dto.response.GenericResponse;
import com.example.controller.mapper.TeamMapper;
import com.example.core.model.Team;
import com.example.core.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class TeamControllerTest {

    private TeamService teamService;
    private TeamMapper teamMapper;
    private TeamController teamController;

    @BeforeEach
    void setUp() {
        teamService = Mockito.mock(TeamService.class);
        teamMapper = Mockito.mock(TeamMapper.class);
        teamController = new TeamController(teamService, teamMapper);
    }

    @Test
    void createTeam_Success_Returns201() {
        TeamCreationRequest request = new TeamCreationRequest();
        request.setName("Ingeniería FC");
        request.setColors("Rojo");

        Team mockTeam = new Team();
        when(teamMapper.toEntity(any(TeamCreationRequest.class))).thenReturn(mockTeam);
        when(teamService.createTeam(any(Team.class))).thenReturn(mockTeam);

        ResponseEntity<GenericResponse> response = teamController.createTeam(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    void createTeam_Exception_Returns400() {
        TeamCreationRequest request = new TeamCreationRequest();
        request.setName("Ingeniería FC");

        when(teamMapper.toEntity(any())).thenReturn(new Team());
        doThrow(new RuntimeException("Error en base de datos")).when(teamService).createTeam(any(Team.class));

        ResponseEntity<GenericResponse> response = teamController.createTeam(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error", response.getBody().getMessage());
    }

    @Test
    void getAllTeams_Returns200() {
        when(teamService.getAllTeams()).thenReturn(List.of(new Team(), new Team()));

        ResponseEntity<List<Team>> response = teamController.getAllTeams();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getTeamById_Found_Returns200() {
        Team mockTeam = new Team();
        mockTeam.setName("Sistemas");
        when(teamService.getTeamById(1L)).thenReturn(mockTeam);

        ResponseEntity<Team> response = teamController.getTeamById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Sistemas", response.getBody().getName());
    }

    @Test
    void getTeamById_NotFound_Returns404() {
        when(teamService.getTeamById(99L)).thenReturn(null);

        ResponseEntity<Team> response = teamController.getTeamById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}