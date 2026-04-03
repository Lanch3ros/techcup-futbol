package com.example.core.factory;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.model.AdminUser;
import com.example.core.model.User;
import com.example.core.validator.AdminValidator;

public class AdminFactory extends PlayerFactory {

    private final AdminValidator emailValidator = new AdminValidator();

    @Override
    protected User createUser(PlayerRegistrationRequest data) {
        AdminUser admin = new AdminUser();
        admin.setFullName(data.getName());
        admin.setIdentification(data.getIdentification());
        admin.setEmail(data.getEmail());
        admin.setPassword(data.getPassword());
        admin.setPosition(data.getPosition());
        admin.setJerseyNumber(data.getJerseyNumber());
        admin.setAvailable(true);
        return admin;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}
