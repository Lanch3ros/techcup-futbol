package com.example.controller;

import com.example.controller.dto.ProfileDTO;
import com.example.controller.dto.RegistrationDTO;
import com.example.controller.dto.response.GenericResponse;
import com.example.controller.mapper.PlayerMapper;
import com.example.core.model.Player;
import com.example.core.model.StudentPlayer;
import com.example.core.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

public class PlayerControllerTest {

    private PlayerService playerService;
    private PlayerMapper playerMapper;
    private PlayerController playerController;

    @BeforeEach
    public void setUp() {
        playerService = Mockito.mock(PlayerService.class);
        playerMapper = Mockito.mock(PlayerMapper.class);

        playerController = new PlayerController(playerService, playerMapper);
    }

    @Test
    public void testRegisterPlayerSuccess() throws Exception {
        RegistrationDTO dto = new RegistrationDTO(
                "Jose Lancheros", "jose@mail.escuelaing.edu.co", "1234", "STUDENT",
                null, null, null, null, null
        );

        Mockito.when(playerService.registerPlayer(any(RegistrationDTO.class))).thenReturn(new StudentPlayer());

        ResponseEntity<GenericResponse> response = playerController.registerPlayer(dto, null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testSearchPlayerFound() {
        StudentPlayer mockPlayer = new StudentPlayer();
        mockPlayer.setFullName("Juan Perez");

        ProfileDTO mockProfile = new ProfileDTO(
                "Juan Perez", "juan@mail.escuelaing.edu.co", "STUDENT", null, null, null
        );

        Mockito.when(playerService.searchPlayer(1L)).thenReturn(mockPlayer);
        Mockito.when(playerMapper.toDto(mockPlayer)).thenReturn(mockProfile);

        ResponseEntity<ProfileDTO> response = playerController.search(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Juan Perez", response.getBody().fullName());
    }

    @Test
    public void testSearchPlayerNotFound() {
        Mockito.when(playerService.searchPlayer(99L)).thenReturn(null);

        ResponseEntity<ProfileDTO> response = playerController.search(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}