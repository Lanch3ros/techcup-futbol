package com.example.core.service;

import com.example.core.factory.*;
import com.example.controller.dto.RegistrationDTO;
import com.example.core.model.Player;
import com.example.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Player registerPlayer(RegistrationDTO data) {
        log.info("Iniciando registro de jugador con rol: {}", data.role());

        PlayerFactory factory = getFactoryByRole(data.role());
        Player newPlayer = factory.registerPlayerData(data);
        Player savedPlayer = playerRepository.save(newPlayer);

        log.info("Jugador registrado exitosamente con ID: {}", savedPlayer.getId());
        return savedPlayer;
    }

    public Player searchPlayer(Long id) {
        log.info("Buscando jugador con ID: {}", id);

        Player player = playerRepository.findById(id);
        if (player != null) {
            log.info("Jugador encontrado exitosamente.");
        } else {
            log.warn("No se encontró ningún jugador con el ID: {}", id);
        }

        return player;
    }

    public List<Player> getAllPlayers() {
        log.info("Consultando la lista de todos los jugadores registrados.");

        List<Player> players = playerRepository.findAll();
        log.info("Se obtuvieron {} jugadores de la base de datos.", players.size());

        return players;
    }

    private PlayerFactory getFactoryByRole(String role) {
        if (role == null) {
            log.error("Error al obtener la fábrica de creación: el rol es nulo.");
            throw new IllegalArgumentException("El rol no puede estar vacío");
        }

        return switch (role.toUpperCase()) {
            case "STUDENT" -> new StudentFactory();
            case "GRADUATE" -> new GraduateFactory();
            case "TEACHER" -> new TeacherFactory();
            case "RELATIVE" -> new RelativeFactory();
            case "ADMIN" -> new AdminFactory();
            default -> {
                log.error("Error al obtener la fábrica de creación: rol no válido ({})", role);
                throw new IllegalArgumentException("Rol no válido: " + role);
            }
        };
    }
}