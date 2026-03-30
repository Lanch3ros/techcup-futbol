package com.example.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MatchCreationRequest {

    @NotNull(message = "El equipo local es obligatorio")
    private Long homeTeamId;

    @NotNull(message = "El equipo visitante es obligatorio")
    private Long awayTeamId;

    @NotNull(message = "La fecha del partido es obligatoria")
    private LocalDateTime matchDate;

    private String field;

    private Long tournamentId;
}