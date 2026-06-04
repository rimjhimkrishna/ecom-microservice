package com.example.orderservice.service;

import com.example.orderservice.dto.request.OrderItemRequest;
import com.example.orderservice.dto.request.OrderRequest;
import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.dto.response.OrderResponse;
import com.example.orderservice.dto.response.ProductResponse;
import com.example.orderservice.exception.BusinessException;
import com.example.orderservice.feign.ProductClient;
import com.example.orderservice.kafka.event.OrderCreatedEvent;
import com.example.orderservice.kafka.producer.OrderKafkaProducer;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.security.UserPrincipal;
import com.example.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderKafkaProducer kafkaProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    public void placeOrder_Success() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, "john", "john@example.com", "USER");

        OrderRequest request = OrderRequest.builder()
                .shippingAddress("123 Main St")
                .items(Arrays.asList(OrderItemRequest.builder().productId(productId).quantity(2).build()))
                .build();

        ProductResponse product = ProductResponse.builder()
                .id(productId)
                .name("Laptop")
                .price(BigDecimal.valueOf(1000.00))
                .stockQuantity(10)
                .active(true)
                .build();

        ApiResponse<ProductResponse> apiRes = ApiResponse.<ProductResponse>builder()
                .success(true)
                .data(product)
                .build();

        when(productClient.getProductById(productId)).thenReturn(ResponseEntity.ok(apiRes));
        when(productClient.updateStock(productId, -2)).thenReturn(ResponseEntity.ok().build());
        
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(UUID.randomUUID());
            return o;
        });

        OrderResponse response = orderService.placeOrder(request, principal);

        assertNotNull(response);
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(0, response.getTotalAmount().compareTo(BigDecimal.valueOf(2000.00)));
        verify(productClient, times(1)).updateStock(productId, -2);
        verify(kafkaProducer, times(1)).sendOrderCreatedEvent(any(OrderCreatedEvent.class));
    }

    @Test
    public void placeOrder_InsufficientStock_ThrowsException() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, "john", "john@example.com", "USER");

        OrderRequest request = OrderRequest.builder()
                .shippingAddress("123 Main St")
                .items(Arrays.asList(OrderItemRequest.builder().productId(productId).quantity(5).build()))
                .build();

        ProductResponse product = ProductResponse.builder()
                .id(productId)
                .name("Laptop")
                .price(BigDecimal.valueOf(1000.00))
                .stockQuantity(3) // Only 3 items in stock
                .active(true)
                .build();

        ApiResponse<ProductResponse> apiRes = ApiResponse.<ProductResponse>builder()
                .success(true)
                .data(product)
                .build();

        when(productClient.getProductById(productId)).thenReturn(ResponseEntity.ok(apiRes));

        assertThrows(BusinessException.class, () -> orderService.placeOrder(request, principal));
    }
}
