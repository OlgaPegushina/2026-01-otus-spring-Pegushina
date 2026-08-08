package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.service.AdminInitializer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminBootstrap {

    private final AdminInitializer adminInitializer;

    @Bean
    ApplicationRunner initAdmin(
            @Value("${app.admin.username:}") String username,
            @Value("${app.admin.password:}") String password
    ) {
        return args -> {
            try {
                adminInitializer.createAdminIfConfigured(username, password);
            } catch (EntityNotFoundException e) {
                log.error("Admin bootstrap failed: required role missing. {}", e.getMessage());
            } catch (Exception e) {
                log.error("Failed to bootstrap admin user '{}': {}", username, e.getMessage(), e);
            }
        };
    }
}
