package com.example.controller.mapper;

import com.example.controller.dto.request.TournamentCreationRequest;
import com.example.core.model.Tournament;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TournamentMapperTest {

    private final TournamentMapper tournamentMapper = new TournamentMapper();

    @Test
    void toEntity_Success() {
        TournamentCreationRequest request = new TournamentCreationRequest();
        request.setStartDate(LocalDate.of(2026, 4, 1));
        request.setEndDate(LocalDate.of(2026, 6, 1));
        request.setTeamCost(500000.0);
        request.setNumberOfTeams(8);
        request.setRules("Reglas estándar");

        Tournament result = tournamentMapper.toEntity(request);

        assertNotNull(result);
        assertEquals(LocalDate.of(2026, 4, 1), result.getStartDate());
        assertEquals(500000.0, result.getTeamCost());
        assertEquals(8, result.getMaxTeams());
        assertEquals("Reglas estándar", result.getRegulations());
        assertEquals("Borrador", result.getStatus());
    }

    @Test
    void toEntity_NullInput_ReturnsNull() {
        Tournament result = tournamentMapper.toEntity(null);
        assertNull(result);
    }
}