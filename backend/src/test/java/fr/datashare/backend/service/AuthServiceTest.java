package fr.datashare.backend.service;

import fr.datashare.backend.dto.auth.LoginRequest;
import fr.datashare.backend.dto.auth.LoginResponse;
import fr.datashare.backend.dto.auth.RegisterRequest;
import fr.datashare.backend.dto.auth.UserResponse;
import fr.datashare.backend.entity.User;
import fr.datashare.backend.exception.EmailAlreadyExistsException;
import fr.datashare.backend.exception.InvalidCredentialsException;
import fr.datashare.backend.repository.UserRepository;
import fr.datashare.backend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService
        );

    }

    @Test
    void register_should_save_user_when_email_is_available() {

        // GIVEN
        RegisterRequest request = new RegisterRequest(
                "Stephane@Example.com",
                "MotDePasse123"
        );

        when(userRepository.existsByEmail("stephane@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("MotDePasse123"))
                .thenReturn("hashed-password");

        User savedUser = new User();
        savedUser.setEmail("stephane@example.com");
        savedUser.setPassword("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // WHEN
        UserResponse response = authService.register(request);

        // THEN
        assertNotNull(response);
        assertEquals("stephane@example.com", response.email());

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User userSentToRepository = userCaptor.getValue();

        assertEquals(
                "stephane@example.com",
                userSentToRepository.getEmail()
        );

        assertEquals(
                "hashed-password",
                userSentToRepository.getPassword()
        );

        verify(passwordEncoder)
                .encode("MotDePasse123");
    }

    @Test
    void register_should_throw_when_email_already_exists() {

        RegisterRequest request = new RegisterRequest(
                "stephane@example.com",
                "MotDePasse123"
        );

        when(userRepository.existsByEmail("stephane@example.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request)
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }

    @Test
    void login_should_return_token_when_credentials_are_valid() {

        LoginRequest request = new LoginRequest(
                "stephane@example.com",
                "MotDePasse123"
        );

        User user = new User();
        user.setEmail("stephane@example.com");
        user.setPassword("hashed-password");

        when(userRepository.findByEmail("stephane@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "MotDePasse123",
                "hashed-password"
        )).thenReturn(true);

        when(jwtService.generateToken("stephane@example.com"))
                .thenReturn("jwt-token");

        LoginResponse response =
                authService.login(request);

        assertEquals(
                "jwt-token",
                response.token()
        );

        verify(jwtService)
                .generateToken("stephane@example.com");
    }

    @Test
    void login_should_throw_when_password_is_invalid() {

        // GIVEN
        LoginRequest request = new LoginRequest(
                "stephane@example.com",
                "MauvaisMotDePasse"
        );

        User user = new User();
        user.setEmail("stephane@example.com");
        user.setPassword("hashed-password");

        when(userRepository.findByEmail("stephane@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "MauvaisMotDePasse",
                "hashed-password"
        )).thenReturn(false);

        // WHEN / THEN
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(jwtService, never())
                .generateToken(anyString());
    }

    @Test
    void login_should_throw_when_user_does_not_exist() {

        // GIVEN
        LoginRequest request = new LoginRequest(
                "inconnu@example.com",
                "MotDePasse123"
        );

        when(userRepository.findByEmail("inconnu@example.com"))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateToken(anyString());
    }
}