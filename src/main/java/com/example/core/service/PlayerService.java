package com.example.core.service;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.factory.*;
import com.example.core.model.Player;
import com.example.repository.PlayerRepository;
import com.example.core.exception.ResourceNotFoundException;
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

    public Player registerPlayer(PlayerRegistrationRequest data) {

        log.info("Iniciando registro de jugador con rol: {}", data.getUserType());
        PlayerFactory factory = getFactoryByRole(data.getUserType());
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

    public void updatePosition(Long id, String position) {
        log.info("Actualizando posición del jugador con ID: {} a {}", id, position);
        Player player = playerRepository.findById(id);

        if (player == null) {
            log.error("No se puede actualizar la posición: Jugador {} no encontrado", id);
            throw new ResourceNotFoundException("Jugador con ID " + id + " no encontrado");
        }

        player.setPosition(position);
        playerRepository.save(player);
        log.info("Posición actualizada correctamente en la base de datos.");
    }

    public void updateAvailability(Long id, boolean isAvailable) {
        log.info("Actualizando disponibilidad del jugador con ID: {} a {}", id, isAvailable);
        Player player = playerRepository.findById(id);

        if (player == null) {
            log.error("No se puede actualizar la disponibilidad: Jugador {} no encontrado", id);
            throw new ResourceNotFoundException("Jugador con ID " + id + " no encontrado");
        }

        player.setAvailable(isAvailable);
        playerRepository.save(player);
        log.info("Disponibilidad actualizada correctamente en la base de datos.");
    }

    public void updateJerseyNumber(Long id, Integer jerseyNumber) {
        log.info("Actualizando número dorsal del jugador con ID: {} a {}", id, jerseyNumber);
        Player player = playerRepository.findById(id);

        if (player == null) {
            log.error("No se puede actualizar el dorsal: Jugador {} no encontrado", id);
            throw new ResourceNotFoundException("Jugador con ID " + id + " no encontrado");
        }

        player.setJerseyNumber(jerseyNumber);
        playerRepository.save(player);
        log.info("Dorsal actualizado correctamente en la base de datos.");
    }

    public void respondToInvitation(Long id, Long teamId, String action) {
        log.info("Procesando respuesta a invitación para el jugador ID: {}, Equipo ID: {}, Acción: {}", id, teamId, action);
        Player player = playerRepository.findById(id);

        if (player == null) {
            log.error("No se puede responder a la invitación: Jugador {} no encontrado", id);
            throw new ResourceNotFoundException("Jugador con ID " + id + " no encontrado");
        }

        if ("ACCEPT".equalsIgnoreCase(action)) {
            player.acceptInvitation(teamId);
            player.setTeamId(teamId);
            player.setAvailable(false);

            log.info("El jugador {} ha aceptado unirse al equipo {}.", id, teamId);
        } else if ("REJECT".equalsIgnoreCase(action)) {
            player.rejectInvitation(teamId);
            log.info("El jugador {} ha rechazado unirse al equipo {}.", id, teamId);
        }

        playerRepository.save(player);
        log.info("Respuesta a la invitación guardada correctamente en la base de datos.");
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