package com.example.core.factory;

import com.example.core.model.Player;
import com.example.core.model.TeacherPlayer;
import com.example.controller.dto.RegistrationDTO;
import com.example.core.validator.AdminValidator;

public class TeacherFactory extends PlayerFactory {

    private final AdminValidator emailValidator = new AdminValidator();

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