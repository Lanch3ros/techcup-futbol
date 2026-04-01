package com.example.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String email;

    public AuthResponse(String token, String email) {
        this.token = token;
        this.email = email;
    }
}
