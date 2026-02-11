package com.example.loanova.seeder;

import com.example.loanova.entity.Role;
import com.example.loanova.repository.RoleRepository;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1) // Run first
public class RoleSeeder implements CommandLineRunner {

  private final RoleRepository roleRepository;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    seedRoles();
  }

  private void seedRoles() {
    List<RoleData> roles =
        Arrays.asList(
            new RoleData("SUPERADMIN", "Super Administrator with full access"),
            new RoleData("MARKETING", "Marketing staff"),
            new RoleData("BRANCHMANAGER", "Branch Manager"),
            new RoleData("BACKOFFICE", "Backoffice staff"),
            new RoleData("CUSTOMER", "Loanova Customer"));

    for (RoleData data : roles) {
      createRoleIfNotFound(data.name, data.description);
    }
  }

  private void createRoleIfNotFound(String roleName, String description) {
    if (roleRepository.findByRoleName(roleName).isEmpty()) {
      log.info("Creating new Role: {}", roleName);
      Role role = Role.builder().roleName(roleName).roleDescription(description).build();
      roleRepository.save(role);
    } else {
      log.info("Role {} already exists.", roleName);
    }
  }

  private record RoleData(String name, String description) {}
}
