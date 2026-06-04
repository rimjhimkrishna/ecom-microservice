package com.example.orderservice.feign;

import com.example.orderservice.dto.response.ApiResponse;
import com.example.orderservice.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable("id") UUID id);

    @PatchMapping("/api/v1/products/{id}/stock")
    ResponseEntity<ApiResponse<Void>> updateStock(
            @PathVariable("id") UUID id,
            @RequestParam("quantityChange") Integer quantityChange
    );
}
