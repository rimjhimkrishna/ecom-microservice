package com.example.productservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    @Digits(integer = 8, fraction = 2, message = "Price format must be up to 8 integer digits and 2 decimals")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 1, message = "Stock quantity must be at least 1")
    private Integer stockQuantity;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    private String imageUrl;
}
