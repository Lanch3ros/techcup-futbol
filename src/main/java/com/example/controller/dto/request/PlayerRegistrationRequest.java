package com.example.controller.dto.request;

import com.example.core.model.Program;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PlayerRegistrationRequest {
    @NotBlank(message = "Ingresar un nombre")
    private String name;

    @NotBlank(message = "Se necesita número de identificación")
    private String identification;

    private Program program;

    @Email(message = "Formato invalido de correo")
    @NotBlank(message = "Se necesita correo")
    private String email;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "Se necesita rol de usuario")
    private String userType;

    @Min(value = 1, message = "El número del dorsal debe ser mínimo 1")
    @Max(value = 99, message = "El número del dorsal debe ser igual o menor a 99")
    private int jerseyNumber;

    @Pattern(regexp = "^(Portero|Defensa|Volante|Delantero)$", message = "Posición invalida")
    private String position;

}
