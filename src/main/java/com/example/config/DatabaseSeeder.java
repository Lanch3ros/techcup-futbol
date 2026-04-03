package com.example.config;

import com.example.core.model.AdminUser;
import com.example.core.model.OrganizerUser;
import com.example.core.model.RefereeUser;
import com.example.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@techcup.edu.co").isPresent()) {
            log.info("DatabaseSeeder: datos iniciales ya presentes, omitiendo seed.");
            return;
        }

        String encodedPassword = passwordEncoder.encode("Admin123*");

        AdminUser admin = new AdminUser();
        admin.setFullName("Administrador TechCup");
        admin.setEmail("admin@techcup.edu.co");
        admin.setPassword(encodedPassword);
        admin.setRole("ADMIN");

        OrganizerUser organizer = new OrganizerUser();
        organizer.setFullName("Organizador TechCup");
        organizer.setEmail("organizador@techcup.edu.co");
        organizer.setPassword(encodedPassword);
        organizer.setRole("ORGANIZADOR");

        RefereeUser referee = new RefereeUser();
        referee.setFullName("Árbitro TechCup");
        referee.setEmail("arbitro@techcup.edu.co");
        referee.setPassword(encodedPassword);
        referee.setRole("ARBITRO");

        userRepository.save(admin);
        userRepository.save(organizer);
        userRepository.save(referee);

        log.info("DatabaseSeeder: 3 usuarios iniciales creados (ADMIN, ORGANIZER, REFEREE).");
    }
}
