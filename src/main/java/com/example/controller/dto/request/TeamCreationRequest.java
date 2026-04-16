package com.example.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamCreationRequest {

    @NotBlank(message = "Ingrese nombre del equipo")
    private String name;

    @NotBlank(message = "Se deben especificar los colores del equipo.")
    private String colors;
}
