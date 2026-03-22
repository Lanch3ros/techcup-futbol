package com.example.controller;

import com.example.dto.RegistrationDTO;
import com.example.dto.response.GenericResponse;
import com.example.model.Player;
import com.example.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/players") 
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping(value = "/register", consumes = {"multipart/form-data"})
    public ResponseEntity<GenericResponse> registerPlayer(
            @Valid @RequestPart("playerData") RegistrationDTO request,
            @RequestPart(value = "profilePhoto", required = false) MultipartFile profilePhoto) {

        if (profilePhoto != null && !profilePhoto.getContentType().startsWith("image/")) {
            return new ResponseEntity<>(new GenericResponse("Error", "Solo se permiten imágenes"), HttpStatus.BAD_REQUEST);
        }

        try {
          
            playerService.registerPlayer(request, profilePhoto);
            return new ResponseEntity<>(new GenericResponse("Éxito", "Jugador creado correctamente"), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new GenericResponse("Error", e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> search(@PathVariable Long id) {
        Player player = playerService.searchPlayer(id);
        return player != null ? ResponseEntity.ok(player) : ResponseEntity.notFound().build();
    }
}
