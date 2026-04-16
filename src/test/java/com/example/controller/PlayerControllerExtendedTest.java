package com.example.controller;

import com.example.controller.dto.response.GenericResponse;
import com.example.controller.dto.response.ProfileDTO;
import com.example.controller.mapper.PlayerMapper;
import com.example.core.model.Player;
import com.example.core.model.StudentPlayer;
import com.example.core.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("PlayerController – Métodos extendidos (available, search, update, invitaciones, foto)")
class PlayerControllerExtendedTest {

    private PlayerService playerService;
    private PlayerMapper playerMapper;
    private PlayerController playerController;

    @BeforeEach
    void setUp() {
        playerService = mock(PlayerService.class);
        playerMapper  = mock(PlayerMapper.class);
        playerController = new PlayerController(playerService, playerMapper);
    }

    private ProfileDTO profile(String name) {
        return new ProfileDTO(null, name, name.toLowerCase() + "@eci.edu.co", "STUDENT", null, null, null, null, null, null, null, null, null);
    }

    // ── getAvailablePlayers ───────────────────────────────────────────────────

    @Test
    @DisplayName("getAvailablePlayers → 200 OK con jugadores disponibles")
    void getAvailablePlayers_Returns200() {
        StudentPlayer p = new StudentPlayer();
        when(playerService.getAvailablePlayers()).thenReturn(List.of(p));
        when(playerMapper.toDto(any(Player.class))).thenReturn(profile("Carlos"));

        ResponseEntity<List<ProfileDTO>> response = playerController.getAvailablePlayers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("getAvailablePlayers → lista vacía cuando no hay disponibles")
    void getAvailablePlayers_EmptyList() {
        when(playerService.getAvailablePlayers()).thenReturn(List.of());

        ResponseEntity<List<ProfileDTO>> response = playerController.getAvailablePlayers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    // ── searchPlayers ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchPlayers – con filtros → 200 OK con resultados")
    void searchPlayers_WithFilters_Returns200() {
        StudentPlayer p = new StudentPlayer();
        when(playerService.searchPlayers("Delantero", "Carlos")).thenReturn(List.of(p));
        when(playerMapper.toDto(any(Player.class))).thenReturn(profile("Carlos"));

        ResponseEntity<List<ProfileDTO>> response = playerController.searchPlayers("Delantero", "Carlos");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("searchPlayers – sin filtros → 200 OK con todos los jugadores")
    void searchPlayers_NoFilters_Returns200() {
        when(playerService.searchPlayers(null, null)).thenReturn(List.of(new StudentPlayer(), new StudentPlayer()));
        when(playerMapper.toDto(any())).thenReturn(profile("X"));

        ResponseEntity<List<ProfileDTO>> response = playerController.searchPlayers(null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }

    // ── updatePosition ────────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePosition – posición válida → 200 OK")
    void updatePosition_Valid_Returns200() {
        Map<String, String> payload = Map.of("position", "Portero");
        doNothing().when(playerService).updatePosition(1L, "Portero");

        ResponseEntity<GenericResponse> response = playerController.updatePosition(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updatePosition – posición null → 400 BAD REQUEST")
    void updatePosition_NullPosition_Returns400() {
        Map<String, String> payload = new HashMap<>();
        payload.put("position", null);

        ResponseEntity<GenericResponse> response = playerController.updatePosition(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El campo 'position' no puede estar vacío", response.getBody().getData());
    }

    @Test
    @DisplayName("updatePosition – posición en blanco → 400 BAD REQUEST")
    void updatePosition_BlankPosition_Returns400() {
        Map<String, String> payload = Map.of("position", "   ");

        ResponseEntity<GenericResponse> response = playerController.updatePosition(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updatePosition – excepción de servicio → 400 BAD REQUEST")
    void updatePosition_ServiceException_Returns400() {
        Map<String, String> payload = Map.of("position", "Portero");
        doThrow(new RuntimeException("Jugador no encontrado"))
                .when(playerService).updatePosition(1L, "Portero");

        ResponseEntity<GenericResponse> response = playerController.updatePosition(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Jugador no encontrado", response.getBody().getData());
    }

    // ── updateAvailability ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateAvailability – available=true → 200 OK")
    void updateAvailability_True_Returns200() {
        Map<String, Boolean> payload = Map.of("available", true);
        doNothing().when(playerService).updateAvailability(1L, true);

        ResponseEntity<GenericResponse> response = playerController.updateAvailability(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updateAvailability – campo 'available' null → 400 BAD REQUEST")
    void updateAvailability_NullAvailable_Returns400() {
        Map<String, Boolean> payload = new HashMap<>();
        payload.put("available", null);

        ResponseEntity<GenericResponse> response = playerController.updateAvailability(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El campo 'available' es obligatorio", response.getBody().getData());
    }

    @Test
    @DisplayName("updateAvailability – excepción de servicio → 400 BAD REQUEST")
    void updateAvailability_ServiceException_Returns400() {
        Map<String, Boolean> payload = Map.of("available", false);
        doThrow(new RuntimeException("Jugador no encontrado"))
                .when(playerService).updateAvailability(1L, false);

        ResponseEntity<GenericResponse> response = playerController.updateAvailability(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── updateJerseyNumber ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateJerseyNumber – dorsal válido (10) → 200 OK")
    void updateJerseyNumber_Valid_Returns200() {
        Map<String, Integer> payload = Map.of("jerseyNumber", 10);
        doNothing().when(playerService).updateJerseyNumber(1L, 10);

        ResponseEntity<GenericResponse> response = playerController.updateJerseyNumber(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updateJerseyNumber – dorsal null → 400 BAD REQUEST")
    void updateJerseyNumber_NullJersey_Returns400() {
        Map<String, Integer> payload = new HashMap<>();
        payload.put("jerseyNumber", null);

        ResponseEntity<GenericResponse> response = playerController.updateJerseyNumber(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("El número dorsal debe ser válido (entre 1 y 99)", response.getBody().getData());
    }

    @Test
    @DisplayName("updateJerseyNumber – dorsal 0 → 400 BAD REQUEST")
    void updateJerseyNumber_Zero_Returns400() {
        Map<String, Integer> payload = Map.of("jerseyNumber", 0);

        ResponseEntity<GenericResponse> response = playerController.updateJerseyNumber(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updateJerseyNumber – dorsal 100 → 400 BAD REQUEST")
    void updateJerseyNumber_Over99_Returns400() {
        Map<String, Integer> payload = Map.of("jerseyNumber", 100);

        ResponseEntity<GenericResponse> response = playerController.updateJerseyNumber(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("updateJerseyNumber – excepción de servicio → 400 BAD REQUEST")
    void updateJerseyNumber_ServiceException_Returns400() {
        Map<String, Integer> payload = Map.of("jerseyNumber", 7);
        doThrow(new RuntimeException("Dorsal ya en uso"))
                .when(playerService).updateJerseyNumber(1L, 7);

        ResponseEntity<GenericResponse> response = playerController.updateJerseyNumber(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── processInvitationResponse ─────────────────────────────────────────────

    @Test
    @DisplayName("processInvitationResponse – ACCEPT → 200 OK con mensaje de aceptación")
    void processInvitationResponse_Accept_Returns200() {
        Map<String, String> payload = Map.of("action", "ACCEPT");
        doNothing().when(playerService).processInvitationResponse(1L, "ACCEPT");

        ResponseEntity<GenericResponse> response = playerController.processInvitationResponse(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().toString().contains("aceptada"));
    }

    @Test
    @DisplayName("processInvitationResponse – REJECT → 200 OK con mensaje de rechazo")
    void processInvitationResponse_Reject_Returns200() {
        Map<String, String> payload = Map.of("action", "REJECT");
        doNothing().when(playerService).processInvitationResponse(1L, "REJECT");

        ResponseEntity<GenericResponse> response = playerController.processInvitationResponse(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().toString().contains("rechazada"));
    }

    @Test
    @DisplayName("processInvitationResponse – accept en minúsculas → se normaliza a ACCEPT")
    void processInvitationResponse_AcceptLowerCase_Returns200() {
        Map<String, String> payload = Map.of("action", "accept");
        doNothing().when(playerService).processInvitationResponse(1L, "ACCEPT");

        ResponseEntity<GenericResponse> response = playerController.processInvitationResponse(1L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(playerService).processInvitationResponse(1L, "ACCEPT");
    }

    @Test
    @DisplayName("processInvitationResponse – acción inválida → 400 BAD REQUEST")
    void processInvitationResponse_InvalidAction_Returns400() {
        Map<String, String> payload = Map.of("action", "IGNORAR");

        ResponseEntity<GenericResponse> response = playerController.processInvitationResponse(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("La acción debe ser ACCEPT o REJECT", response.getBody().getData());
    }

    @Test
    @DisplayName("processInvitationResponse – acción null → 400 BAD REQUEST")
    void processInvitationResponse_NullAction_Returns400() {
        Map<String, String> payload = new HashMap<>();
        payload.put("action", null);

        ResponseEntity<GenericResponse> response = playerController.processInvitationResponse(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("processInvitationResponse – excepción de servicio → 400 BAD REQUEST")
    void processInvitationResponse_ServiceException_Returns400() {
        Map<String, String> payload = Map.of("action", "ACCEPT");
        doThrow(new RuntimeException("Invitación no encontrada"))
                .when(playerService).processInvitationResponse(1L, "ACCEPT");

        ResponseEntity<GenericResponse> response = playerController.processInvitationResponse(1L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── respondToInvitation ───────────────────────────────────────────────────

    @Test
    @DisplayName("respondToInvitation – ACCEPT → 200 OK")
    void respondToInvitation_Accept_Returns200() {
        Map<String, String> payload = Map.of("action", "ACCEPT");
        doNothing().when(playerService).respondToInvitation(1L, 5L, "ACCEPT");

        ResponseEntity<GenericResponse> response = playerController.respondToInvitation(1L, 5L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("respondToInvitation – REJECT → 200 OK")
    void respondToInvitation_Reject_Returns200() {
        Map<String, String> payload = Map.of("action", "REJECT");
        doNothing().when(playerService).respondToInvitation(1L, 5L, "REJECT");

        ResponseEntity<GenericResponse> response = playerController.respondToInvitation(1L, 5L, payload);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("respondToInvitation – acción inválida → 400 BAD REQUEST")
    void respondToInvitation_InvalidAction_Returns400() {
        Map<String, String> payload = Map.of("action", "MAYBE");

        ResponseEntity<GenericResponse> response = playerController.respondToInvitation(1L, 5L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("La acción debe ser ACCEPT o REJECT", response.getBody().getData());
    }

    @Test
    @DisplayName("respondToInvitation – acción null → 400 BAD REQUEST")
    void respondToInvitation_NullAction_Returns400() {
        Map<String, String> payload = new HashMap<>();
        payload.put("action", null);

        ResponseEntity<GenericResponse> response = playerController.respondToInvitation(1L, 5L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("respondToInvitation – excepción de servicio → 400 BAD REQUEST")
    void respondToInvitation_ServiceException_Returns400() {
        Map<String, String> payload = Map.of("action", "ACCEPT");
        doThrow(new RuntimeException("Jugador no encontrado"))
                .when(playerService).respondToInvitation(1L, 5L, "ACCEPT");

        ResponseEntity<GenericResponse> response = playerController.respondToInvitation(1L, 5L, payload);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ── updatePhoto ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("updatePhoto – imagen válida y jugador encontrado → 200 OK")
    void updatePhoto_ValidImage_Returns200() {
        MockMultipartFile photo = new MockMultipartFile("photo", "foto.jpg", "image/jpeg", new byte[]{1, 2});
        StudentPlayer player = new StudentPlayer();
        player.setId(1L);
        when(playerService.searchPlayer(1L)).thenReturn(player);

        ResponseEntity<GenericResponse> response = playerController.updatePhoto(1L, photo);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Éxito", response.getBody().getMessage());
    }

    @Test
    @DisplayName("updatePhoto – foto null → 400 BAD REQUEST (rama photo == null)")
    void updatePhoto_NullPhoto_Returns400() {
        // photo == null → rama true del || → short-circuit → bad request
        // No es posible pasar null directamente por @RequestPart en producción, pero
        // la rama existe en el código y se cubre instanciando el controlador directamente
        MockMultipartFile nullContentType = mock(MockMultipartFile.class);
        when(nullContentType.getContentType()).thenReturn(null);

        // Forzamos NullPointerException al llamar startsWith sobre null → excepción capturada
        // El camino más directo es simular photo cuyo getContentType() es null
        // pero el guard `photo == null` nunca se activa con MockMultipartFile
        // Usamos reflexión para pasar null directamente:
        ResponseEntity<GenericResponse> response = playerController.updatePhoto(1L, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Solo se permiten archivos de imagen", response.getBody().getData());
    }

    @Test
    @DisplayName("updatePhoto – archivo no es imagen → 400 BAD REQUEST")
    void updatePhoto_NotAnImage_Returns400() {
        MockMultipartFile pdf = new MockMultipartFile("photo", "doc.pdf", "application/pdf", new byte[]{1});

        ResponseEntity<GenericResponse> response = playerController.updatePhoto(1L, pdf);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Solo se permiten archivos de imagen", response.getBody().getData());
    }

    @Test
    @DisplayName("updatePhoto – jugador no encontrado → 404 NOT FOUND")
    void updatePhoto_PlayerNotFound_Returns404() {
        MockMultipartFile photo = new MockMultipartFile("photo", "foto.png", "image/png", new byte[]{1});
        when(playerService.searchPlayer(99L)).thenReturn(null);

        ResponseEntity<GenericResponse> response = playerController.updatePhoto(99L, photo);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("updatePhoto – excepción de servicio → 400 BAD REQUEST")
    void updatePhoto_ServiceException_Returns400() {
        MockMultipartFile photo = new MockMultipartFile("photo", "foto.jpg", "image/jpeg", new byte[]{1});
        when(playerService.searchPlayer(1L)).thenThrow(new RuntimeException("Error interno"));

        ResponseEntity<GenericResponse> response = playerController.updatePhoto(1L, photo);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
