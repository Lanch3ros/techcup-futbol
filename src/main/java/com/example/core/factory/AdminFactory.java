package com.example.core.factory;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.model.Player;
import com.example.core.model.AdminPlayer;
import com.example.core.validator.AdminValidator;

public class AdminFactory extends PlayerFactory {

    private final AdminValidator emailValidator = new AdminValidator();

    @Override
    protected Player createPlayer(PlayerRegistrationRequest data) {
        AdminPlayer admin = new AdminPlayer();
        admin.setFullName(data.getName());
        admin.setEmail(data.getEmail());
        admin.setPassword(data.getPassword());
        admin.setPosition(data.getPosition());
        admin.setJerseyNumber(data.getJerseyNumber());
        admin.setAvailable(true);
        return admin;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}