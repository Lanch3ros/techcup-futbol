package com.example.service;

import com.example.model.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
        userService = new UserService(userRepository);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        User user = new User(1234,
                        "Jose",
                        "jose.lancheros-a@mail.escuelaing.edu.co",
                        "nosejose",
                        "Capitan");
        assertNotNull(userService.registerUser(user));
        assertEquals("Jose", user.getName());
    }

    @Test
    void shouldReturnAllUsers() {
        User user = new User(4321,
                "Dana",
                "dana.leal-g@mail.escuelaing.edu.co",
                "danita",
                "Jugador");
        userService.registerUser(user);
        assertEquals(1, userService.getAllUsers().size());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsMissing() {
        assertThrows(IllegalArgumentException.class, () -> userService.registerUser(new User()));
    }
}
