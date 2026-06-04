package com.example.orderservice.service;

import com.example.orderservice.dto.request.OrderRequest;
import com.example.orderservice.dto.request.OrderStatusUpdateRequest;
import com.example.orderservice.dto.response.OrderResponse;
import com.example.orderservice.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest request, UserPrincipal principal);
    Page<OrderResponse> getMyOrders(UserPrincipal principal, Pageable pageable);
    OrderResponse getOrderById(UUID id, UserPrincipal principal);
    OrderResponse updateOrderStatus(UUID id, OrderStatusUpdateRequest request);
    void cancelOrder(UUID id, UserPrincipal principal);
    Page<OrderResponse> getAllOrders(Pageable pageable);
}
