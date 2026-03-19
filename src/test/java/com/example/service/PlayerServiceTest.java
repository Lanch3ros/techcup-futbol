package com.example.service;

import com.example.dto.RegistrationDTO;
import com.example.model.Player;
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
        RegistrationDTO data = new RegistrationDTO();
        data.setFullName("Jose Lancheros");
        data.setEmail("jose.lancheros@mail.escuelaing.edu.co");
        data.setRole("STUDENT");

        Player registeredPlayer = playerService.registerPlayer(data);

        assertNotNull(registeredPlayer, "El jugador registrado no debe ser nulo");
        assertEquals("STUDENT", registeredPlayer.getUserType(), "El tipo de usuario debe ser STUDENT");

        Player savedPlayer = playerService.searchPlayer(0L);
        assertNotNull(savedPlayer, "El jugador debe estar guardado en la lista en memoria");
        assertEquals("Jose Lancheros", ((com.example.model.StudentPlayer) savedPlayer).getFullName());
    }

    @Test
    public void testRegisterPlayerInvalidRole() {
        RegistrationDTO data = new RegistrationDTO();
        data.setFullName("Juan Perez");
        data.setEmail("juan@gmail.com");
        data.setRole("INVALID_ROLE");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.registerPlayer(data);
        });

        assertEquals("Rol no válido: INVALID_ROLE", exception.getMessage());
    }

    @Test
    public void testRegisterPlayerNullData() {
        RegistrationDTO data = new RegistrationDTO();
        data.setRole("STUDENT");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            playerService.registerPlayer(data);
        });

        assertEquals("Datos básicos inválidos", exception.getMessage());
    }
}
