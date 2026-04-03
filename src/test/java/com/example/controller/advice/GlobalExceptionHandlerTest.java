package com.example.controller.advice;

import com.example.controller.dto.response.ErrorResponse;
import com.example.core.exception.BusinessRuleException;
import com.example.core.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();

        // Mockeamos la petición HTTP para simular una ruta específica
        request = Mockito.mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/v1/recurso-prueba");
    }

    @Test
    void handleResourceNotFound_ShouldReturn404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Jugador no encontrado");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Jugador no encontrado", response.getBody().getMessage());
        assertEquals("/api/v1/recurso-prueba", response.getBody().getPath());
    }

    @Test
    void handleBusinessRuleException_ShouldReturn409() {
        BusinessRuleException ex = new BusinessRuleException("El equipo ya tiene el máximo de jugadores permitidos");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusinessRuleException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("El equipo ya tiene el máximo de jugadores permitidos", response.getBody().getMessage());
        assertEquals("/api/v1/recurso-prueba", response.getBody().getPath());
    }

    @Test
    void handleValidationExceptions_ShouldReturn400() {
        MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
        when(ex.getMessage()).thenReturn("Error de validación interno");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationExceptions(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("Datos inválidos en la petición", response.getBody().getMessage());
        assertEquals("/api/v1/recurso-prueba", response.getBody().getPath());
    }

    @Test
    void handleDataIntegrityViolation_ShouldReturn409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement; SQL [n/a]; constraint [users_license_number_key]");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleDataIntegrityViolation(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals(
                "Ya existe un registro con ese valor único (correo, número de licencia u otro campo irrepetible).",
                response.getBody().getMessage());
        assertEquals("/api/v1/recurso-prueba", response.getBody().getPath());
    }

    @Test
    void handleGlobalException_ShouldReturn500() {
        Exception ex = new Exception("Error de conexión a base de datos simulado");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGlobalException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Error interno del servidor", response.getBody().getMessage());
        assertEquals("/api/v1/recurso-prueba", response.getBody().getPath());
    }
}