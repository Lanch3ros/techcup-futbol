package com.example.core.factory;

import com.example.core.model.Player;
import com.example.core.model.RelativePlayer;
import com.example.controller.dto.RegistrationDTO;
import com.example.core.validator.GmailValidator;

public class RelativeFactory extends PlayerFactory {

    private final GmailValidator emailValidator = new GmailValidator();

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