package com.example.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefereeRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String fullName;

    @Email(message = "Formato de correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    @NotBlank(message = "El número de licencia es obligatorio")
    private String licenseNumber;

    private String certificationLevel;
}