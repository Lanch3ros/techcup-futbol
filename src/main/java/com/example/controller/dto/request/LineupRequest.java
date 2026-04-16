package com.example.controller.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;
import lombok.Data;

@Data
public class LineupRequest {
    @NotNull(message = "No puede estar vacía la lista de jugadores")
    @Size(min = 7, max = 11, message = "Deben haber mínimo 7 jugadores titulares y máximo 11")
    private List<Long> startingPlayersIds;

    @NotBlank(message = "Formation schema is required")
    private String formation;

    @Size(max = 5, message = "Se permiten máximo 5 suplentes")
    private List<Long> reservePlayerIds;
}
