package ru.otus.hw.service;

import ru.otus.hw.dto.RegisterRequestDto;

public interface RegistrationService {
    void register(RegisterRequestDto req);
}
