package com.example.orderservice.service.impl;

import com.example.orderservice.dto.request.OrderItemRequest;
import com.example.orderservice.dto.request.OrderRequest;
import com.example.orderservice.dto.request.OrderStatusUpdateRequest;
import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.dto.response.OrderItemResponse;
import com.example.orderservice.dto.response.OrderResponse;
import com.example.orderservice.dto.response.ProductResponse;
import com.example.orderservice.exception.BusinessException;
import com.example.orderservice.exception.ResourceNotFoundException;
import com.example.orderservice.feign.ProductClient;
import com.example.orderservice.kafka.event.OrderCancelledEvent;
import com.example.orderservice.kafka.event.OrderCreatedEvent;
import com.example.orderservice.kafka.event.OrderStatusUpdatedEvent;
import com.example.orderservice.kafka.producer.OrderKafkaProducer;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.security.UserPrincipal;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderKafkaProducer kafkaProducer;

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request, UserPrincipal principal) {
        logger.info("Placing order for user: {}", principal.getUsername());

        Order order = Order.builder()
                .userId(principal.getId())
                .shippingAddress(request.getShippingAddress())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            // 1. Fetch product details synchronously via Feign
            ResponseEntity<ApiResponse<ProductResponse>> productRes = productClient.getProductById(itemReq.getProductId());
            if (productRes.getBody() == null || !productRes.getBody().isSuccess()) {
                throw new ResourceNotFoundException("Product not found with id " + itemReq.getProductId());
            }

            ProductResponse product = productRes.getBody().getData();
            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new BusinessException("Insufficient stock for product: " + product.getName() + 
                        ". Available: " + product.getStockQuantity() + ", Requested: " + itemReq.getQuantity());
            }

            // 2. Reduce stock via Feign (use negative quantity to reduce)
            productClient.updateStock(product.getId(), -itemReq.getQuantity());

            // 3. Create order item snapshot
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(itemReq.getQuantity())
                    .subtotal(subtotal)
                    .build();

            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CONFIRMED); // Confirmed after successful stock reductions
        
        Order savedOrder = orderRepository.save(order);
        logger.info("Order placed successfully. ID: {}", savedOrder.getId());

        // 4. Publish order.created Kafka Event
        List<OrderCreatedEvent.OrderItemEvent> itemEvents = savedOrder.getItems().stream()
                .map(item -> OrderCreatedEvent.OrderItemEvent.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        OrderCreatedEvent createdEvent = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .userId(savedOrder.getUserId())
                .userEmail(principal.getEmail())
                .items(itemEvents)
                .totalAmount(savedOrder.getTotalAmount())
                .createdAt(savedOrder.getCreatedAt())
                .build();

        kafkaProducer.sendOrderCreatedEvent(createdEvent);

        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(UserPrincipal principal, Pageable pageable) {
        return orderRepository.findByUserId(principal.getId(), pageable).map(this::mapToOrderResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id, UserPrincipal principal) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));

        // Enforce ownership unless user is ADMIN
        if (!order.getUserId().equals(principal.getId()) && !"ADMIN".equalsIgnoreCase(principal.getRole())) {
            throw new BusinessException("Access denied: You do not own this order.");
        }

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID id, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        if (oldStatus == OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot update status of a cancelled order");
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        logger.info("Order status updated from {} to {}", oldStatus, newStatus);

        // Publish order.status.updated event
        OrderStatusUpdatedEvent event = OrderStatusUpdatedEvent.builder()
                .orderId(updatedOrder.getId())
                .userId(updatedOrder.getUserId())
                .userEmail("customer@example.com")
                .oldStatus(oldStatus.name())
                .newStatus(newStatus.name())
                .updatedAt(updatedOrder.getUpdatedAt())
                .build();

        kafkaProducer.sendOrderStatusUpdatedEvent(event);

        return mapToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(UUID id, UserPrincipal principal) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));

        // Owner check
        if (!order.getUserId().equals(principal.getId()) && !"ADMIN".equalsIgnoreCase(principal.getRole())) {
            throw new BusinessException("Access denied: You do not own this order.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException("Order is already cancelled");
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("Order cannot be cancelled in status: " + order.getStatus());
        }

        // Restore stock (quantity is positive to increase stock)
        for (OrderItem item : order.getItems()) {
            productClient.updateStock(item.getProductId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        logger.info("Order cancelled. ID: {}", id);

        // Publish order.cancelled event
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .userEmail(principal.getEmail())
                .reason("Cancelled by user")
                .cancelledAt(LocalDateTime.now())
                .build();

        kafkaProducer.sendOrderCancelledEvent(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAllWithItems(pageable).map(this::mapToOrderResponse);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
