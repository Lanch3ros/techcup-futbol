package com.example.core.service;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.model.Player;
import com.example.core.model.StudentPlayer;
import com.example.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PlayerServiceTest {

    private PlayerRepository playerRepository;
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerRepository = Mockito.mock(PlayerRepository.class);
        playerService = new PlayerService(playerRepository);
    }

    private PlayerRegistrationRequest buildRequest(String name, String email, String role) {
        PlayerRegistrationRequest request = new PlayerRegistrationRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword("12345678");
        request.setUserType(role);
        request.setJerseyNumber(10);
        request.setPosition("Delantero");
        return request;
    }

    @Test
    void registerPlayer_Student_Success() {
        PlayerRegistrationRequest data = buildRequest("Jose", "jose@mail.escuelaing.edu.co", "STUDENT");

        StudentPlayer mockPlayer = new StudentPlayer();
        mockPlayer.setId(1L);

        when(playerRepository.save(any(Player.class))).thenReturn(mockPlayer);

        Player result = playerService.registerPlayer(data);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void registerPlayer_Graduate_Success() {
        PlayerRegistrationRequest data = buildRequest("Ana", "ana@mail.escuelaing.edu.co", "GRADUATE");
        when(playerRepository.save(any(Player.class))).thenReturn(new StudentPlayer());
        assertDoesNotThrow(() -> playerService.registerPlayer(data));
    }

    @Test
    void registerPlayer_Teacher_Success() {
        PlayerRegistrationRequest data = buildRequest("Prof", "prof@escuelaing.edu.co", "TEACHER");
        when(playerRepository.save(any(Player.class))).thenReturn(new StudentPlayer());
        assertDoesNotThrow(() -> playerService.registerPlayer(data));
    }

    @Test
    void registerPlayer_Relative_Success() {
        PlayerRegistrationRequest data = buildRequest("Fam", "fam@gmail.com", "RELATIVE");
        when(playerRepository.save(any(Player.class))).thenReturn(new StudentPlayer());
        assertDoesNotThrow(() -> playerService.registerPlayer(data));
    }

    @Test
    void registerPlayer_Admin_Success() {
        PlayerRegistrationRequest data = buildRequest("Admin", "admin@escuelaing.edu.co", "ADMIN");
        when(playerRepository.save(any(Player.class))).thenReturn(new StudentPlayer());
        assertDoesNotThrow(() -> playerService.registerPlayer(data));
    }

    @Test
    void registerPlayer_NullRole_ThrowsException() {
        PlayerRegistrationRequest data = buildRequest("Jose", "jose@mail.com", null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> playerService.registerPlayer(data));
        assertEquals("El rol no puede estar vacío", exception.getMessage());
    }

    @Test
    void registerPlayer_InvalidRole_ThrowsException() {
        PlayerRegistrationRequest data = buildRequest("Jose", "jose@mail.com", "GOKU");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> playerService.registerPlayer(data));
        assertEquals("Rol no válido: GOKU", exception.getMessage());
    }

    @Test
    void searchPlayer_Found_ReturnsPlayer() {
        StudentPlayer mockPlayer = new StudentPlayer();
        mockPlayer.setId(1L);
        when(playerRepository.findById(1L)).thenReturn(mockPlayer);

        Player result = playerService.searchPlayer(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void searchPlayer_NotFound_ReturnsNull() {
        when(playerRepository.findById(99L)).thenReturn(null);

        Player result = playerService.searchPlayer(99L);

        assertNull(result);
    }

    @Test
    void getAllPlayers_ReturnsList() {
        when(playerRepository.findAll()).thenReturn(List.of(new StudentPlayer()));

        List<Player> result = playerService.getAllPlayers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}