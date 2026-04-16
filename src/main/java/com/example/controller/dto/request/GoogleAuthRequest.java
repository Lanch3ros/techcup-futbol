package com.example.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequest {

    @NotBlank(message = "El token de Google no puede estar vacío")
    private String idToken;
}
