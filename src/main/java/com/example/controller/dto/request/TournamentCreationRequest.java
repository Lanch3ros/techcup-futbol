package com.example.controller.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Data;

@Data
public class TournamentCreationRequest {
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @Min(value = 0, message = "Team cost must be greater than or equal to 0")
    private Double teamCost; // RN-10: Costo >= 0

    @NotBlank(message = "Rules cannot be empty")
    private String rules;
}
