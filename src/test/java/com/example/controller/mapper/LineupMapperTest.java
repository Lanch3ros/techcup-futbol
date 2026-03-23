package com.example.controller.mapper;

import com.example.controller.dto.request.LineupRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LineupMapperTest {

    private final LineupMapper lineupMapper = new LineupMapper();

    @Test
    void toEntity_Success() {
        LineupRequest request = new LineupRequest();
        request.setStartingPlayersIds(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L));
        request.setFormation("4-3-3");

        Map<String, Object> result = lineupMapper.toEntity(request);

        assertNotNull(result);
        assertEquals("4-3-3", result.get("formation"));
        assertEquals(7, ((List<?>) result.get("startingPlayersIds")).size());
    }

    @Test
    void toEntity_NullInput_ReturnsNull() {
        Map<String, Object> result = lineupMapper.toEntity(null);
        assertNull(result);
    }
}