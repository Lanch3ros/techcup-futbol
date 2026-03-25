package com.example.controller;

import com.example.controller.dto.RegistrationDTO;
import com.example.controller.dto.ProfileDTO;
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

    @Operation(summary = "Registrar un nuevo jugador", description = "Permite registrar estudiantes, profesores, graduados o familiares. Soporta la subida de una foto de perfil en formato imagen.")
    @PostMapping(value = "/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<GenericResponse> registerPlayer(
            @Parameter(description = "Datos del jugador en formato JSON", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @Valid @RequestPart("playerData") PlayerRegistrationRequest request,

            @Parameter(description = "Foto de perfil opcional (JPG, PNG)")
            @RequestPart(value = "profilePhoto", required = false) MultipartFile profilePhoto) {

        log.info("Petición REST POST recibida en /api/v1/players/register");

        if (profilePhoto != null && !profilePhoto.getContentType().startsWith("image/")) {
            log.warn("Validación fallida: El archivo subido no es una imagen. ContentType: {}", profilePhoto.getContentType());
            return new ResponseEntity<>(new GenericResponse("Error", "Solo se permiten imágenes"), HttpStatus.BAD_REQUEST);
        }

        try {
            playerService.registerPlayer(request);
            log.info("Petición procesada exitosamente. Retornando código HTTP 201 (CREATED).");
            return new ResponseEntity<>(new GenericResponse("Éxito", "Jugador creado correctamente"), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Fallo al procesar la petición de registro: {}", e.getMessage(), e);
            return new ResponseEntity<>(new GenericResponse("Error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Buscar jugador por ID", description = "Retorna la información detallada de un jugador previamente registrado utilizando su identificador único.")
    @GetMapping("/{id}")
    public ResponseEntity<ProfileDTO> search(@PathVariable Long id) {
        log.info("Petición REST GET recibida en /api/v1/players/{}", id);

        Player player = playerService.searchPlayer(id);

        if (player != null) {
            log.info("Jugador encontrado. Retornando código HTTP 200 (OK).");
            return ResponseEntity.ok(playerMapper.toDto(player));
        } else {
            log.warn("Jugador no encontrado en la base de datos. Retornando código HTTP 404 (NOT FOUND).");
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Listar todos los jugadores", description = "Retorna una lista completa de todos los jugadores registrados en la plataforma.")
    @GetMapping
    public ResponseEntity<List<ProfileDTO>> getAllPlayers() {
        log.info("Petición REST GET recibida en /api/v1/players");

        List<Player> players = playerService.getAllPlayers();
        List<ProfileDTO> profiles = players.stream()
                .map(playerMapper::toDto)
                .toList();

        log.info("Retornando lista con {} jugadores. Código HTTP 200 (OK).", profiles.size());

        return ResponseEntity.ok(profiles);
    }

    @Operation(summary = "Actualizar la posición de juego del jugador", description = "Modifica la posición preferida del jugador en la cancha.")
    @PatchMapping("/{id}/position")
    public ResponseEntity<GenericResponse> updatePosition(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        log.info("Petición REST PATCH recibida en /api/v1/players/{}/position", id);
        try {
            String newPosition = payload.get("position");
            if (newPosition == null || newPosition.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'position' no puede estar vacío"));
            }

            playerService.updatePosition(id, newPosition);
            log.info("Posición del jugador {} actualizada exitosamente a {}.", id, newPosition);

            return ResponseEntity.ok(new GenericResponse("Éxito", "Posición actualizada correctamente"));
        } catch (Exception e) {
            log.error("Error al actualizar la posición del jugador {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }

    @Operation(summary = "Cambiar el estado de disponibilidad del jugador", description = "Marca si el jugador está disponible como agente libre para ser reclutado o si ya pertenece a un equipo.")
    @PatchMapping("/{id}/availability")
    public ResponseEntity<GenericResponse> updateAvailability(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        log.info("Petición REST PATCH recibida en /api/v1/players/{}/availability", id);
        try {
            Boolean isAvailable = payload.get("available");
            if (isAvailable == null) {
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El campo 'available' es obligatorio"));
            }

            playerService.updateAvailability(id, isAvailable);
            log.info("Disponibilidad del jugador {} actualizada exitosamente a {}.", id, isAvailable);

            return ResponseEntity.ok(new GenericResponse("Éxito", "Estado de disponibilidad actualizado"));
        } catch (Exception e) {
            log.error("Error al actualizar la disponibilidad del jugador {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }

    @Operation(summary = "Asignar o cambiar el número dorsal", description = "Permite al jugador elegir el número que usará en su camiseta durante el torneo.")
    @PatchMapping("/{id}/jersey-number")
    public ResponseEntity<GenericResponse> updateJerseyNumber(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        log.info("Petición REST PATCH recibida en /api/v1/players/{}/jersey-number", id);
        try {
            Integer jerseyNumber = payload.get("jerseyNumber");
            if (jerseyNumber == null || jerseyNumber <= 0 || jerseyNumber > 99) {
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "El número dorsal debe ser válido (ej. entre 1 y 99)"));
            }

            playerService.updateJerseyNumber(id, jerseyNumber);
            log.info("Dorsal del jugador {} actualizado exitosamente a {}.", id, jerseyNumber);

            return ResponseEntity.ok(new GenericResponse("Éxito", "Número dorsal actualizado correctamente"));
        } catch (Exception e) {
            log.error("Error al actualizar el dorsal del jugador {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }

    @Operation(summary = "Responder a una invitación de equipo", description = "Permite a un jugador aceptar o rechazar la invitación para unirse a un equipo específico.")
    @PatchMapping("/{id}/invitations/{teamId}")
    public ResponseEntity<GenericResponse> respondToInvitation(
            @PathVariable Long id,
            @PathVariable Long teamId,
            @RequestBody Map<String, String> payload) {

        log.info("Petición REST PATCH recibida en /api/v1/players/{}/invitations/{}", id, teamId);
        try {
            String action = payload.get("action");
            if (action == null || (!action.equalsIgnoreCase("ACCEPT") && !action.equalsIgnoreCase("REJECT"))) {
                return ResponseEntity.badRequest().body(new GenericResponse("Error", "La acción debe ser ACCEPT o REJECT"));
            }

            playerService.respondToInvitation(id, teamId, action.toUpperCase());
            log.info("Jugador {} ha respondido {} a la invitación del equipo {}.", id, action, teamId);

            return ResponseEntity.ok(new GenericResponse("Éxito", "Respuesta a la invitación procesada correctamente"));
        } catch (Exception e) {
            log.error("Error al procesar la invitación del jugador {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new GenericResponse("Error", e.getMessage()));
        }
    }
}