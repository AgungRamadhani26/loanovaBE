package com.example.loanova.seeder;

import com.example.loanova.entity.Role;
import com.example.loanova.entity.User;
import com.example.loanova.repository.RoleRepository;
import com.example.loanova.repository.UserRepository;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2) // Run after PermissionSeeder (or other seeders if any)
public class SuperAdminSeeder implements CommandLineRunner {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    seedSuperAdmin();
  }

  private void seedSuperAdmin() {
    String username = "BimaSuperAdmin11@";
    String email = "bimasuperadmin@gmail.com";
    String password = "BimaSuperAdmin11@";
    String roleName = "SUPERADMIN";

    Optional<User> existingUser = userRepository.findByUsername(username);
    if (existingUser.isPresent()) {
      log.info("Super Admin user already exists. Skipping seeding.");
      return;
    }

    Role superAdminRole =
        roleRepository
            .findByRoleName(roleName)
            .orElseGet(
                () ->
                    roleRepository.save(
                        Role.builder()
                            .roleName(roleName)
                            .roleDescription("Super Administrator with full access")
                            .build()));

    User superAdmin =
        User.builder()
            .username(username)
            .email(email)
            .password(passwordEncoder.encode(password))
            .isActive(true)
            .authProvider("LOCAL")
            .roles(new HashSet<>(Collections.singletonList(superAdminRole)))
            .build();

    userRepository.save(superAdmin);
    log.info("Super Admin user seeded successfully with username: {}", username);
  }
}
