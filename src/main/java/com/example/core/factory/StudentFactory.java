package com.example.core.factory;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.model.StudentPlayer;
import com.example.core.model.User;
import com.example.core.validator.StudentEmailValidator;

public class StudentFactory extends PlayerFactory {

    private final StudentEmailValidator emailValidator = new StudentEmailValidator();

    @Override
    protected User createUser(PlayerRegistrationRequest data) {
        StudentPlayer student = new StudentPlayer();
        student.setFullName(data.getName());
        student.setIdentification(data.getIdentification());
        student.setEmail(data.getEmail());
        student.setPassword(data.getPassword());
        student.setProgram(data.getProgram());
        student.setPosition(data.getPosition());
        student.setJerseyNumber(data.getJerseyNumber());
        student.setAvailable(true);
        return student;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}
