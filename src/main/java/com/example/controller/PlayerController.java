package com.example.controller;

import com.example.controller.dto.response.ProfileDTO;
import com.example.controller.dto.response.GenericResponse;
import com.example.controller.mapper.PlayerMapper;
import com.example.core.model.Player;
import com.example.core.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

        log.info("POST /api/v1/players/register");

        if (profilePhoto != null && !profilePhoto.getContentType().startsWith("image/")) {
            return new ResponseEntity<>(new GenericResponse("Error", "Solo se permiten imágenes"), HttpStatus.BAD_REQUEST);
        }

        try {
            playerService.registerPlayer(request);
            return new ResponseEntity<>(new GenericResponse("Éxito", "Jugador creado correctamente"), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new GenericResponse("Error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }


    @Operation(summary = "Buscar jugador por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProfileDTO> search(@PathVariable Long id) {
        log.info("GET /api/v1/players/{}", id);
        Player player = playerService.searchPlayer(id);
        if (player != null) return ResponseEntity.ok(playerMapper.toDto(player));
        return ResponseEntity.notFound().build();
    }


    @Operation(summary = "Listar todos los jugadores")
    @GetMapping
    public ResponseEntity<List<ProfileDTO>> getAllPlayers() {
        log.info("GET /api/v1/players");
        List<ProfileDTO> profiles = playerService.getAllPlayers().stream()
                .map(playerMapper::toDto).toList();
        return ResponseEntity.ok(profiles);
    }


    @Operation(summary = "Listar jugadores disponibles (agentes libres)")
    @GetMapping("/available")
    public ResponseEntity<List<ProfileDTO>> getAvailablePlayers() {
        log.info("GET /api/v1/players/available");
        List<ProfileDTO> profiles = playerService.getAvailablePlayers().stream()
                .map(playerMapper::toDto).toList();
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
        return ResponseEntity.ok(profiles);
    }


    @Operation(summary = "Actualizar la posición de juego del jugador")
    @PatchMapping("/{id}/position")
    public ResponseEntity<GenericResponse> updatePosition(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        log.info("PATCH /api/v1/players/{}/position", id);
        try {
            String newPosition = payload.get("position");
            if (newPosition == null || newPosition.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'position' no puede estar vacío"));
            }
            playerService.updatePosition(id, newPosition);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Posición actualizada correctamente"));
        } catch (Exception e) {
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
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'available' es obligatorio"));
            }
            playerService.updateAvailability(id, isAvailable);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Estado de disponibilidad actualizado"));
        } catch (Exception e) {
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
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El número dorsal debe ser válido (entre 1 y 99)"));
            }
            playerService.updateJerseyNumber(id, jerseyNumber);
            return ResponseEntity.ok(new GenericResponse("Éxito", "Número dorsal actualizado correctamente"));
        } catch (Exception e) {
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
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "La acción debe ser ACCEPT o REJECT"));
            }
            playerService.respondToInvitation(id, teamId, action.toUpperCase());
            return ResponseEntity.ok(new GenericResponse("Éxito", "Respuesta a la invitación procesada correctamente"));
        } catch (Exception e) {
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
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "Solo se permiten archivos de imagen"));
            }
            Player player = playerService.searchPlayer(id);
            if (player == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new GenericResponse("Éxito", "Foto de perfil actualizada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }
}