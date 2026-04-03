package com.example.core.factory;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.model.GraduatePlayer;
import com.example.core.model.User;
import com.example.core.validator.StudentEmailValidator;

public class GraduateFactory extends PlayerFactory {

    private final StudentEmailValidator emailValidator = new StudentEmailValidator();

    @Override
    protected User createUser(PlayerRegistrationRequest data) {
        GraduatePlayer graduate = new GraduatePlayer();
        graduate.setFullName(data.getName());
        graduate.setIdentification(data.getIdentification());
        graduate.setEmail(data.getEmail());
        graduate.setPassword(data.getPassword());
        graduate.setProgram(data.getProgram());
        graduate.setPosition(data.getPosition());
        graduate.setJerseyNumber(data.getJerseyNumber());
        graduate.setAvailable(true);
        return graduate;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}
