package com.example.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TeamCreationRequest {

    @NotBlank(message = "Ingrese nombre del equipo")
    private String name; // RF-03: Nombre oficial

    @NotBlank(message = "Se deben especificar los colores del equipo.")
    private String colors; // RF-03: Al menos un color

}
