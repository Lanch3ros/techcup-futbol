package com.example.core.service;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.exception.BusinessRuleException;
import com.example.core.factory.*;
import com.example.core.model.Invitation;
import com.example.core.model.Player;
import com.example.core.model.RefereeUser;
import com.example.core.model.User;
import com.example.repository.InvitationRepository;
import com.example.repository.UserRepository;
import com.example.core.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PlayerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InvitationRepository invitationRepository;

    public PlayerService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                         InvitationRepository invitationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.invitationRepository = invitationRepository;
    }

    public User registerPlayer(PlayerRegistrationRequest data) {
        normalizeIncomingRegistration(data);
        log.info("Iniciando registro con tipo: {}, email: {}", data.getUserType(), data.getEmail());
        if ("REFEREE".equalsIgnoreCase(data.getUserType())) {
            return registerRefereeUser(data);
        }

        PlayerFactory factory = getFactoryByRole(data.getUserType());
        User newUser = factory.registerPlayerData(data);
        if (!(newUser instanceof Player)) {
            throw new BusinessRuleException("El tipo de usuario '" + data.getUserType() + "' no es un jugador registrable.");
        }
        if (userRepository.existsByEmail(data.getEmail())) {
            throw new BusinessRuleException("El correo '" + data.getEmail() + "' ya está registrado.");
        }
        if (data.getIdentification() != null && !data.getIdentification().isBlank()
                && userRepository.existsByIdentification(data.getIdentification())) {
            throw new BusinessRuleException("La identificación '" + data.getIdentification() + "' ya está registrada.");
        }
        if (data.getSecurityRole() != null && !data.getSecurityRole().isBlank()) {
            newUser.setRole(data.getSecurityRole());
        }
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        User savedUser = userRepository.save(newUser);
        log.info("Jugador registrado exitosamente - ID: {}, email: {}", savedUser.getId(), data.getEmail());
        return savedUser;
    }

    private User registerRefereeUser(PlayerRegistrationRequest data) {
        if (userRepository.existsByEmail(data.getEmail())) {
            throw new BusinessRuleException("El correo '" + data.getEmail() + "' ya está registrado.");
        }
        String license = data.getIdentification() != null ? data.getIdentification().trim() : "";
        if (license.isBlank()) {
            throw new BusinessRuleException("La identificación / número de licencia es obligatorio para árbitros.");
        }
        if (userRepository.existsByIdentification(license)) {
            throw new BusinessRuleException("La identificación '" + license + "' ya está registrada.");
        }
        if (userRepository.existsByLicenseNumber(license)) {
            throw new BusinessRuleException("El número de licencia '" + license + "' ya está registrado.");
        }

        RefereeUser referee = new RefereeUser();
        referee.setFullName(data.getName());
        referee.setEmail(data.getEmail().trim().toLowerCase(Locale.ROOT));
        referee.setIdentification(license);
        referee.setLicenseNumber(license);
        referee.setPassword(passwordEncoder.encode(data.getPassword()));
        referee.setRole("ARBITRO");
        if (data.getSkillLevel() != null && !data.getSkillLevel().isBlank()) {
            referee.setCertificationLevel(data.getSkillLevel());
        }
        if (data.getBirthDate() != null) {
            referee.setBirthDate(data.getBirthDate());
        }
        if (data.getGender() != null && !data.getGender().isBlank()) {
            referee.setGender(data.getGender());
        }

        User saved = userRepository.save(referee);
        log.info("Árbitro registrado exitosamente - ID: {}, email: {}", saved.getId(), data.getEmail());
        return saved;
    }

    public Player searchPlayer(Long id) {
        log.info("Buscando jugador con ID: {}", id);
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            log.warn("Jugador no encontrado - ID: {}", id);
            return null;
        }
        log.info("Jugador encontrado - ID: {}", id);
        return user instanceof Player ? (Player) user : null;
    }

    public List<Player> getAllPlayers() {
        log.info("Consultando la lista de todos los jugadores");
        List<Player> players = userRepository.findAll().stream()
                .filter(u -> u instanceof Player)
                .map(u -> (Player) u)
                .collect(Collectors.toList());
        log.info("Total de jugadores obtenidos: {}", players.size());
        return players;
    }

    public List<Player> getAvailablePlayers() {
        log.info("Consultando jugadores disponibles (agentes libres)");
        List<Player> available = userRepository.findAll().stream()
                .filter(u -> u instanceof Player)
                .map(u -> (Player) u)
                .filter(Player::isAvailable)
                .collect(Collectors.toList());
        log.info("Total de jugadores disponibles: {}", available.size());
        return available;
    }

    public List<Player> searchPlayers(String position, String name) {
        log.info("Buscando jugadores con filtros - posición: {}, nombre: {}", position, name);
        List<Player> result = userRepository.findAll().stream()
                .filter(u -> u instanceof Player)
                .map(u -> (Player) u)
                .filter(p -> position == null || position.isBlank() || position.equalsIgnoreCase(p.getPosition()))
                .filter(p -> name == null || name.isBlank() || p.getFullName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
        log.info("Jugadores encontrados con filtros aplicados: {}", result.size());
        return result;
    }

    public void updatePosition(Long id, String position) {
        log.info("Actualizando posición del jugador ID: {} a '{}'", id, position);
        User player = userRepository.findById(id).orElseThrow(() -> {
            log.warn("Jugador no encontrado al actualizar posición - ID: {}", id);
            return new ResourceNotFoundException("Jugador con ID " + id + " no encontrado");
        });
        player.setPosition(position);
        userRepository.save(player);
        log.info("Posición actualizada exitosamente para jugador ID: {} -> '{}'", id, position);
    }

    public void updateAvailability(Long id, boolean isAvailable) {
        log.info("Actualizando disponibilidad del jugador ID: {} a {}", id, isAvailable);
        User player = userRepository.findById(id).orElseThrow(() -> {
            log.warn("Jugador no encontrado al actualizar disponibilidad - ID: {}", id);
            return new ResourceNotFoundException("Jugador con ID " + id + " no encontrado");
        });
        player.setAvailable(isAvailable);
        userRepository.save(player);
        log.info("Disponibilidad actualizada exitosamente para jugador ID: {} -> {}", id, isAvailable);
    }

    public void updateJerseyNumber(Long id, Integer jerseyNumber) {
        log.info("Actualizando número dorsal del jugador ID: {} a {}", id, jerseyNumber);
        User player = userRepository.findById(id).orElseThrow(() -> {
            log.warn("Jugador no encontrado al actualizar dorsal - ID: {}", id);
            return new ResourceNotFoundException("Jugador con ID " + id + " no encontrado");
        });
        player.setJerseyNumber(jerseyNumber);
        userRepository.save(player);
        log.info("Número dorsal actualizado exitosamente para jugador ID: {} -> {}", id, jerseyNumber);
    }

    public void respondToInvitation(Long id, Long teamId, String action) {
        log.info("Procesando respuesta a invitación - jugador ID: {}, equipo ID: {}, acción: {}", id, teamId, action);
        User player = userRepository.findById(id).orElseThrow(() -> {
            log.warn("Jugador no encontrado al procesar invitación - ID: {}", id);
            return new ResourceNotFoundException("Jugador con ID " + id + " no encontrado");
        });

        if ("ACCEPT".equalsIgnoreCase(action)) {
            player.setTeamId(teamId);
            player.setAvailable(false);
            if (player instanceof Player) ((Player) player).acceptInvitation(teamId);
            log.info("Jugador ID: {} aceptó unirse al equipo ID: {}", id, teamId);
        } else if ("REJECT".equalsIgnoreCase(action)) {
            if (player instanceof Player) ((Player) player).rejectInvitation(teamId);
            log.info("Jugador ID: {} rechazó unirse al equipo ID: {}", id, teamId);
        }

        userRepository.save(player);
        log.info("Respuesta a invitación procesada exitosamente - jugador ID: {}, acción: {}", id, action);
    }

    /**
     * RF-11 / RN-11-3: Procesa la respuesta del jugador a una invitación.
     * Si acepta, vincula al equipo, marca no-disponible y rechaza automáticamente
     * todas las demás invitaciones pendientes del mismo jugador.
     */
    public void processInvitationResponse(Long invitationId, String action) {
        log.info("Procesando respuesta a invitación ID: {}, acción: {}", invitationId, action);

        Invitation invitation = invitationRepository.findById(invitationId).orElseThrow(() -> {
            log.warn("Invitación no encontrada - ID: {}", invitationId);
            return new ResourceNotFoundException("Invitación con ID " + invitationId + " no encontrada");
        });

        if (!Invitation.PENDING.equalsIgnoreCase(invitation.getStatus())) {
            log.warn("Invitación ID: {} ya fue procesada - estado actual: {}", invitationId, invitation.getStatus());
            throw new BusinessRuleException("Esta invitación ya fue procesada (estado: " + invitation.getStatus() + ").");
        }

        if ("ACCEPT".equalsIgnoreCase(action)) {
            User player = userRepository.findById(invitation.getPlayerId()).orElseThrow(() ->
                    new ResourceNotFoundException("Jugador con ID " + invitation.getPlayerId() + " no encontrado"));

            player.setTeamId(invitation.getTeamId());
            player.setAvailable(false);
            userRepository.save(player);

            invitation.setStatus(Invitation.ACCEPTED);
            invitationRepository.save(invitation);

            // RN-11-3: rechazar automáticamente todas las demás invitaciones pendientes
            List<Invitation> otherPending = invitationRepository
                    .findByPlayerIdAndStatusIgnoreCase(invitation.getPlayerId(), Invitation.PENDING);
            otherPending.forEach(inv -> inv.setStatus(Invitation.REJECTED));
            invitationRepository.saveAll(otherPending);

            log.info("Jugador ID: {} vinculado al equipo ID: {}. {} invitaciones pendientes rechazadas (RN-11-3)",
                    player.getId(), invitation.getTeamId(), otherPending.size());

        } else if ("REJECT".equalsIgnoreCase(action)) {
            invitation.setStatus(Invitation.REJECTED);
            invitationRepository.save(invitation);
            log.info("Invitación ID: {} rechazada por el jugador", invitationId);
        }
    }

    private PlayerFactory getFactoryByRole(String role) {
        if (role == null) {
            log.error("El rol no puede ser nulo al obtener la factory de jugador");
            throw new IllegalArgumentException("El rol no puede estar vacío");
        }

        return switch (role.toUpperCase(Locale.ROOT)) {
            case "STUDENT", "PLAYER" -> new StudentFactory();
            case "GRADUATE" -> new GraduateFactory();
            case "TEACHER"  -> new TeacherFactory();
            case "RELATIVE" -> new RelativeFactory();
            case "STAFF"    -> new StaffFactory();
            default -> {
                log.error("Rol no válido recibido: {}", role);
                throw new IllegalArgumentException("Rol no válido: " + role);
            }
        };
    }

    private static boolean isCaptainUserType(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String u = raw.trim().toUpperCase(Locale.ROOT);
        return "CAPTAIN".equals(u) || "CAPITAN".equals(u) || "CAPTAN".equals(u);
    }

    private static boolean isRefereeUserType(String raw) {
        if (raw == null || raw.isBlank()) {
            return false;
        }
        String u = raw.trim().toUpperCase(Locale.ROOT);
        return "REFEREE".equals(u) || "ARBITRO".equals(u);
    }

    /**
     * Alinea payloads del front (firstName/lastName, PLAYER/CAPTAIN/REFEREE, posición, dorsal, etc.)
     * con los campos que usan las factories o el registro de árbitro.
     */
    private void normalizeIncomingRegistration(PlayerRegistrationRequest data) {
        String rawType = data.getUserType() != null ? data.getUserType().trim() : "";

        if (isCaptainUserType(rawType)) {
            data.setSecurityRole("CAPITAN");
            data.setUserType("STUDENT");
        } else if (isRefereeUserType(rawType)) {
            data.setUserType("REFEREE");
        } else if ("PLAYER".equalsIgnoreCase(rawType) || "JUGADOR".equalsIgnoreCase(rawType)) {
            data.setUserType("STUDENT");
        }

        if ((data.getName() == null || data.getName().isBlank())
                && data.getFirstName() != null && data.getLastName() != null) {
            data.setName(data.getFirstName().trim() + " " + data.getLastName().trim());
        }

        boolean referee = "REFEREE".equalsIgnoreCase(data.getUserType());

        if (!referee && data.getPosition() != null && !data.getPosition().isBlank()) {
            String lower = data.getPosition().trim().toLowerCase(Locale.ROOT);
            data.setPosition(switch (lower) {
                case "portero" -> "Portero";
                case "defensa" -> "Defensa";
                case "volante" -> "Volante";
                case "delantero" -> "Delantero";
                default -> data.getPosition().trim();
            });
        }
        if (!referee && data.getBirthDate() == null && data.getAge() != null && data.getAge() > 0) {
            data.setBirthDate(LocalDate.now().minusYears(data.getAge()));
        }
        if (!referee && (data.getJerseyNumber() == null || data.getJerseyNumber() < 1 || data.getJerseyNumber() > 99)) {
            data.setJerseyNumber(10);
        }
        if (!referee && (data.getPosition() == null || data.getPosition().isBlank())) {
            data.setPosition("Delantero");
        }
    }
}

