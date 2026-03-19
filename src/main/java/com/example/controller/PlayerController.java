package com.example.controller;

import com.example.dto.RegistrationDTO;
import com.example.model.Player;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegistrationDTO data) {
        try {
            return ResponseEntity.ok("Jugador registrado exitosamente.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error en el registro: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> search(@PathVariable Long id) {
        return ResponseEntity.ok(null);
    }
}
