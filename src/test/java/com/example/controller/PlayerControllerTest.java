package com.example.controller;

import com.example.dto.RegistrationDTO;
import com.example.dto.response.GenericResponse;
import com.example.model.Player;
import com.example.model.StudentPlayer;
import com.example.service.PlayerService;
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
    private PlayerController playerController;

    @BeforeEach
    public void setUp() {
        playerService = Mockito.mock(PlayerService.class);

        playerController = new PlayerController(playerService);
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

        Mockito.when(playerService.searchPlayer(1L)).thenReturn(mockPlayer);

        ResponseEntity<Player> response = playerController.search(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Juan Perez", ((StudentPlayer) response.getBody()).getFullName());
    }

    @Test
    public void testSearchPlayerNotFound() {
        Mockito.when(playerService.searchPlayer(99L)).thenReturn(null);

        ResponseEntity<Player> response = playerController.search(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}