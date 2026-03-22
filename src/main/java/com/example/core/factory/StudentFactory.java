package com.example.core.factory;

import com.example.core.model.Player;
import com.example.core.model.StudentPlayer;
import com.example.controller.dto.RegistrationDTO;
import com.example.core.validator.StudentEmailValidator;

public class StudentFactory extends PlayerFactory {

    private final StudentEmailValidator emailValidator = new StudentEmailValidator();

    @Override
    protected Player createPlayer(RegistrationDTO data) {
        StudentPlayer student = new StudentPlayer();
        student.setFullName(data.fullName());
        student.setEmail(data.email());
        return student;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}