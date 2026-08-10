package fr.datashare.backend.service;

import fr.datashare.backend.dto.auth.RegisterRequest;
import fr.datashare.backend.dto.auth.UserResponse;
import fr.datashare.backend.entity.User;
import fr.datashare.backend.exception.EmailAlreadyExistsException;
import fr.datashare.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

    class UserRegistrationServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private UserRegistrationService userRegistrationService;

        @BeforeEach
        void setUp() {
            userRegistrationService = new UserRegistrationService(
                    userRepository,
                    passwordEncoder
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
        UserResponse response = userRegistrationService.register(request);

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
                () -> userRegistrationService.register(request)
        );

        verify(passwordEncoder, never())
                .encode(anyString());

        verify(userRepository, never())
                .save(any(User.class));
    }
}
