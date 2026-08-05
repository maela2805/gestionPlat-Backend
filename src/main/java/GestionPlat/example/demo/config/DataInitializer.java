package GestionPlat.example.demo.config;

import GestionPlat.example.demo.modules.auth.model.Role;
import GestionPlat.example.demo.modules.auth.model.User;
import GestionPlat.example.demo.modules.auth.repository.RoleRepository;
import GestionPlat.example.demo.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE products ALTER COLUMN image_url TYPE TEXT;");
            jdbcTemplate.execute("ALTER TABLE products ALTER COLUMN description TYPE TEXT;");
            jdbcTemplate.execute("ALTER TABLE inventories ALTER COLUMN boutique_id DROP NOT NULL;");
            jdbcTemplate.execute("ALTER TABLE stock_movements ALTER COLUMN boutique_id DROP NOT NULL;");
            jdbcTemplate.execute("ALTER TABLE invoices DROP CONSTRAINT IF EXISTS invoices_type_check;");
            // Ensure BL columns exist (added for persistence of delivery info)
            jdbcTemplate.execute("ALTER TABLE invoices ADD COLUMN IF NOT EXISTS driver_name VARCHAR(255);");
            jdbcTemplate.execute("ALTER TABLE invoices ADD COLUMN IF NOT EXISTS driver_phone VARCHAR(255);");
            jdbcTemplate.execute("ALTER TABLE invoices ADD COLUMN IF NOT EXISTS vehicle_registration VARCHAR(255);");
            jdbcTemplate.execute("ALTER TABLE invoices ADD COLUMN IF NOT EXISTS attachment_url TEXT;");
            log.info("Successfully ensured table schema constraints");
        } catch (Exception e) {
            log.warn("Column alter execution notice: {}", e.getMessage());
        }


        // Ensure default roles exist
        Role superAdminRole = roleRepository.findByName("ROLE_SUPER_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_SUPER_ADMIN")
                        .description("Accès total à la plateforme")
                        .build()));

        roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_ADMIN")
                        .description("Administration et paramétrage")
                        .build()));

        roleRepository.findByName("ROLE_MANAGER")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_MANAGER")
                        .description("Gestion opérationnelle stock et commandes")
                        .build()));

        roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_EMPLOYEE")
                        .description("Opérations quotidiennes et caisse")
                        .build()));

        roleRepository.findByName("ROLE_CLIENT")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_CLIENT")
                        .description("Client externe - Boutique et commandes en ligne")
                        .build()));

        // Create or update default Super Admin: admin@gesten.com / passer
        createOrUpdateAdmin("admin@gesten.com", "passer", "Mael", "Koutoglo", superAdminRole);
        
        // Create or update default Super Admin: admin@gmail.com / passer
        createOrUpdateAdmin("admin@gmail.com", "passer", "Admin", "Super", superAdminRole);
    }

    private void createOrUpdateAdmin(String email, String rawPassword, String firstName, String lastName, Role role) {
        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        if (existingUserOpt.isPresent()) {
            User existing = existingUserOpt.get();
            if (existing.getRole() == null || !existing.isActive()) {
                existing.setRole(role);
                existing.setActive(true);
                userRepository.save(existing);
                log.info("Admin user updated: {}", email);
            }
        } else {
            User newAdmin = User.builder()
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(role)
                    .active(true)
                    .build();
            userRepository.save(newAdmin);
            log.info("Admin user created: {}", email);
        }
    }
}
