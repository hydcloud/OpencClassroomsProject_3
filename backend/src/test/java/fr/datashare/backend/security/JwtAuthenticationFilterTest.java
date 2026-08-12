package fr.datashare.backend.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        filter = new JwtAuthenticationFilter(
                jwtService,
                userDetailsService
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_should_continue_when_authorization_header_is_missing()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn(null);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );
    }

    @Test
    void doFilter_should_continue_when_header_is_not_bearer()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Basic abc123");

        filter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilter_should_authenticate_when_token_is_valid()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer valid-token");

        when(jwtService.extractEmail("valid-token"))
                .thenReturn("test@datashare.fr");

        UserDetails userDetails =
                org.springframework.security.core.userdetails.User
                        .withUsername("test@datashare.fr")
                        .password("password")
                        .authorities("ROLE_USER")
                        .build();

        when(userDetailsService.loadUserByUsername(
                "test@datashare.fr"
        )).thenReturn(userDetails);

        when(jwtService.isTokenValid(
                "valid-token",
                "test@datashare.fr"
        )).thenReturn(true);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNotNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        assertEquals(
                userDetails,
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal()
        );

        assertTrue(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .isAuthenticated()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilter_should_not_authenticate_when_token_is_invalid()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer invalid-token");

        when(jwtService.extractEmail("invalid-token"))
                .thenReturn("test@datashare.fr");

        UserDetails userDetails = mock(UserDetails.class);

        when(userDetails.getUsername())
                .thenReturn("test@datashare.fr");

        when(userDetailsService.loadUserByUsername(
                "test@datashare.fr"
        )).thenReturn(userDetails);

        when(jwtService.isTokenValid(
                "invalid-token",
                "test@datashare.fr"
        )).thenReturn(false);

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilter_should_clear_context_when_token_throws_jwt_exception()
            throws Exception {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer malformed-token");

        when(jwtService.extractEmail("malformed-token"))
                .thenThrow(new JwtException("Invalid token"));

        filter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verifyNoInteractions(userDetailsService);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void doFilter_should_not_reload_user_when_already_authenticated()
            throws Exception {

        UserDetails existingUser = mock(UserDetails.class);

        UsernamePasswordAuthenticationToken authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        existingUser,
                        null,
                        List.of()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer valid-token");

        when(jwtService.extractEmail("valid-token"))
                .thenReturn("test@datashare.fr");

        filter.doFilter(
                request,
                response,
                filterChain
        );

        verifyNoInteractions(userDetailsService);

        verify(jwtService, never())
                .isTokenValid(
                        anyString(),
                        anyString()
                );

        assertEquals(
                authentication,
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }
}