package com.example.factory;

import com.example.model.Player;
import com.example.model.TeacherPlayer;
import com.example.dto.RegistrationDTO;

public class TeacherFactory extends PlayerFactory {

    @Override
    protected Player createPlayer(RegistrationDTO data) {
        TeacherPlayer teacher = new TeacherPlayer();
        teacher.setFullName(data.getFullName());
        teacher.setEmail(data.getEmail());
        return teacher;
    }

    @Override
    protected boolean validateEmail(String email) {
        return false;
    }
}