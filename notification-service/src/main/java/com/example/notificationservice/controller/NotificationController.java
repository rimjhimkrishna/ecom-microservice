package com.example.notificationservice.controller;

import com.example.notificationservice.dto.response.ApiResponse;
import com.example.notificationservice.dto.response.NotificationResponse;
import com.example.notificationservice.security.UserPrincipal;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getMyNotifications() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<NotificationResponse> response = notificationService.getMyNotifications(principal);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(@PathVariable String id) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        NotificationResponse response = notificationService.getNotificationById(id, principal);
        return ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully", response));
    }
}
