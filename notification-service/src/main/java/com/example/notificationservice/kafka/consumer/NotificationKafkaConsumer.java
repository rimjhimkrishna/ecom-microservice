package com.example.notificationservice.kafka.consumer;

import com.example.notificationservice.kafka.event.OrderCancelledEvent;
import com.example.notificationservice.kafka.event.OrderCreatedEvent;
import com.example.notificationservice.kafka.event.OrderStatusUpdatedEvent;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationKafkaConsumer.class);

    private final NotificationService notificationService;

    @KafkaListener(topics = "order.created", groupId = "notification-group")
    public void consumeOrderCreated(OrderCreatedEvent event) {
        logger.info("Received order.created event from Kafka for orderId: {}", event.getOrderId());
        try {
            notificationService.handleOrderCreated(event);
        } catch (Exception e) {
            logger.error("Error processing order.created event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order.status.updated", groupId = "notification-group")
    public void consumeOrderStatusUpdated(OrderStatusUpdatedEvent event) {
        logger.info("Received order.status.updated event from Kafka for orderId: {}", event.getOrderId());
        try {
            notificationService.handleOrderStatusUpdated(event);
        } catch (Exception e) {
            logger.error("Error processing order.status.updated event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "order.cancelled", groupId = "notification-group")
    public void consumeOrderCancelled(OrderCancelledEvent event) {
        logger.info("Received order.cancelled event from Kafka for orderId: {}", event.getOrderId());
        try {
            notificationService.handleOrderCancelled(event);
        } catch (Exception e) {
            logger.error("Error processing order.cancelled event: {}", e.getMessage(), e);
        }
    }
}
