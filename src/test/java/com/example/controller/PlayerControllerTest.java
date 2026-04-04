package com.example.controller;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.controller.dto.response.ProfileDTO;
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
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PlayerControllerTest {

    private PlayerService playerService;
    private PlayerMapper playerMapper;
    private PlayerController playerController;

    @BeforeEach
    void setUp() {
        playerService = Mockito.mock(PlayerService.class);
        playerMapper = Mockito.mock(PlayerMapper.class);
        playerController = new PlayerController(playerService, playerMapper);
    }

    @Test
    void registerPlayer_Success_Returns201() {
        PlayerRegistrationRequest request = new PlayerRegistrationRequest();
        request.setName("Jose");
        request.setEmail("jose@mail.escuelaing.edu.co");
        request.setPassword("12345678");
        request.setUserType("STUDENT");
        request.setJerseyNumber(10);
        request.setPosition("Delantero");

        MockMultipartFile file = new MockMultipartFile(
                "profilePhoto", "foto.png", "image/png", "imagen".getBytes());

        when(playerService.registerPlayer(any(PlayerRegistrationRequest.class)))
                .thenReturn(new StudentPlayer());

        ResponseEntity<GenericResponse> response = playerController.registerPlayer(request, file);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void registerPlayer_InvalidImageFormat_Returns400() {
        PlayerRegistrationRequest request = new PlayerRegistrationRequest();
        request.setName("Jose");
        request.setEmail("jose@mail.escuelaing.edu.co");
        request.setPassword("12345678");
        request.setUserType("STUDENT");
        request.setJerseyNumber(10);
        request.setPosition("Delantero");

        MockMultipartFile file = new MockMultipartFile(
                "profilePhoto", "doc.txt", "text/plain", "texto".getBytes());

        ResponseEntity<GenericResponse> response = playerController.registerPlayer(request, file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Solo se permiten imágenes", response.getBody().getData());
    }

    @Test
    void registerPlayer_Exception_Returns400() {
        PlayerRegistrationRequest request = new PlayerRegistrationRequest();
        request.setName("Jose");
        request.setEmail("jose@mail.escuelaing.edu.co");
        request.setPassword("12345678");
        request.setUserType("STUDENT");
        request.setJerseyNumber(10);
        request.setPosition("Delantero");

        when(playerService.registerPlayer(any(PlayerRegistrationRequest.class)))
                .thenThrow(new RuntimeException("Error interno"));

        ResponseEntity<GenericResponse> response = playerController.registerPlayer(request, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Error interno", response.getBody().getData());
    }

    @Test
    void searchPlayer_Found_Returns200() {
        StudentPlayer mockPlayer = new StudentPlayer();
        ProfileDTO mockProfile = new ProfileDTO(null, "Juan", "juan@mail.com", "STUDENT", null, null, null, null, null, null, null, null, null);

        when(playerService.searchPlayer(1L)).thenReturn(mockPlayer);
        when(playerMapper.toDto(mockPlayer)).thenReturn(mockProfile);

        ResponseEntity<ProfileDTO> response = playerController.search(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void searchPlayer_NotFound_Returns404() {
        when(playerService.searchPlayer(99L)).thenReturn(null);

        ResponseEntity<ProfileDTO> response = playerController.search(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAllPlayers_Returns200() {
        StudentPlayer mockPlayer = new StudentPlayer();
        ProfileDTO mockProfile = new ProfileDTO(null, "Juan", "juan@mail.com", "STUDENT", null, null, null, null, null, null, null, null, null);

        when(playerService.getAllPlayers()).thenReturn(List.of(mockPlayer));
        when(playerMapper.toDto(any(Player.class))).thenReturn(mockProfile);

        ResponseEntity<List<ProfileDTO>> response = playerController.getAllPlayers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }
}