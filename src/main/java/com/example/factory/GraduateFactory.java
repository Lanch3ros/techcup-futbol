package com.example.factory;

import com.example.model.Player;
import com.example.model.GraduatePlayer;
import com.example.dto.RegistrationDTO;
import com.example.validator.StudentEmailValidator;

public class GraduateFactory extends PlayerFactory {

    private final StudentEmailValidator emailValidator = new StudentEmailValidator();

    @Override
    protected Player createPlayer(RegistrationDTO data) {
        GraduatePlayer graduate = new GraduatePlayer();
        graduate.setFullName(data.getFullName());
        graduate.setEmail(data.getEmail());
        return graduate;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}
