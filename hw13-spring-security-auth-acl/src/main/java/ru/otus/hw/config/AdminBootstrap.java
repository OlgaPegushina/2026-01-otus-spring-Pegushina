package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.model.AppUser;
import ru.otus.hw.model.RoleName;
import ru.otus.hw.repository.AppUserRepository;
import ru.otus.hw.repository.RoleRepository;

@Configuration
@RequiredArgsConstructor
public class AdminBootstrap {

    private final AppUserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner initAdmin(
            @Value("${app.admin.username:}") String username,
            @Value("${app.admin.password:}") String password
    ) {
        return args -> createAdminIfConfigured(username, password);
    }

    @Transactional
    public void createAdminIfConfigured(String username, String password) {

        if (username == null || username.isBlank()) {
            return;
        }
        if (userRepository.findByUsername(username).isPresent()) {
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
    }
}
