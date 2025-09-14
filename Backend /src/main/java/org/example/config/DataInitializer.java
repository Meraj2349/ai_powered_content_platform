package org.example.config;

import org.example.model.Role;
import org.example.model.RoleName;
import org.example.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Database initialization
 * Creates default roles if they don't exist
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create default roles if they don't exist
        createRoleIfNotExists(RoleName.ROLE_USER, "Standard user role");
        createRoleIfNotExists(RoleName.ROLE_ADMIN, "Administrator role");
        createRoleIfNotExists(RoleName.ROLE_MODERATOR, "Moderator role");
        createRoleIfNotExists(RoleName.ROLE_PREMIUM_USER, "Premium user role");
    }

    private void createRoleIfNotExists(RoleName roleName, String description) {
        if (!roleRepository.findByName(roleName).isPresent()) {
            Role role = new Role(roleName, description);
            roleRepository.save(role);
            System.out.println("Created role: " + roleName);
        }
    }
}
