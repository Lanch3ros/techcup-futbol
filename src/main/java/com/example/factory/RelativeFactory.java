package com.example.factory;

import com.example.model.Player;
import com.example.model.RelativePlayer;
import com.example.dto.RegistrationDTO;
import com.example.validator.StudentEmailValidator;

public class RelativeFactory extends PlayerFactory {

    private final StudentEmailValidator emailValidator = new StudentEmailValidator();

    @Override
    protected Player createPlayer(RegistrationDTO data) {
        RelativePlayer relative = new RelativePlayer();
        relative.setFullName(data.fullName());
        relative.setEmail(data.email());
        return relative;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}