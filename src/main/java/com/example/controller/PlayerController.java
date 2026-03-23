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

import java.util.List;

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
            @Valid @RequestPart("playerData") RegistrationDTO request,

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
}