package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.model.AppUser;
import ru.otus.hw.model.RoleName;
import ru.otus.hw.repository.AppUserRepository;
import ru.otus.hw.repository.RoleRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminInitializerImpl {
    private final AppUserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void createAdminIfConfigured(String username, String password) {
        if (username == null || username.isBlank()) {
            log.info("Admin bootstrap skipped: username is not configured");
            return;
        }

        if (userRepository.findByUsername(username).isPresent()) {
            log.info("Admin bootstrap skipped: user '{}' already exists", username);
            return;
        }

        var adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new EntityNotFoundException("ROLE_ADMIN not found"));

        AppUser admin = new AppUser();
        admin.setUsername(username);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setEnabled(true);
        admin.getRoles().add(adminRole);

        userRepository.save(admin);
        log.info("Admin user '{}' created successfully", username);
    }
}
