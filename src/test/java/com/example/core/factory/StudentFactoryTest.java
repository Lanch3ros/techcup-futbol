package com.example.core.factory;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.model.Player;
import com.example.core.model.StudentPlayer;
import com.example.core.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StudentFactoryTest {

    private final StudentFactory factory = new StudentFactory();

    private PlayerRegistrationRequest buildRequest(String name, String email) {
        PlayerRegistrationRequest request = new PlayerRegistrationRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword("12345678");
        request.setUserType("STUDENT");
        request.setJerseyNumber(10);
        request.setPosition("Delantero");
        return request;
    }

    @Test
    void registerPlayerData_Success_ReturnsStudentPlayer() {
        PlayerRegistrationRequest dto = buildRequest("Jose Lancheros", "jose@mail.escuelaing.edu.co");

        User result = factory.registerPlayerData(dto);

        assertNotNull(result);
        assertTrue(result instanceof StudentPlayer);
        assertEquals("Jose Lancheros", result.getFullName());
        assertEquals("jose@mail.escuelaing.edu.co", result.getEmail());
    }

    @Test
    void registerPlayerData_NullData_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                factory.registerPlayerData(null));
        assertEquals("Datos básicos inválidos", exception.getMessage());
    }

    @Test
    void registerPlayerData_NullFullName_ThrowsException() {
        PlayerRegistrationRequest dto = buildRequest(null, "jose@mail.escuelaing.edu.co");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                factory.registerPlayerData(dto));
        assertEquals("Datos básicos inválidos", exception.getMessage());
    }

    @Test
    void registerPlayerData_NullEmail_ThrowsException() {
        PlayerRegistrationRequest dto = buildRequest("Jose Lancheros", null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                factory.registerPlayerData(dto));
        assertEquals("Datos básicos inválidos", exception.getMessage());
    }

    @Test
    void registerPlayerData_InvalidEmailDomain_ThrowsException() {
        PlayerRegistrationRequest dto = buildRequest("Jose Lancheros", "jose@gmail.com");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                factory.registerPlayerData(dto));
        assertEquals("Correo inválido para este tipo de usuario", exception.getMessage());
    }
}