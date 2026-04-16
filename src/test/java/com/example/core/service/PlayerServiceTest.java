package com.example.core.service;

import com.example.controller.dto.request.PlayerRegistrationRequest;
import com.example.core.model.AdminUser;
import com.example.core.model.Player;
import com.example.core.model.RefereeUser;
import com.example.core.model.StudentPlayer;
import com.example.core.model.User;
import com.example.repository.InvitationRepository;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import com.example.core.exception.BusinessRuleException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


class PlayerServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private InvitationRepository invitationRepository;
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        userRepository    = Mockito.mock(UserRepository.class);
        passwordEncoder     = Mockito.mock(PasswordEncoder.class);
        invitationRepository = Mockito.mock(InvitationRepository.class);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashed");
        playerService = new PlayerService(userRepository, passwordEncoder, invitationRepository);
    }

    private PlayerRegistrationRequest buildRequest(String name, String email, String role) {
        PlayerRegistrationRequest request = new PlayerRegistrationRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword("12345678");
        request.setUserType(role);
        request.setJerseyNumber(10);
        request.setPosition("Delantero");
        return request;
    }

    @Test
    void registerPlayer_Student_Success() {
        PlayerRegistrationRequest data = buildRequest("Jose", "jose@mail.escuelaing.edu.co", "STUDENT");

        StudentPlayer mockPlayer = new StudentPlayer();
        mockPlayer.setId(1L);

        when(userRepository.save(any(User.class))).thenReturn(mockPlayer);

        User result = playerService.registerPlayer(data);

        assertNotNull(result);
        assertEquals(1L, ((Player) result).getId());
    }

    @Test
    void registerPlayer_FrontPayload_PlayerFirstLast_DerivesStudent() {
        PlayerRegistrationRequest data = new PlayerRegistrationRequest();
        data.setUserType("PLAYER");
        data.setEmail("andres@mail.escuelaing.edu.co");
        data.setPassword("12345678");
        data.setFirstName("Andres");
        data.setLastName("Vivas");
        data.setIdentification("1069720788");
        data.setPosition("DELANTERO");
        data.setAge(20);

        when(userRepository.save(any(User.class))).thenReturn(new StudentPlayer());

        assertDoesNotThrow(() -> playerService.registerPlayer(data));
        assertEquals("STUDENT", data.getUserType());
        assertEquals("Andres Vivas", data.getName());
        assertEquals("Delantero", data.getPosition());
        assertEquals(10, data.getJerseyNumber());
        assertNotNull(data.getBirthDate());
    }

    @Test
    void registerPlayer_Captain_SetsCapitanRole() {
        PlayerRegistrationRequest data = new PlayerRegistrationRequest();
        data.setUserType("CAPTAIN");
        data.setEmail("cap@mail.escuelaing.edu.co");
        data.setPassword("12345678");
        data.setFirstName("Ana");
        data.setLastName("Cap");
        data.setIdentification("999");
        data.setPosition("Delantero");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByIdentification("999")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = playerService.registerPlayer(data);
        assertEquals("CAPITAN", saved.getRole());
        assertTrue(saved instanceof StudentPlayer);
    }

    @Test
    void registerPlayer_Referee_Success() {
        PlayerRegistrationRequest data = new PlayerRegistrationRequest();
        data.setUserType("REFEREE");
        data.setName("Árbitro Nuevo");
        data.setEmail("arb@test.com");
        data.setPassword("12345678");
        data.setIdentification("LIC-001");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByIdentification("LIC-001")).thenReturn(false);
        when(userRepository.existsByLicenseNumber("LIC-001")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(77L);
            return u;
        });

        User saved = playerService.registerPlayer(data);
        assertTrue(saved instanceof RefereeUser);
        assertEquals("ARBITRO", saved.getRole());
        assertEquals("LIC-001", saved.getIdentification());
    }

    @Test
    void registerPlayer_ArbitroAlias_SameAsReferee() {
        PlayerRegistrationRequest data = new PlayerRegistrationRequest();
        data.setUserType("ARBITRO");
        data.setName("Árbitro Dos");
        data.setEmail("arb2@test.com");
        data.setPassword("12345678");
        data.setIdentification("LIC-002");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByIdentification("LIC-002")).thenReturn(false);
        when(userRepository.existsByLicenseNumber("LIC-002")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User saved = playerService.registerPlayer(data);
        assertTrue(saved instanceof RefereeUser);
    }

    @Test
    void registerPlayer_Graduate_Success() {
        PlayerRegistrationRequest data = buildRequest("Ana", "ana@mail.escuelaing.edu.co", "GRADUATE");
        when(userRepository.save(any(User.class))).thenReturn(new StudentPlayer());
        assertDoesNotThrow(() -> playerService.registerPlayer(data));
    }

    @Test
    void registerPlayer_Teacher_Success() {
        PlayerRegistrationRequest data = buildRequest("Prof", "prof@escuelaing.edu.co", "TEACHER");
        when(userRepository.save(any(User.class))).thenReturn(new StudentPlayer());
        assertDoesNotThrow(() -> playerService.registerPlayer(data));
    }

    @Test
    void registerPlayer_Relative_Success() {
        PlayerRegistrationRequest data = buildRequest("Fam", "fam@gmail.com", "RELATIVE");
        when(userRepository.save(any(User.class))).thenReturn(new StudentPlayer());
        assertDoesNotThrow(() -> playerService.registerPlayer(data));
    }

    @Test
    void registerPlayer_PasswordIsEncoded() {
        PlayerRegistrationRequest data = buildRequest("Jose", "jose@mail.escuelaing.edu.co", "STUDENT");

        StudentPlayer mockPlayer = new StudentPlayer();
        mockPlayer.setId(1L);

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            // La contraseña guardada NO debe ser el texto plano original
            assertNotEquals("12345678", saved.getPassword(), "La contraseña debe estar encriptada antes de persistir");
            assertEquals("$2a$10$hashed", saved.getPassword());
            return mockPlayer;
        });

        playerService.registerPlayer(data);

        verify(passwordEncoder).encode("12345678");
    }

    @Test
    void registerPlayer_Staff_Success() {
        PlayerRegistrationRequest data = buildRequest("Staff", "staff@escuelaing.edu.co", "STAFF");
        when(userRepository.save(any(User.class))).thenReturn(new StudentPlayer());
        assertDoesNotThrow(() -> playerService.registerPlayer(data));
    }

    @Test
    void registerPlayer_Staff_InvalidEmail_ThrowsException() {
        PlayerRegistrationRequest data = buildRequest("Staff", "staff@gmail.com", "STAFF");
        assertThrows(IllegalArgumentException.class, () -> playerService.registerPlayer(data));
    }

    @Test
    void registerPlayer_InvalidEmailDomain_ThrowsException() {
        PlayerRegistrationRequest data = buildRequest("Jose", "jose@gmail.com", "STUDENT");
        assertThrows(IllegalArgumentException.class, () -> playerService.registerPlayer(data));
    }

    @Test
    void registerPlayer_NullRole_ThrowsException() {
        PlayerRegistrationRequest data = buildRequest("Jose", "jose@mail.com", null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> playerService.registerPlayer(data));
        assertEquals("El rol no puede estar vacío", exception.getMessage());
    }

    @Test
    void registerPlayer_InvalidRole_ThrowsException() {
        PlayerRegistrationRequest data = buildRequest("Jose", "jose@mail.com", "GOKU");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> playerService.registerPlayer(data));
        assertEquals("Rol no válido: GOKU", exception.getMessage());
    }

    @Test
    void registerPlayer_DuplicateEmail_ThrowsBusinessRuleException() {
        PlayerRegistrationRequest data = buildRequest("Jose", "jose@mail.escuelaing.edu.co", "STUDENT");
        when(userRepository.existsByEmail("jose@mail.escuelaing.edu.co")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> playerService.registerPlayer(data));
        assertTrue(ex.getMessage().contains("ya está registrado"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerPlayer_DuplicateIdentification_ThrowsBusinessRuleException() {
        PlayerRegistrationRequest data = buildRequest("Jose", "jose@mail.escuelaing.edu.co", "STUDENT");
        data.setIdentification("IDA001");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByIdentification("IDA001")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> playerService.registerPlayer(data));
        assertTrue(ex.getMessage().contains("ya está registrada"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void searchPlayer_Found_ReturnsPlayer() {
        StudentPlayer mockPlayer = new StudentPlayer();
        mockPlayer.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockPlayer));

        Player result = playerService.searchPlayer(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void searchPlayer_NotFound_ReturnsNull() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Player result = playerService.searchPlayer(99L);

        assertNull(result);
    }

    @Test
    void searchPlayer_UserNotPlayer_ReturnsNull() {
        // AdminUser extends User but does NOT implement Player
        AdminUser admin = new AdminUser();
        admin.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(admin));

        Player result = playerService.searchPlayer(5L);

        assertNull(result); // instanceof Player = false → null
    }

    @Test
    void getAllPlayers_ReturnsList() {
        when(userRepository.findAll()).thenReturn(List.of(new StudentPlayer()));

        List<Player> result = playerService.getAllPlayers();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
