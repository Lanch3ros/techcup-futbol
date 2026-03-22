package com.example.controller;

import com.example.controller.dto.request.TeamCreationRequest;
import com.example.controller.dto.response.GenericResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/teams")
public class TeamController {

    @PostMapping
    public ResponseEntity<GenericResponse> createTeam(@Valid @RequestBody TeamCreationRequest request) {
        // Respuesta de confirmación para el RF-03
        GenericResponse response = new GenericResponse("Equipo creado correctamente", request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}