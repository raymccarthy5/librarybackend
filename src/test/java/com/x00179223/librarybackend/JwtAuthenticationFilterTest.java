package com.x00179223.librarybackend;

import com.x00179223.librarybackend.config.JwtAuthenticationFilter;
import com.x00179223.librarybackend.config.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void doFilterInternal_validToken_setAuthentication() throws ServletException, IOException {
        final String authHeader = "Bearer valid_token";
        final String jwt = "valid_token";
        final String userEmail = "user@example.com";
        UserDetails userDetails = User.builder()
                .username(userEmail)
                .password("password")
                .roles("USER")
                .build();

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(jwt, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request).getHeader("Authorization");
        verify(jwtService).extractUsername(jwt);
        verify(userDetailsService).loadUserByUsername(userEmail);
        verify(jwtService).isTokenValid(jwt, userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void doFilterInternal_invalidToken_doNotSetAuthentication() throws ServletException, IOException {
        final String authHeader = "Bearer invalid_token";
        final String jwt = "invalid_token";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request).getHeader("Authorization");
        verify(jwtService).extractUsername(jwt);
        verify(filterChain).doFilter(request, response);
        verify(request, never()).setAttribute(anyString(), anyString());
    }

    @Test
    public void doFilterInternal_userNotFound_doNotSetAuthentication() throws ServletException, IOException {
        final String authHeader = "Bearer valid_token";
        final String jwt = "valid_token";
        final String userEmail = "nonexistent_user@example.com";

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenThrow(UsernameNotFoundException.class);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request).getHeader("Authorization");
        verify(jwtService).extractUsername(jwt);
        verify(filterChain).doFilter(request, response);
        verify(request, never()).setAttribute(anyString(), anyString());
    }

    @Test
    public void doFilterInternal_tokenNotValid_doNotSetAuthentication() throws ServletException, IOException {
        final String authHeader = "Bearer invalid_token";
        final String jwt = "invalid_token";
        final String userEmail = "user@example.com";
        UserDetails userDetails = User.builder()
                .username(userEmail)
                .password("password")
                .roles("USER")
                .build();

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUsername(jwt)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(jwt, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request).getHeader("Authorization");
        verify(jwtService).extractUsername(jwt);
        verify(filterChain).doFilter(request, response);
        verify(request, never()).setAttribute(anyString(), anyString());
    }

    @Test
    public void doFilterInternal_missingAuthorizationHeader_doNotSetAuthentication() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(request).getHeader("Authorization");
        verify(filterChain).doFilter(request, response);
        verify(request, never()).setAttribute(anyString(), anyString());
    }
}
