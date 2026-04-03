package com.example.core.factory;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.model.StaffPlayer;
import com.example.core.model.User;
import com.example.core.validator.StaffEmailValidator;

public class StaffFactory extends PlayerFactory {

    private final StaffEmailValidator emailValidator = new StaffEmailValidator();

    @Override
    protected User createUser(PlayerRegistrationRequest data) {
        StaffPlayer staff = new StaffPlayer();
        staff.setFullName(data.getName());
        staff.setIdentification(data.getIdentification());
        staff.setEmail(data.getEmail());
        staff.setPassword(data.getPassword());
        staff.setPosition(data.getPosition());
        staff.setJerseyNumber(data.getJerseyNumber());
        staff.setBirthDate(data.getBirthDate());
        staff.setGender(data.getGender());
        staff.setAvailable(true);
        return staff;
    }

    @Override
    protected boolean validateEmail(String email) {
        return emailValidator.validate(email);
    }
}
