package com.example.notificationservice.service;

import com.example.notificationservice.dto.response.NotificationResponse;
import com.example.notificationservice.kafka.event.OrderCancelledEvent;
import com.example.notificationservice.kafka.event.OrderCreatedEvent;
import com.example.notificationservice.kafka.event.OrderStatusUpdatedEvent;
import com.example.notificationservice.security.UserPrincipal;

import java.util.List;

public interface NotificationService {
    void handleOrderCreated(OrderCreatedEvent event);
    void handleOrderStatusUpdated(OrderStatusUpdatedEvent event);
    void handleOrderCancelled(OrderCancelledEvent event);
    List<NotificationResponse> getMyNotifications(UserPrincipal principal);
    NotificationResponse getNotificationById(String id, UserPrincipal principal);
}
