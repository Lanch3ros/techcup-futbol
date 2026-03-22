package com.example.factory;

import com.example.model.Player;
import com.example.model.TeacherPlayer;
import com.example.dto.RegistrationDTO;
import com.example.validator.StudentEmailValidator;

public class TeacherFactory extends PlayerFactory {

    private final StudentEmailValidator emailValidator = new StudentEmailValidator();

    @Override
    protected Player createPlayer(RegistrationDTO data) {
        TeacherPlayer teacher = new TeacherPlayer();
        teacher.setFullName(data.fullName());
        teacher.setEmail(data.email());
        return teacher;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}