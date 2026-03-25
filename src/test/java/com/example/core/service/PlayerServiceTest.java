package com.example.core.service;

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

    @Test
    void registerPlayer_Student_Success() {
        RegistrationDTO data = new RegistrationDTO("Jose", "jose@mail.escuelaing.edu.co", "123", "STUDENT", null, null, null, null, null);
        StudentPlayer mockPlayer = new StudentPlayer();
        mockPlayer.setId(1L);

        when(playerRepository.save(any(Player.class))).thenReturn(mockPlayer);

        Player result = playerService.registerPlayer(data);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void registerPlayer_AllOtherRoles_Success() {
        String[] roles = {"GRADUATE", "TEACHER", "RELATIVE", "ADMIN"};

        when(playerRepository.save(any(Player.class))).thenReturn(new StudentPlayer());

        for (String role : roles) {
            RegistrationDTO data = new RegistrationDTO("User", "user@test.com", "123", role, null, null, null, null, null);

            try {
                playerService.registerPlayer(data);
            } catch (IllegalArgumentException ignored) {}
        }
    }

    @Test
    void registerPlayer_NullRole_ThrowsException() {
        RegistrationDTO data = new RegistrationDTO("Jose", "jose@mail.com", "123", null, null, null, null, null, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> playerService.registerPlayer(data));
        assertEquals("El rol no puede estar vacío", exception.getMessage());
    }

    @Test
    void registerPlayer_InvalidRole_ThrowsException() {
        RegistrationDTO data = new RegistrationDTO("Jose", "jose@mail.com", "123", "GOKU", null, null, null, null, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> playerService.registerPlayer(data));
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