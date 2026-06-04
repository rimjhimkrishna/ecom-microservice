package com.example.userservice.service;

import com.example.userservice.dto.request.LoginRequest;
import com.example.userservice.dto.request.RegisterRequest;
import com.example.userservice.dto.response.AuthResponse;
import com.example.userservice.exception.DuplicateResourceException;
import com.example.userservice.exception.UnauthorizedException;
import com.example.userservice.model.Role;
import com.example.userservice.model.User;
import com.example.userservice.repository.RefreshTokenRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.JwtService;
import com.example.userservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(authService, "refreshExpiryMs", 604800000L);
    }

    @Test
    public void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("john")
                .email("john@example.com")
                .password("Password123")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("john")
                .email("john@example.com")
                .role(Role.USER)
                .active(true)
                .build();

        when(userRepository.existsByUsername("john")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("access_token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("john", response.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void register_DuplicateUsername_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("john")
                .email("john@example.com")
                .password("Password123")
                .build();

        when(userRepository.existsByUsername("john")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    public void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .username("john")
                .password("Password123")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .username("john")
                .email("john@example.com")
                .password("encoded_pass")
                .role(Role.USER)
                .active(true)
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123", "encoded_pass")).thenReturn(true);
        when(jwtService.generateToken(any(User.class))).thenReturn("access_token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        verify(refreshTokenRepository, times(1)).deleteByUser(user);
    }

    @Test
    public void login_InvalidPassword_ThrowsException() {
        LoginRequest request = LoginRequest.builder()
                .username("john")
                .password("WrongPass")
                .build();

        User user = User.builder()
                .username("john")
                .password("encoded_pass")
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass", "encoded_pass")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }
}
