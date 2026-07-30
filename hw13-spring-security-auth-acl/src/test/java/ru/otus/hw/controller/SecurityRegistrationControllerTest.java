package ru.otus.hw.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.dto.RegisterRequestDto;
import ru.otus.hw.service.RegistrationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Контроллер регистрации: доступ/CSRF/redirect")
@WebMvcTest(RegistrationController.class)
@Import(SecurityConfig.class)
class SecurityRegistrationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RegistrationService registrationService;

    @Test
    @DisplayName("GET /register доступен анониму - 200")
    void getRegisterShouldBeOkForAnonymous() throws Exception {
        mvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("form"));
    }

    @Test
    @DisplayName("POST /register без csrf - 403")
    void postRegisterShouldBeForbiddenWithoutCsrf() throws Exception {
        mvc.perform(post("/register")
                        .param("username", "user123")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isForbidden());

        verify(registrationService, never()).register(any());
    }

    @Test
    @DisplayName("POST /register с csrf и валидными params - 302 redirect")
    void postRegisterShouldRedirectWithCsrfAndValidParams() throws Exception {
        mvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "user123")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(registrationService, times(1))
                .register(new RegisterRequestDto("user123", "password123", "password123"));
    }
}