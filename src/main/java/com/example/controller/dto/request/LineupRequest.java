package com.example.controller.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;
import lombok.Data;

@Data
public class LineupRequest {
    @NotNull(message = "No puede estar vacía la lista de jugadores")
    @Size(min = 7, max = 12, message = "Deben haber mínimo 7 jugadores y máximo 12 jugadores")
    private List<Long> startingPlayersIds;

    @NotBlank(message = "Formation schema is required")
    private String formation;
}
