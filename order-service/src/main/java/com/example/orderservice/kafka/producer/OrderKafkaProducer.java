package com.example.orderservice.kafka.producer;

import com.example.orderservice.kafka.event.OrderCancelledEvent;
import com.example.orderservice.kafka.event.OrderCreatedEvent;
import com.example.orderservice.kafka.event.OrderStatusUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderKafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(OrderKafkaProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        logger.info("Publishing order.created event to Kafka for orderId: {}", event.getOrderId());
        kafkaTemplate.send("order.created", event.getOrderId().toString(), event);
    }

    public void sendOrderStatusUpdatedEvent(OrderStatusUpdatedEvent event) {
        logger.info("Publishing order.status.updated event to Kafka for orderId: {}", event.getOrderId());
        kafkaTemplate.send("order.status.updated", event.getOrderId().toString(), event);
    }

    public void sendOrderCancelledEvent(OrderCancelledEvent event) {
        logger.info("Publishing order.cancelled event to Kafka for orderId: {}", event.getOrderId());
        kafkaTemplate.send("order.cancelled", event.getOrderId().toString(), event);
    }
}
