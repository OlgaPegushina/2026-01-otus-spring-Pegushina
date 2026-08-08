package ru.otus.hw.service;

public interface AdminInitializer {
    void createAdminIfConfigured(String username, String password);
}
