package fr.datashare.backend.security;

import fr.datashare.backend.entity.User;
import fr.datashare.backend.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService =
                new CustomUserDetailsService(
                        userRepository
                );
    }

    @Test
    void loadUserByUsername_should_return_user_details() {

        User user = new User();
        user.setEmail("test@datashare.fr");
        user.setPassword("hashed-password");

        when(userRepository.findByEmail("test@datashare.fr"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                userDetailsService.loadUserByUsername(
                        "test@datashare.fr"
                );

        assertEquals(
                "test@datashare.fr",
                result.getUsername()
        );

        assertEquals(
                "hashed-password",
                result.getPassword()
        );

        assertTrue(
                result.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_USER")
                        )
        );
    }

    @Test
    void loadUserByUsername_should_throw_when_user_does_not_exist() {

        when(userRepository.findByEmail("unknown@datashare.fr"))
                .thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(
                        "unknown@datashare.fr"
                )
        );
    }
}