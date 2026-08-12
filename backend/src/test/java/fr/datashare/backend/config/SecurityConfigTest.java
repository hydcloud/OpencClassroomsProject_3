package fr.datashare.backend.config;

import fr.datashare.backend.security.JwtAuthenticationFilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        JwtAuthenticationFilter jwtAuthenticationFilter =
                mock(JwtAuthenticationFilter.class);

        securityConfig =
                new SecurityConfig(jwtAuthenticationFilter);
    }

    @Test
    void passwordEncoder_should_return_bcrypt_encoder() {

        PasswordEncoder passwordEncoder =
                securityConfig.passwordEncoder();

        assertNotNull(passwordEncoder);

        assertInstanceOf(
                BCryptPasswordEncoder.class,
                passwordEncoder
        );

        String encoded =
                passwordEncoder.encode("Secret123");

        assertTrue(
                passwordEncoder.matches(
                        "Secret123",
                        encoded
                )
        );

        assertFalse(
                passwordEncoder.matches(
                        "WrongPassword",
                        encoded
                )
        );
    }

    @Test
    void corsConfiguration_should_allow_angular_frontend() {

        CorsConfigurationSource source =
                securityConfig.corsConfigurationSource();

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setRequestURI("/api/files");

        CorsConfiguration configuration =
                source.getCorsConfiguration(request);

        assertNotNull(configuration);

        assertEquals(
                1,
                configuration.getAllowedOrigins().size()
        );

        assertTrue(
                configuration.getAllowedOrigins()
                        .contains("http://localhost:4200")
        );

        assertTrue(
                configuration.getAllowedMethods()
                        .contains("GET")
        );

        assertTrue(
                configuration.getAllowedMethods()
                        .contains("POST")
        );

        assertTrue(
                configuration.getAllowedMethods()
                        .contains("DELETE")
        );

        assertTrue(
                configuration.getAllowedMethods()
                        .contains("OPTIONS")
        );

        assertTrue(
                configuration.getAllowedHeaders()
                        .contains("Authorization")
        );

        assertTrue(
                configuration.getAllowedHeaders()
                        .contains("Content-Type")
        );

        assertEquals(
                Boolean.TRUE,
                configuration.getAllowCredentials()
        );
    }
}