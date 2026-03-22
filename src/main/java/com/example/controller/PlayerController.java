package com.example.controller;

import com.example.dto.RegistrationDTO;
import com.example.dto.response.GenericResponse;
import com.example.model.Player;
import com.example.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/players")
@Tag(name = "Jugadores", description = "Endpoints para la gestión y registro de jugadores en TechCup")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Operation(summary = "Registrar un nuevo jugador", description = "Permite registrar estudiantes, profesores, graduados o familiares. Soporta la subida de una foto de perfil en formato imagen.")
    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<GenericResponse> registerPlayer(
            @Valid @RequestPart("playerData") RegistrationDTO request,
            @RequestPart(value = "profilePhoto", required = false) MultipartFile profilePhoto) {

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

    @Operation(summary = "Buscar jugador por ID", description = "Retorna la información detallada de un jugador previamente registrado utilizando su identificador único.")
    @GetMapping("/{id}")
    public ResponseEntity<Player> search(@PathVariable Long id) {
        Player player = playerService.searchPlayer(id);
        return player != null ? ResponseEntity.ok(player) : ResponseEntity.notFound().build();
    }
}
