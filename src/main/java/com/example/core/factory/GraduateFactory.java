package com.example.core.factory;

import com.example.core.model.Player;
import com.example.core.model.GraduatePlayer;
import com.example.controller.dto.RegistrationDTO;
import com.example.core.validator.StudentEmailValidator;

public class GraduateFactory extends PlayerFactory {

    private final StudentEmailValidator emailValidator = new StudentEmailValidator();

    @Override
    protected Player createPlayer(RegistrationDTO data) {
        GraduatePlayer graduate = new GraduatePlayer();
        graduate.setFullName(data.fullName());
        graduate.setEmail(data.email());
        return graduate;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}
