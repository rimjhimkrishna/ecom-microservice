package com.example.notificationservice.service.impl;

import com.example.notificationservice.dto.response.NotificationResponse;
import com.example.notificationservice.exception.BusinessException;
import com.example.notificationservice.exception.ResourceNotFoundException;
import com.example.notificationservice.kafka.event.OrderCancelledEvent;
import com.example.notificationservice.kafka.event.OrderCreatedEvent;
import com.example.notificationservice.kafka.event.OrderStatusUpdatedEvent;
import com.example.notificationservice.model.Notification;
import com.example.notificationservice.model.NotificationType;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.security.UserPrincipal;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;

    @Override
    public void handleOrderCreated(OrderCreatedEvent event) {
        logger.info("Processing order created event for orderId: {}", event.getOrderId());
        
        StringBuilder message = new StringBuilder();
        message.append("Thank you for your order! Here are your order details:\n\n");
        message.append("Order ID: ").append(event.getOrderId()).append("\n");
        message.append("Total Amount: $").append(event.getTotalAmount()).append("\n\n");
        message.append("Items purchased:\n");
        event.getItems().forEach(item -> 
            message.append("- ").append(item.getProductName())
                   .append(" x ").append(item.getQuantity())
                   .append(" ($").append(item.getUnitPrice()).append(" each)\n")
        );
        message.append("\nYour order is currently CONFIRMED and will be processed shortly.");

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .email(event.getUserEmail())
                .type(NotificationType.ORDER_CREATED)
                .subject("Order Confirmation - " + event.getOrderId())
                .message(message.toString())
                .sent(true) // Mock email sending
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        logger.info("Notification stored successfully in MongoDB for orderId: {}", event.getOrderId());
    }

    @Override
    public void handleOrderStatusUpdated(OrderStatusUpdatedEvent event) {
        logger.info("Processing order status updated event for orderId: {}", event.getOrderId());

        String message = String.format("Hello!\n\nThe status of your order %s has been updated from %s to %s.\n\nThank you for shopping with us!",
                event.getOrderId(), event.getOldStatus(), event.getNewStatus());

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .email(event.getUserEmail())
                .type(NotificationType.STATUS_UPDATED)
                .subject("Order Status Update - " + event.getOrderId())
                .message(message)
                .sent(true)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        logger.info("Notification stored successfully in MongoDB for orderId: {}", event.getOrderId());
    }

    @Override
    public void handleOrderCancelled(OrderCancelledEvent event) {
        logger.info("Processing order cancelled event for orderId: {}", event.getOrderId());

        String message = String.format("Hello!\n\nYour order %s has been successfully cancelled.\nReason: %s\n\nIf you did not request this, please contact support.",
                event.getOrderId(), event.getReason());

        Notification notification = Notification.builder()
                .userId(event.getUserId())
                .email(event.getUserEmail())
                .type(NotificationType.ORDER_CANCELLED)
                .subject("Order Cancelled - " + event.getOrderId())
                .message(message)
                .sent(true)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
        logger.info("Notification stored successfully in MongoDB for orderId: {}", event.getOrderId());
    }

    @Override
    public List<NotificationResponse> getMyNotifications(UserPrincipal principal) {
        return notificationRepository.findByUserId(principal.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponse getNotificationById(String id, UserPrincipal principal) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id " + id));

        // Enforce ownership
        if (!notification.getUserId().equals(principal.getId()) && !"ADMIN".equalsIgnoreCase(principal.getRole())) {
            throw new BusinessException("Access denied: You do not own this notification.");
        }

        return mapToResponse(notification);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .email(notification.getEmail())
                .type(notification.getType().name())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .sent(notification.isSent())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
