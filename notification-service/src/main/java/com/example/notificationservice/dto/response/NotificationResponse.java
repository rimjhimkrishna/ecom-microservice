package com.example.notificationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private String id;
    private UUID userId;
    private String email;
    private String type;
    private String subject;
    private String message;
    private boolean sent;
    private LocalDateTime createdAt;
}
