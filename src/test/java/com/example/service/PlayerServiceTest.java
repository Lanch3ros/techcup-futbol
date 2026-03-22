package com.example.service;

import com.example.dto.RegistrationDTO;
import com.example.model.Player;
import com.example.model.StudentPlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerServiceTest {

    private PlayerService playerService;

    @BeforeEach
    public void setUp() {
        playerService = new PlayerService();
    }

    @Test
    public void testRegisterStudentSuccess() {
        RegistrationDTO data = new RegistrationDTO(
                "Jose Lancheros",
                "jose.lancheros@mail.escuelaing.edu.co",
                "miPasswordSeguro123",
                "STUDENT",
                null,
                null,
                null,
                null,
                null
        );

        Player registeredPlayer = playerService.registerPlayer(data);

        assertNotNull(registeredPlayer);
        assertEquals("STUDENT", registeredPlayer.getUserType());

        Player savedPlayer = playerService.searchPlayer(0L);
        assertNotNull(savedPlayer);
        assertEquals("Jose Lancheros", ((StudentPlayer) savedPlayer).getFullName());
    }

    @Test
    public void testRegisterPlayerInvalidRole() {
        RegistrationDTO data = new RegistrationDTO(
                "Juan Perez",
                "juan@gmail.com",
                null,
                "INVALID_ROLE",
                null, null, null, null, null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.registerPlayer(data);
        });

        assertEquals("Rol no válido: INVALID_ROLE", exception.getMessage());
    }

    @Test
    public void testRegisterPlayerNullData() {
        RegistrationDTO data = new RegistrationDTO(
                null,
                null,
                null,
                "STUDENT",
                null, null, null, null, null
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.registerPlayer(data);
        });

        assertEquals("Datos básicos inválidos", exception.getMessage());
    }
}