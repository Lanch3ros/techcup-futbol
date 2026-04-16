package com.example.controller.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Data;

@Data
public class TournamentCreationRequest {

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotNull(message = "El costo de inscripción es obligatorio")
    @Min(value = 0, message = "Team cost must be greater than or equal to 0")
    private Double teamCost;

    @NotNull(message = "La cantidad de equipos es obligatoria")
    @Min(value = 2, message = "Debe haber al menos 2 equipos para realizar un torneo")
    private Integer numberOfTeams;

    @NotBlank(message = "Rules cannot be empty")
    private String rules;
}
