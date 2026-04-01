package com.example.repository;

import com.example.core.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class TeamRepositoryTest {

    private TeamRepository teamRepository;

    @BeforeEach
    void setUp() {
        teamRepository = Mockito.mock(TeamRepository.class);
    }

    @Test
    void save_NewTeam_AssignsId() {
        Team team = new Team();
        team.setName("Equipo Nuevo");

        Team savedTeam = new Team();
        savedTeam.setId(1L);
        savedTeam.setName("Equipo Nuevo");

        when(teamRepository.save(any(Team.class))).thenReturn(savedTeam);

        Team result = teamRepository.save(team);

        assertNotNull(result.getId());
        assertEquals(1L, result.getId());
        assertEquals("Equipo Nuevo", result.getName());
    }

    @Test
    void save_ExistingTeam_UpdatesWithoutChangingId() {
        Team team = new Team();
        team.setId(10L);
        team.setName("Equipo Existente");

        when(teamRepository.save(any(Team.class))).thenReturn(team);

        Team savedTeam = teamRepository.save(team);

        assertEquals(10L, savedTeam.getId());
        assertEquals("Equipo Existente", savedTeam.getName());
    }

    @Test
    void findAll_ReturnsAllSavedTeams() {
        when(teamRepository.findAll()).thenReturn(List.of(new Team(), new Team()));

        List<Team> teams = teamRepository.findAll();

        assertEquals(2, teams.size());
    }

    @Test
    void findById_ExistingId_ReturnsTeam() {
        Team team = new Team();
        team.setId(1L);

        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));

        Optional<Team> result = teamRepository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void findById_NonExistingId_ReturnsEmpty() {
        when(teamRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Team> result = teamRepository.findById(99L);

        assertFalse(result.isPresent());
    }
}
