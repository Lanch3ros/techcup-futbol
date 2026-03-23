package com.example.core.factory;

import com.example.controller.dto.RegistrationDTO;
import com.example.core.model.Player;
import com.example.core.model.StudentPlayer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentFactoryTest {

    private final StudentFactory factory = new StudentFactory();

    @Test
    void registerPlayerData_Success_ReturnsStudentPlayer() {
        RegistrationDTO dto = new RegistrationDTO("Jose Lancheros", "jose@mail.escuelaing.edu.co", "123", "STUDENT", null, null, null, null, null);

        Player result = factory.registerPlayerData(dto);

        assertNotNull(result);
        assertTrue(result instanceof StudentPlayer);
        assertEquals("Jose Lancheros", result.getFullName());
        assertEquals("jose@mail.escuelaing.edu.co", result.getEmail());
    }

    @Test
    void registerPlayerData_NullData_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            factory.registerPlayerData(null);
        });
        assertEquals("Datos básicos inválidos", exception.getMessage());
    }

    @Test
    void registerPlayerData_NullFullName_ThrowsException() {
        RegistrationDTO dto = new RegistrationDTO(null, "jose@mail.escuelaing.edu.co", "123", "STUDENT", null, null, null, null, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            factory.registerPlayerData(dto);
        });
        assertEquals("Datos básicos inválidos", exception.getMessage());
    }

    @Test
    void registerPlayerData_NullEmail_ThrowsException() {
        RegistrationDTO dto = new RegistrationDTO("Jose Lancheros", null, "123", "STUDENT", null, null, null, null, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            factory.registerPlayerData(dto);
        });
        assertEquals("Datos básicos inválidos", exception.getMessage());
    }

    @Test
    void registerPlayerData_InvalidEmailDomain_ThrowsException() {
        RegistrationDTO dto = new RegistrationDTO("Jose Lancheros", "jose@gmail.com", "123", "STUDENT", null, null, null, null, null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            factory.registerPlayerData(dto);
        });
        assertEquals("Correo inválido para este tipo de usuario", exception.getMessage());
    }
}