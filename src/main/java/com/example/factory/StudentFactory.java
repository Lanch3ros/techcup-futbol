package com.example.factory;

import com.example.model.Player;
import com.example.model.StudentPlayer;
import com.example.dto.RegistrationDTO;
import com.example.validator.StudentEmailValidator;

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