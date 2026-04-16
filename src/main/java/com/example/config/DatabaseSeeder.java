package com.example.config;

import com.example.core.model.AdminUser;
import com.example.core.model.OrganizerUser;
import com.example.core.model.RefereeUser;
import com.example.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String defaultAdminPassword;

    public DatabaseSeeder(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.seed.admin-password:Admin123*}") String defaultAdminPassword) {
        this.userRepository       = userRepository;
        this.passwordEncoder      = passwordEncoder;
        this.defaultAdminPassword = defaultAdminPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail("admin@techcup.edu.co").isPresent()) {
            log.info("DatabaseSeeder: datos iniciales ya presentes, omitiendo seed.");
            return;
        }

        String encodedPassword = passwordEncoder.encode(defaultAdminPassword);

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
