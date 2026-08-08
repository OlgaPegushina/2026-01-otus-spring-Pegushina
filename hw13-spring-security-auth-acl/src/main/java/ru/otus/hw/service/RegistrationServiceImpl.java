package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.RegisterRequestDto;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.exception.DuplicateKeyException;
import ru.otus.hw.model.AppUser;
import ru.otus.hw.model.Role;
import ru.otus.hw.model.RoleName;
import ru.otus.hw.repository.AppUserRepository;
import ru.otus.hw.repository.RoleRepository;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final AppUserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequestDto req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.findByUsername(req.username()).isPresent()) {
            throw new DuplicateKeyException("Username already exists");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new EntityNotFoundException("ROLE_USER not found in DB"));

        AppUser user = new AppUser();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setEnabled(true);

        user.getRoles().add(userRole);
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateKeyException("Username already exists");
        }
    }
}
