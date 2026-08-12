package fr.datashare.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "01234567890123456789012345678901",
                3600000
        );
    }

    @Test
    void generateToken_should_create_token() {

        String token =
                jwtService.generateToken("test@datashare.fr");

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractEmail_should_return_token_subject() {

        String token =
                jwtService.generateToken("test@datashare.fr");

        String email =
                jwtService.extractEmail(token);

        assertEquals(
                "test@datashare.fr",
                email
        );
    }

    @Test
    void isTokenValid_should_return_true_for_expected_email() {

        String token =
                jwtService.generateToken("test@datashare.fr");

        assertTrue(
                jwtService.isTokenValid(
                        token,
                        "test@datashare.fr"
                )
        );
    }

    @Test
    void isTokenValid_should_return_false_for_another_email() {

        String token =
                jwtService.generateToken("test@datashare.fr");

        assertFalse(
                jwtService.isTokenValid(
                        token,
                        "other@datashare.fr"
                )
        );
    }

    @Test
    void isTokenValid_should_fail_for_expired_token() {

        JwtService expiredJwtService =
                new JwtService(
                        "01234567890123456789012345678901",
                        -1000
                );

        String token =
                expiredJwtService.generateToken(
                        "test@datashare.fr"
                );

        assertThrows(
                RuntimeException.class,
                () -> expiredJwtService.isTokenValid(
                        token,
                        "test@datashare.fr"
                )
        );
    }
}