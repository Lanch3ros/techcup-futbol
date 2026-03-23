package com.example.core.service;

import com.example.core.model.Team;
import com.example.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TeamServiceTest {

    private TeamRepository teamRepository;
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamRepository = Mockito.mock(TeamRepository.class);
        teamService = new TeamService(teamRepository);
    }

    @Test
    void createTeam_Success() {
        Team inputTeam = new Team();
        inputTeam.setName("FC Backend");
        inputTeam.setColors("Negro y Verde");

        Team savedTeam = new Team();
        savedTeam.setId(1L);
        savedTeam.setName("FC Backend");

        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);

        Team result = teamService.createTeam(inputTeam);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("FC Backend", result.getName());
    }

    @Test
    void getAllTeams_ReturnsList() {
        when(teamRepository.findAll()).thenReturn(List.of(new Team(), new Team()));

        List<Team> result = teamService.getAllTeams();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getTeamById_Found_ReturnsTeam() {
        Team mockTeam = new Team();
        mockTeam.setId(1L);
        mockTeam.setName("Sistemas FC");

        when(teamRepository.findById(1L)).thenReturn(mockTeam);

        Team result = teamService.getTeamById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Sistemas FC", result.getName());
    }

    @Test
    void getTeamById_NotFound_ReturnsNull() {
        when(teamRepository.findById(99L)).thenReturn(null);

        Team result = teamService.getTeamById(99L);

        assertNull(result);
    }
}