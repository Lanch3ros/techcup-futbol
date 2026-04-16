package com.example.controller.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class MatchEventRequest {

    @NotNull(message = "El ID del jugador es obligatorio")
    private Long playerId;

    private String playerName;

    @NotBlank(message = "El tipo de evento es obligatorio")
    @Pattern(regexp = "^(GOL|AMARILLA|ROJA)$", message = "El tipo debe ser GOL, AMARILLA o ROJA")
    private String type;

    @Min(value = 0, message = "El minuto no puede ser negativo")
    private int minute;
}