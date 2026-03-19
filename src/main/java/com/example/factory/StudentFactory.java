package com.example.factory;

import com.example.model.Player;
import com.example.model.StudentPlayer;
import com.example.dto.RegistrationDTO;

public class StudentFactory extends PlayerFactory {

    @Override
    protected Player createPlayer(RegistrationDTO data) {
        StudentPlayer student = new StudentPlayer();
        student.setFullName(data.getFullName());
        student.setEmail(data.getEmail());
        return student;
    }

    @Override
    protected boolean validateEmail(String email) {
        return false;
    }
}