package com.example.core.service;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.factory.*;
import com.example.core.model.Player;
import com.example.core.model.User;
import com.example.repository.PlayerRepository;
import com.example.core.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PasswordEncoder passwordEncoder;

    public PlayerService(PlayerRepository playerRepository, PasswordEncoder passwordEncoder) {
        this.playerRepository = playerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Player registerPlayer(PlayerRegistrationRequest data) {
        log.info("Iniciando registro de jugador con rol: {}, email: {}", data.getUserType(), data.getEmail());
        PlayerFactory factory = getFactoryByRole(data.getUserType());
        Player newPlayer = factory.registerPlayerData(data);
        ((User) newPlayer).setPassword(passwordEncoder.encode(((User) newPlayer).getPassword()));
        User savedPlayer = playerRepository.save((User) newPlayer);
        log.info("Jugador registrado exitosamente - ID: {}, email: {}", savedPlayer.getId(), data.getEmail());
        return savedPlayer;
    }

    public Player searchPlayer(Long id) {
        log.info("Buscando jugador con ID: {}", id);
        Player player = playerRepository.findById(id).orElse(null);
        if (player == null) {
            log.warn("Jugador no encontrado - ID: {}", id);
        } else {
            log.info("Jugador encontrado - ID: {}", id);
        }
        return player;
    }

    public List<Player> getAllPlayers() {
        log.info("Consultando la lista de todos los jugadores");
        List<Player> players = playerRepository.findAll().stream()
                .map(u -> (Player) u)
                .collect(Collectors.toList());
        log.info("Total de jugadores obtenidos: {}", players.size());
        return players;
    }

    public List<Player> getAvailablePlayers() {
        log.info("Consultando jugadores disponibles (agentes libres)");
        List<Player> available = playerRepository.findAll().stream()
                .filter(Player::isAvailable)
                .map(u -> (Player) u)
                .collect(Collectors.toList());
        log.info("Total de jugadores disponibles: {}", available.size());
        return available;
    }

    public List<Player> searchPlayers(String position, String name) {
        log.info("Buscando jugadores con filtros - posición: {}, nombre: {}", position, name);
        List<Player> result = playerRepository.findAll().stream()
                .filter(p -> position == null || position.isBlank() || position.equalsIgnoreCase(p.getPosition()))
                .filter(p -> name == null || name.isBlank() || p.getFullName().toLowerCase().contains(name.toLowerCase()))
                .map(u -> (Player) u)
                .collect(Collectors.toList());
        log.info("Jugadores encontrados con filtros aplicados: {}", result.size());
        return result;
    }

    public void updatePosition(Long id, String position) {
        log.info("Actualizando posición del jugador ID: {} a '{}'", id, position);
        User player = playerRepository.findById(id).orElseThrow(() -> {
            log.warn("Jugador no encontrado al actualizar posición - ID: {}", id);
            return new ResourceNotFoundException("Jugador con ID " + id + " no encontrado");
        });
        player.setPosition(position);
        playerRepository.save(player);
        log.info("Posición actualizada exitosamente para jugador ID: {} -> '{}'", id, position);
    }

    public void updateAvailability(Long id, boolean isAvailable) {
        log.info("Actualizando disponibilidad del jugador ID: {} a {}", id, isAvailable);
        User player = playerRepository.findById(id).orElseThrow(() -> {
            log.warn("Jugador no encontrado al actualizar disponibilidad - ID: {}", id);
            return new ResourceNotFoundException("Jugador con ID " + id + " no encontrado");
        });
        player.setAvailable(isAvailable);
        playerRepository.save(player);
        log.info("Disponibilidad actualizada exitosamente para jugador ID: {} -> {}", id, isAvailable);
    }

    public void updateJerseyNumber(Long id, Integer jerseyNumber) {
        log.info("Actualizando número dorsal del jugador ID: {} a {}", id, jerseyNumber);
        User player = playerRepository.findById(id).orElseThrow(() -> {
            log.warn("Jugador no encontrado al actualizar dorsal - ID: {}", id);
            return new ResourceNotFoundException("Jugador con ID " + id + " no encontrado");
        });
        player.setJerseyNumber(jerseyNumber);
        playerRepository.save(player);
        log.info("Número dorsal actualizado exitosamente para jugador ID: {} -> {}", id, jerseyNumber);
    }

    public void respondToInvitation(Long id, Long teamId, String action) {
        log.info("Procesando respuesta a invitación - jugador ID: {}, equipo ID: {}, acción: {}", id, teamId, action);
        User player = playerRepository.findById(id).orElseThrow(() -> {
            log.warn("Jugador no encontrado al procesar invitación - ID: {}", id);
            return new ResourceNotFoundException("Jugador con ID " + id + " no encontrado");
        });

        if ("ACCEPT".equalsIgnoreCase(action)) {
            player.acceptInvitation(teamId);
            player.setTeamId(teamId);
            player.setAvailable(false);
            log.info("Jugador ID: {} aceptó unirse al equipo ID: {}", id, teamId);
        } else if ("REJECT".equalsIgnoreCase(action)) {
            player.rejectInvitation(teamId);
            log.info("Jugador ID: {} rechazó unirse al equipo ID: {}", id, teamId);
        }

        playerRepository.save(player);
        log.info("Respuesta a invitación procesada exitosamente - jugador ID: {}, acción: {}", id, action);
    }

    private PlayerFactory getFactoryByRole(String role) {
        if (role == null) {
            log.error("El rol no puede ser nulo al obtener la factory de jugador");
            throw new IllegalArgumentException("El rol no puede estar vacío");
        }

        return switch (role.toUpperCase()) {
            case "STUDENT" -> new StudentFactory();
            case "GRADUATE" -> new GraduateFactory();
            case "TEACHER" -> new TeacherFactory();
            case "RELATIVE" -> new RelativeFactory();
            case "ADMIN" -> new AdminFactory();
            default -> {
                log.error("Rol no válido recibido: {}", role);
                throw new IllegalArgumentException("Rol no válido: " + role);
            }
        };
    }
}