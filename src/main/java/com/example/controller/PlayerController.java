package com.example.controller;

import com.example.controller.dto.response.ProfileDTO;
import com.example.controller.dto.response.GenericResponse;
import com.example.controller.mapper.PlayerMapper;
import com.example.core.model.Player;
import com.example.core.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.controller.dto.request.PlayerRegistrationRequest;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/players")
@Tag(name = "Jugadores", description = "Endpoints para la gestión y registro de jugadores en TechCup")
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerMapper playerMapper;

    public PlayerController(PlayerService playerService, PlayerMapper playerMapper) {
        this.playerService = playerService;
        this.playerMapper = playerMapper;
    }


    @Operation(summary = "Registrar un nuevo jugador")
    @PostMapping(value = "/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<GenericResponse> registerPlayer(
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @Valid @RequestPart("playerData") PlayerRegistrationRequest request,
            @RequestPart(value = "profilePhoto", required = false) MultipartFile profilePhoto) {

        log.info("POST /api/v1/players/register - userType: {}, email: {}", request.getUserType(), request.getEmail());

        if (profilePhoto != null && !profilePhoto.getContentType().startsWith("image/")) {
            log.warn("Formato de imagen inválido: {}", profilePhoto.getContentType());
            return new ResponseEntity<>(new GenericResponse("Error", "Solo se permiten imágenes"), HttpStatus.BAD_REQUEST);
        }

        try {
            playerService.registerPlayer(request);
            log.info("Jugador registrado exitosamente - email: {}", request.getEmail());
            return new ResponseEntity<>(new GenericResponse("Éxito", "Jugador creado correctamente"), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error al registrar jugador - email: {}, causa: {}", request.getEmail(), e.getMessage());
            return new ResponseEntity<>(new GenericResponse("Error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    @Operation(summary = "Buscar jugador por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProfileDTO> search(@PathVariable Long id) {
        log.info("GET /api/v1/players/{}", id);
        Player player = playerService.searchPlayer(id);
        if (player != null) {
            log.info("Jugador encontrado - ID: {}", id);
            return ResponseEntity.ok(playerMapper.toDto(player));
        }
        log.warn("Jugador no encontrado - ID: {}", id);
        return ResponseEntity.notFound().build();
    }


    @Operation(summary = "Listar todos los jugadores")
    @GetMapping
    public ResponseEntity<List<ProfileDTO>> getAllPlayers() {
        log.info("GET /api/v1/players");
        List<ProfileDTO> profiles = playerService.getAllPlayers().stream()
                .map(playerMapper::toDto).toList();
        log.info("Total de jugadores retornados: {}", profiles.size());
        return ResponseEntity.ok(profiles);
    }


    @Operation(summary = "Listar jugadores disponibles (agentes libres)")
    @GetMapping("/available")
    public ResponseEntity<List<ProfileDTO>> getAvailablePlayers() {
        log.info("GET /api/v1/players/available");
        List<ProfileDTO> profiles = playerService.getAvailablePlayers().stream()
                .map(playerMapper::toDto).toList();
        log.info("Jugadores disponibles retornados: {}", profiles.size());
        return ResponseEntity.ok(profiles);
    }


    @Operation(summary = "Buscar jugadores con filtros")
    @GetMapping("/search")
    public ResponseEntity<List<ProfileDTO>> searchPlayers(
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String name) {

        log.info("GET /api/v1/players/search - position: {}, name: {}", position, name);
        List<ProfileDTO> profiles = playerService.searchPlayers(position, name).stream()
                .map(playerMapper::toDto).toList();
        log.info("Resultados de búsqueda: {} jugadores encontrados", profiles.size());
        return ResponseEntity.ok(profiles);
    }


    @Operation(summary = "Actualizar la posición de juego del jugador")
    @PatchMapping("/{id}/position")
    public ResponseEntity<GenericResponse> updatePosition(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        log.info("PATCH /api/v1/players/{}/position", id);
        try {
            String newPosition = payload.get("position");
            if (newPosition == null || newPosition.trim().isEmpty()) {
                log.warn("Campo 'position' vacío en la petición para jugador ID: {}", id);
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'position' no puede estar vacío"));
            }
            playerService.updatePosition(id, newPosition);
            log.info("Posición actualizada para jugador ID: {} -> {}", id, newPosition);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Posición actualizada correctamente"));
        } catch (Exception e) {
            log.error("Error al actualizar posición del jugador ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Cambiar el estado de disponibilidad del jugador")
    @PatchMapping("/{id}/availability")
    public ResponseEntity<GenericResponse> updateAvailability(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        log.info("PATCH /api/v1/players/{}/availability", id);
        try {
            Boolean isAvailable = payload.get("available");
            if (isAvailable == null) {
                log.warn("Campo 'available' no proporcionado para jugador ID: {}", id);
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'available' es obligatorio"));
            }
            playerService.updateAvailability(id, isAvailable);
            log.info("Disponibilidad actualizada para jugador ID: {} -> {}", id, isAvailable);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Estado de disponibilidad actualizado"));
        } catch (Exception e) {
            log.error("Error al actualizar disponibilidad del jugador ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Asignar o cambiar el número dorsal")
    @PatchMapping("/{id}/jersey-number")
    public ResponseEntity<GenericResponse> updateJerseyNumber(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        log.info("PATCH /api/v1/players/{}/jersey-number", id);
        try {
            Integer jerseyNumber = payload.get("jerseyNumber");
            if (jerseyNumber == null || jerseyNumber <= 0 || jerseyNumber > 99) {
                log.warn("Número dorsal inválido: {} para jugador ID: {}", jerseyNumber, id);
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El número dorsal debe ser válido (entre 1 y 99)"));
            }
            playerService.updateJerseyNumber(id, jerseyNumber);
            log.info("Número dorsal actualizado para jugador ID: {} -> {}", id, jerseyNumber);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Número dorsal actualizado correctamente"));
        } catch (Exception e) {
            log.error("Error al actualizar dorsal del jugador ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Responder a una invitación de equipo")
    @PatchMapping("/{id}/invitations/{teamId}")
    public ResponseEntity<GenericResponse> respondToInvitation(
            @PathVariable Long id,
            @PathVariable Long teamId,
            @RequestBody Map<String, String> payload) {

        log.info("PATCH /api/v1/players/{}/invitations/{}", id, teamId);
        try {
            String action = payload.get("action");
            if (action == null || (!action.equalsIgnoreCase("ACCEPT") && !action.equalsIgnoreCase("REJECT"))) {
                log.warn("Acción inválida '{}' para invitación - jugador ID: {}, equipo ID: {}", action, id, teamId);
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "La acción debe ser ACCEPT o REJECT"));
            }
            playerService.respondToInvitation(id, teamId, action.toUpperCase());
            log.info("Invitación procesada - jugador ID: {}, equipo ID: {}, acción: {}", id, teamId, action);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Respuesta a la invitación procesada correctamente"));
        } catch (Exception e) {
            log.error("Error al procesar invitación - jugador ID: {}, equipo ID: {} - {}", id, teamId, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }


    @Operation(summary = "Actualizar foto de perfil del jugador")
    @PatchMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericResponse> updatePhoto(
            @PathVariable Long id,
            @RequestPart("photo") MultipartFile photo) {

        log.info("PATCH /api/v1/players/{}/photo", id);
        try {
            if (photo == null || !photo.getContentType().startsWith("image/")) {
                log.warn("Formato de archivo inválido para foto de jugador ID: {}", id);
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "Solo se permiten archivos de imagen"));
            }
            Player player = playerService.searchPlayer(id);
            if (player == null) {
                log.warn("Jugador no encontrado al actualizar foto - ID: {}", id);
                return ResponseEntity.notFound().build();
            }
            log.info("Foto de perfil actualizada para jugador ID: {}", id);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Foto de perfil actualizada correctamente"));
        } catch (Exception e) {
            log.error("Error al actualizar foto del jugador ID: {} - {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }
}