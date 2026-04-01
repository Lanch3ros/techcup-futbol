package com.example.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequest {

    @NotNull(message = "El ID del equipo es obligatorio")
    private Long teamId;

    private String receiptUrl;
}