package com.example.controller.mapper;

import com.example.controller.dto.ProfileDTO;
import com.example.core.model.StudentPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMapperTest {

    private final PlayerMapper playerMapper = new PlayerMapper();

    @Test
    void toDto_Success() {
        StudentPlayer player = new StudentPlayer();
        player.setFullName("Jose Lancheros");
        player.setEmail("jose@mail.escuelaing.edu.co");
        player.setProfilePhoto("foto.png");

        ProfileDTO result = playerMapper.toDto(player);

        assertNotNull(result);
        assertEquals("Jose Lancheros", result.fullName());
        assertEquals("jose@mail.escuelaing.edu.co", result.email());
        assertEquals("foto.png", result.profilePhoto());
    }

    @Test
    void toDto_NullInput_ReturnsNull() {
        ProfileDTO result = playerMapper.toDto(null);
        assertNull(result);
    }
}