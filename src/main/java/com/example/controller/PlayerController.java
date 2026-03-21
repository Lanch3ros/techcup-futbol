package com.example.controller;

import com.example.dto.RegistrationDTO;
import com.example.model.Player;
import com.example.service.PlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationDTO data) {
        try {
            playerService.registerPlayer(data);
            return ResponseEntity.ok("Jugador registrado exitosamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error en el registro: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> search(@PathVariable Long id) {
        Player player = playerService.searchPlayer(id);
        if (player != null) {
            return ResponseEntity.ok(player);
        }
        return ResponseEntity.notFound().build();
    }
}