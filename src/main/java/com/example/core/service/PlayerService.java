package com.example.core.service;

import com.example.core.factory.*;
import com.example.controller.dto.RegistrationDTO;
import com.example.core.model.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlayerService {
    private final List<Player> registeredPlayers = new ArrayList<>();

    public Player registerPlayer(RegistrationDTO data) {
        PlayerFactory factory = getFactoryByRole(data.role());
        Player newPlayer = factory.registerPlayerData(data);
        registeredPlayers.add(newPlayer);
        return newPlayer;
    }

    public Player searchPlayer(Long id) {
        if (id != null && id >= 0 && id < registeredPlayers.size()) {
            return registeredPlayers.get(id.intValue());
        }
        return null;
    }

    private PlayerFactory getFactoryByRole(String role) {
        if (role == null) {
            throw new IllegalArgumentException("El rol no puede estar vacío");
        }
        return switch (role.toUpperCase()) {
            case "STUDENT" -> new StudentFactory();
            case "GRADUATE" -> new GraduateFactory();
            case "TEACHER" -> new TeacherFactory();
            case "RELATIVE" -> new RelativeFactory();
            case "ADMIN" -> new AdminFactory();
            default -> throw new IllegalArgumentException("Rol no válido: " + role);
        };
    }
}
