package com.example.factory;

import com.example.model.Player;
import com.example.dto.RegistrationDTO;

public abstract class PlayerFactory {

    public Player registerPlayerData(RegistrationDTO data) {
        if (!validateBasicData(data)) {
            throw new IllegalArgumentException("Datos básicos inválidos");
        }
        if (!validateEmail(data.getEmail())) {
            throw new IllegalArgumentException("Correo inválido para este tipo de usuario");
        }
        return createPlayer(data);
    }

    protected abstract Player createPlayer(RegistrationDTO data);
    protected abstract boolean validateEmail(String email);

    private boolean validateBasicData(RegistrationDTO data) {
        return data != null && data.getFullName() != null && data.getEmail() != null;
    }
}