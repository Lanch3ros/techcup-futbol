package com.example.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefereeRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String fullName;

    @Email(message = "Formato de correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "El número de licencia es obligatorio")
    private String licenseNumber;

    private String certificationLevel;

    /** Opcional; si no hay certificationLevel, se refleja en certificationLevel como texto. */
    @Min(value = 0, message = "Los años de experiencia no pueden ser negativos")
    @Max(value = 80, message = "Los años de experiencia no son válidos")
    private Integer experienceYears;
}