package com.example.productservice.service;

import com.example.productservice.dto.request.ProductRequest;
import com.example.productservice.dto.response.ProductResponse;
import com.example.productservice.exception.InsufficientStockException;
import com.example.productservice.model.Category;
import com.example.productservice.model.Product;
import com.example.productservice.repository.CategoryRepository;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    public void createProduct_Success() {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder().id(categoryId).name("Electronics").build();

        ProductRequest request = ProductRequest.builder()
                .name("Laptop")
                .description("Dell XPS 15")
                .price(BigDecimal.valueOf(1299.99))
                .stockQuantity(10)
                .categoryId(categoryId)
                .build();

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Laptop")
                .description("Dell XPS 15")
                .price(BigDecimal.valueOf(1299.99))
                .stockQuantity(10)
                .category(category)
                .active(true)
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponse response = productService.createProduct(request);

        assertNotNull(response);
        assertEquals("Laptop", response.getName());
        assertEquals("Electronics", response.getCategoryName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void getProductById_Success() {
        UUID productId = UUID.randomUUID();
        Category category = Category.builder().name("Electronics").build();
        Product product = Product.builder()
                .id(productId)
                .name("Laptop")
                .category(category)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(productId);

        assertNotNull(response);
        assertEquals("Laptop", response.getName());
    }

    @Test
    public void updateStock_ReduceSuccess() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(productId)
                .name("Laptop")
                .stockQuantity(10)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);

        productService.updateStock(productId, -3);

        assertEquals(7, product.getStockQuantity());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    public void updateStock_InsufficientStock_ThrowsException() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(productId)
                .name("Laptop")
                .stockQuantity(5)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class, () -> productService.updateStock(productId, -6));
    }
}
