package com.example.core.factory;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.model.Player;
import com.example.core.model.StudentPlayer;
import com.example.core.validator.StudentEmailValidator;

public class StudentFactory extends PlayerFactory {

    private final StudentEmailValidator emailValidator = new StudentEmailValidator();

    @Override
    protected Player createPlayer(PlayerRegistrationRequest data) {
        StudentPlayer student = new StudentPlayer();
        student.setFullName(data.getName());
        student.setEmail(data.getEmail());
        return student;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}