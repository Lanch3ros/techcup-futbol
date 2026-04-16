package com.example.controller.mapper;

import com.example.controller.dto.request.TeamCreationRequest;
import com.example.core.model.Team;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeamMapperTest {

    private final TeamMapper teamMapper = new TeamMapper();

    @Test
    void toEntity_Success() {
        TeamCreationRequest request = new TeamCreationRequest();
        request.setName("Ingeniería FC");
        request.setColors("Rojo y Blanco");

        Team result = teamMapper.toEntity(request);

        assertNotNull(result);
        assertEquals("Ingeniería FC", result.getName());
        assertEquals("Rojo y Blanco", result.getColors());
    }

    @Test
    void toEntity_NullInput_ReturnsNull() {
        Team result = teamMapper.toEntity(null);
        assertNull(result);
    }
}