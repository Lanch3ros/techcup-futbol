package com.example.core.factory;

import com.example.core.model.Player;
import com.example.controller.dto.RegistrationDTO;

public abstract class PlayerFactory {

    public Player registerPlayerData(RegistrationDTO data) {
        if (!validateBasicData(data)) {
            throw new IllegalArgumentException("Datos básicos inválidos");
        }
        if (!validateEmail(data.email())) {
            throw new IllegalArgumentException("Correo inválido para este tipo de usuario");
        }
        return createPlayer(data);
    }

    protected abstract Player createPlayer(RegistrationDTO data);
    protected abstract boolean validateEmail(String email);

    private boolean validateBasicData(RegistrationDTO data) {
        return data != null && data.fullName() != null && data.email() != null;
    }
}