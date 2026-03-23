package com.example.repository;

import com.example.core.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamRepositoryTest {

    private TeamRepository teamRepository;

    @BeforeEach
    void setUp() {
        teamRepository = new TeamRepository();
    }

    @Test
    void save_NewTeam_AssignsId() {
        Team team = new Team();
        team.setName("Equipo Nuevo");

        Team savedTeam = teamRepository.save(team);

        assertNotNull(savedTeam.getId());
        assertEquals(1L, savedTeam.getId());
        assertEquals("Equipo Nuevo", savedTeam.getName());
    }

    @Test
    void save_ExistingTeam_UpdatesWithoutChangingId() {
        Team team = new Team();
        team.setId(10L);
        team.setName("Equipo Existente");

        Team savedTeam = teamRepository.save(team);

        assertEquals(10L, savedTeam.getId());
        assertEquals("Equipo Existente", savedTeam.getName());
    }

    @Test
    void findAll_ReturnsAllSavedTeams() {
        teamRepository.save(new Team());
        teamRepository.save(new Team());

        List<Team> teams = teamRepository.findAll();

        assertEquals(2, teams.size());
    }

    @Test
    void findById_ExistingId_ReturnsTeam() {
        Team team = new Team();
        teamRepository.save(team);

        Team foundTeam = teamRepository.findById(1L);

        assertNotNull(foundTeam);
        assertEquals(1L, foundTeam.getId());
    }

    @Test
    void findById_NonExistingId_ReturnsNull() {
        Team foundTeam = teamRepository.findById(99L);

        assertNull(foundTeam);
    }
}