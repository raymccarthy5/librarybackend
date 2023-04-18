package com.x00179223.librarybackend;

import com.x00179223.librarybackend.auth.*;
import com.x00179223.librarybackend.config.JwtService;
import com.x00179223.librarybackend.model.Role;
import com.x00179223.librarybackend.model.User;
import com.x00179223.librarybackend.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private UpdatePasswordRequest updatePasswordRequest;

    @BeforeEach
    public void setUp() {
        registerRequest = RegisterRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .build();

        authenticationRequest = AuthenticationRequest.builder()
                .email("john.doe@example.com")
                .password("password123")
                .build();

        updatePasswordRequest = UpdatePasswordRequest.builder()
                .id(1L)
                .password("newPassword123")
                .build();
    }

    @Test
    public void testRegister() {
        User user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .fine(0.0)
                .build();

        String jwtToken = "mockJwtToken";

        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userService.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn(jwtToken);

        AuthenticationResponse response = authenticationService.register(registerRequest);

        assertEquals(jwtToken, response.getToken());
        assertEquals(user.getRole(), response.getRole());

        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userService).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    public void testAuthenticate() {
        User user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .fine(0.0)
                .build();

        String jwtToken = "mockJwtToken";

        when(userService.findByEmail(authenticationRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(jwtToken);

        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        assertEquals(jwtToken, response.getToken());
        assertEquals(user.getRole(), response.getRole());
        assertEquals(user.getId(), response.getId());

        verify(authenticationManager).authenticate(any());
        verify(userService).findByEmail(authenticationRequest.getEmail());
        verify(jwtService).generateToken(user);
    }

    @Test
    public void testRegisterAdmin() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .firstname("Admin")
                .lastname("User")
                .email("admin@example.com")
                .password("adminPassword123")
                .build();

        User user = User.builder()
                .id(1L)
                .firstname(registerRequest.getFirstname())
                .lastname(registerRequest.getLastname())
                .email(registerRequest.getEmail())
                .password("test")
                .role(Role.ADMIN)
                .fine(0.0)
                .build();

        String jwtToken = "mockJwtToken";

        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn(user.getPassword());
        when(userService.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn(jwtToken);

        AuthenticationResponse response = authenticationService.registerAdmin(registerRequest);

        assertEquals(jwtToken, response.getToken());
        assertEquals(user.getRole(), response.getRole());

        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userService).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }


    @Test
    public void testUpdatePassword() {
        User user = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .fine(0.0)
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.doe@example.com")
                .password("newEncodedPassword")
                .role(Role.USER)
                .fine(0.0)
                .build();

        String jwtToken = "mockJwtToken";

        when(userService.findById(updatePasswordRequest.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(updatePasswordRequest.getPassword())).thenReturn("newEncodedPassword");
        when(userService.updatePassword(user)).thenReturn(updatedUser);
        when(jwtService.generateToken(updatedUser)).thenReturn(jwtToken);

        AuthenticationResponse response = authenticationService.updatePassword(updatePasswordRequest);

        assertEquals(jwtToken, response.getToken());
        assertEquals(updatedUser.getRole(), response.getRole());
        assertEquals(updatedUser.getId(), response.getId());

        verify(userService).findById(updatePasswordRequest.getId());
        verify(passwordEncoder).encode(updatePasswordRequest.getPassword());
        verify(userService).updatePassword(user);
        verify(jwtService).generateToken(updatedUser);
    }
}
