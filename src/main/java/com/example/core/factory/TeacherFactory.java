package com.example.core.factory;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.model.TeacherPlayer;
import com.example.core.model.User;
import com.example.core.validator.AdminValidator;

public class TeacherFactory extends PlayerFactory {

    private final AdminValidator emailValidator = new AdminValidator();

    @Override
    protected User createUser(PlayerRegistrationRequest data) {
        TeacherPlayer teacher = new TeacherPlayer();
        teacher.setFullName(data.getName());
        teacher.setIdentification(data.getIdentification());
        teacher.setEmail(data.getEmail());
        teacher.setPassword(data.getPassword());
        teacher.setPosition(data.getPosition());
        teacher.setJerseyNumber(data.getJerseyNumber());
        teacher.setBirthDate(data.getBirthDate());
        teacher.setGender(data.getGender());
        teacher.setAvailable(true);
        return teacher;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}
