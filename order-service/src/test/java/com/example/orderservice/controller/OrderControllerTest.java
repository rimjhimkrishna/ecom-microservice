package com.example.orderservice.controller;

import com.example.orderservice.dto.response.OrderResponse;
import com.example.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    public void getOrderById_ReturnsOk() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponse orderResponse = OrderResponse.builder()
                .id(orderId)
                .userId(UUID.randomUUID())
                .status("CONFIRMED")
                .totalAmount(BigDecimal.valueOf(200.00))
                .shippingAddress("123 Main St")
                .items(Collections.emptyList())
                .build();

        when(orderService.getOrderById(eq(orderId), any())).thenReturn(orderResponse);

        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.totalAmount").value(200.00));
    }
}
