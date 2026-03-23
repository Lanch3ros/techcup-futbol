package com.example.core.factory;

import com.example.core.model.Player;
import com.example.core.model.AdminPlayer;
import com.example.controller.dto.RegistrationDTO;
import com.example.core.validator.AdminValidator;

public class AdminFactory extends PlayerFactory {

    private final AdminValidator emailValidator = new AdminValidator();

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