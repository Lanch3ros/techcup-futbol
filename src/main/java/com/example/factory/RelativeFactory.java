package com.example.factory;

import com.example.model.Player;
import com.example.model.RelativePlayer;
import com.example.dto.RegistrationDTO;

public class RelativeFactory extends PlayerFactory {

    @Override
    protected Player createPlayer(RegistrationDTO data) {
        RelativePlayer relative = new RelativePlayer();
        relative.setFullName(data.getFullName());
        relative.setEmail(data.getEmail());
        return relative;
    }

    @Override
    protected boolean validateEmail(String email) {
        return false;
    }
}