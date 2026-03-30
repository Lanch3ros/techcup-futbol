package com.example.core.factory;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.model.Player;
import com.example.core.model.RelativePlayer;
import com.example.core.validator.GmailValidator;

public class RelativeFactory extends PlayerFactory {

    private final GmailValidator emailValidator = new GmailValidator();

    @Override
    protected Player createPlayer(PlayerRegistrationRequest data) {
        RelativePlayer relative = new RelativePlayer();
        relative.setFullName(data.getName());
        relative.setEmail(data.getEmail());
        relative.setPassword(data.getPassword());
        relative.setPosition(data.getPosition());
        relative.setJerseyNumber(data.getJerseyNumber());
        relative.setAvailable(true);
        return relative;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}