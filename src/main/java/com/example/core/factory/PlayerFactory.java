package com.example.core.factory;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.model.Player;

public abstract class PlayerFactory {

    public Player registerPlayerData(PlayerRegistrationRequest data) {
        if (!validateBasicData(data)) {
            throw new IllegalArgumentException("Datos básicos inválidos");
        }
        if (!validateEmail(data.getEmail())) {
            throw new IllegalArgumentException("Correo inválido para este tipo de usuario");
        }
        return createPlayer(data);
    }

    protected abstract Player createPlayer(PlayerRegistrationRequest data);
    protected abstract boolean validateEmail(String email);

    private boolean validateBasicData(PlayerRegistrationRequest data) {
        return data != null && data.getName() != null && data.getEmail() != null;
    }
}