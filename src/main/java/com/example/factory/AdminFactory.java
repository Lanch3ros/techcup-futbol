package com.example.factory;

import com.example.model.Player;
import com.example.model.AdminPlayer;
import com.example.dto.RegistrationDTO;
import com.example.validator.StudentEmailValidator;

public class AdminFactory extends PlayerFactory {

    private final StudentEmailValidator emailValidator = new StudentEmailValidator();

    @Override
    protected Player createPlayer(RegistrationDTO data) {
        AdminPlayer admin = new AdminPlayer();
        admin.setFullName(data.fullName());
        admin.setEmail(data.email());
        return admin;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}