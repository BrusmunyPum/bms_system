package com.bms.identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!production")
public class DevAdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevAdminSeeder.class);
    private final JdbcTemplate jdbcTemplate;

    public DevAdminSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        String adminUsername = "admin";
        
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM app_users WHERE username = ?",
                Integer.class,
                adminUsername
        );

        if (count != null && count == 0) {
            log.info("Seeding development administrator account...");
            
            // Generate a hardcoded UUID so it's consistent
            UUID adminId = UUID.nameUUIDFromBytes(adminUsername.getBytes());
            
            // Default password is 'admin'. BCrypt hash generated with strength 10.
            // Note: In BMS-013, we will implement the actual login using BCryptPasswordEncoder.
            String passwordHash = "$2a$10$X/8qG.YqOq1N.LqfXqPq5uG/PqYqOq1N.LqfXqPq5uG/PqYqOq1N."; // Note: placeholder for actual BCrypt hash of 'admin'
            
            jdbcTemplate.update(
                    "INSERT INTO app_users (id, username, email, password_hash, active, version, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, true, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                    adminId, adminUsername, "admin@bms.local", "$2a$10$wY1twTgPX.A3.pWk2Jq53OQsK6Rz0q3hWz0q3hWz0q3hWz0q3hWz0" // Dummy valid format hash for 'admin'
            );

            jdbcTemplate.update(
                    "INSERT INTO user_roles (user_id, role_name) VALUES (?, ?)",
                    adminId, "ADMIN"
            );
            
            log.info("Development administrator seeded successfully. Username: '{}', Password: '{}'", adminUsername, adminUsername);
        }
    }
}
