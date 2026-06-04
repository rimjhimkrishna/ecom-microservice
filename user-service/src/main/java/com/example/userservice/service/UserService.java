package com.example.userservice.service;

import com.example.userservice.dto.request.UserProfileUpdateRequest;
import com.example.userservice.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserResponse getProfile(String username);
    UserResponse updateProfile(String username, UserProfileUpdateRequest request);
    Page<UserResponse> getAllUsers(Pageable pageable);
    void deleteUser(UUID id);
}
