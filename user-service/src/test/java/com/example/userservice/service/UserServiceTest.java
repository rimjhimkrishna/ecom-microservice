package com.example.userservice.service;

import com.example.userservice.dto.request.UserProfileUpdateRequest;
import com.example.userservice.dto.response.UserResponse;
import com.example.userservice.exception.ResourceNotFoundException;
import com.example.userservice.model.Role;
import com.example.userservice.model.User;
import com.example.userservice.repository.RefreshTokenRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    public void getProfile_Success() {
        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .role(Role.USER)
                .active(true)
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        UserResponse response = userService.getProfile("john");

        assertNotNull(response);
        assertEquals("john", response.getUsername());
        assertEquals("john@example.com", response.getEmail());
    }

    @Test
    public void getProfile_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getProfile("john"));
    }

    @Test
    public void updateProfile_Success() {
        User user = User.builder()
                .username("john")
                .email("john@example.com")
                .role(Role.USER)
                .active(true)
                .build();

        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
                .username("john_new")
                .email("john_new@example.com")
                .build();

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("john_new")).thenReturn(false);
        when(userRepository.existsByEmail("john_new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateProfile("john", request);

        assertNotNull(response);
        assertEquals("john_new", response.getUsername());
        assertEquals("john_new@example.com", response.getEmail());
    }

    @Test
    public void deleteUser_Success() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("john")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(refreshTokenRepository, times(1)).deleteByUser(user);
        verify(userRepository, times(1)).delete(user);
    }
}
