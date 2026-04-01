package com.example.core.service;

import com.example.core.exception.ResourceNotFoundException;
import com.example.core.model.Team;
import com.example.repository.PlayerRepository;
import com.example.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TeamServiceTest {

    private TeamRepository teamRepository;
    private PlayerRepository playerRepository;
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamRepository = Mockito.mock(TeamRepository.class);
        playerRepository = Mockito.mock(PlayerRepository.class);
        teamService = new TeamService(teamRepository, playerRepository);
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

        when(teamRepository.findById(1L)).thenReturn(Optional.of(mockTeam));

        Team result = teamService.getTeamById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Sistemas FC", result.getName());
    }

    @Test
    void getTeamById_NotFound_ThrowsResourceNotFoundException() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        // TeamService lanza ResourceNotFoundException cuando no encuentra el equipo
        assertThrows(ResourceNotFoundException.class, () -> teamService.getTeamById(99L));
    }
}
