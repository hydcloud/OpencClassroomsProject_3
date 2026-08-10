package fr.datashare.backend.controller;

import fr.datashare.backend.dto.auth.LoginRequest;
import fr.datashare.backend.dto.auth.LoginResponse;
import fr.datashare.backend.dto.auth.RegisterRequest;
import fr.datashare.backend.dto.auth.UserResponse;
import fr.datashare.backend.service.AuthService;
import fr.datashare.backend.service.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import fr.datashare.backend.security.JwtAuthenticationFilter;
import fr.datashare.backend.security.JwtService;
import fr.datashare.backend.security.CustomUserDetailsService;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private UserRegistrationService userRegistrationService;

    @Test
    void register_should_return_201_created() throws Exception {

        RegisterRequest request = new RegisterRequest(
                "stephane@example.com",
                "MotDePasse123"
        );

        UserResponse response = new UserResponse(
                1L,
                "stephane@example.com",
                LocalDateTime.now()
        );

        when(userRegistrationService.register(request))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "email": "stephane@example.com",
                                      "password": "MotDePasse123"
                                    }
                                    """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email")
                        .value("stephane@example.com"));
    }

    @Test
    void login_should_return_200_and_token() throws Exception {

        LoginRequest request = new LoginRequest(
                "stephane@example.com",
                "MotDePasse123"
        );

        LoginResponse response = new LoginResponse("jwt-token");

        when(authService.login(request))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "email": "stephane@example.com",
                                      "password": "MotDePasse123"
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token")
                        .value("jwt-token"));
    }

    @Test
    void register_should_return_400_when_request_is_invalid() throws Exception {

        mockMvc.perform(
                        post("/api/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "email": "email-invalide",
                                      "password": "123"
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email")
                        .value("L'adresse email doit être valide."))
                .andExpect(jsonPath("$.validationErrors.password")
                        .value("Le mot de passe doit contenir au moins 8 caractères."));
    }
}