package com.example.core.service;

import com.example.core.factory.*;
import com.example.controller.dto.RegistrationDTO;
import com.example.core.model.Player;
import com.example.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player registerPlayer(RegistrationDTO data) {
        PlayerFactory factory = getFactoryByRole(data.role());
        Player newPlayer = factory.registerPlayerData(data);

        return playerRepository.save(newPlayer);
    }

    public Player searchPlayer(Long id) {
        return playerRepository.findById(id);
    }

    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
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