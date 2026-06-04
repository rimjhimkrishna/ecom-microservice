package com.example.productservice.service;

import com.example.productservice.dto.request.CategoryRequest;
import com.example.productservice.dto.response.CategoryResponse;
import com.example.productservice.exception.DuplicateResourceException;
import com.example.productservice.model.Category;
import com.example.productservice.repository.CategoryRepository;
import com.example.productservice.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    public void createCategory_Success() {
        CategoryRequest request = CategoryRequest.builder()
                .name("Books")
                .description("All kinds of books")
                .build();

        Category category = Category.builder()
                .id(UUID.randomUUID())
                .name("Books")
                .description("All kinds of books")
                .build();

        when(categoryRepository.existsByName("Books")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.createCategory(request);

        assertNotNull(response);
        assertEquals("Books", response.getName());
    }

    @Test
    public void createCategory_DuplicateName_ThrowsException() {
        CategoryRequest request = CategoryRequest.builder().name("Books").build();

        when(categoryRepository.existsByName("Books")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> categoryService.createCategory(request));
    }

    @Test
    public void getAllCategories_Success() {
        Category cat1 = Category.builder().name("Books").build();
        Category cat2 = Category.builder().name("Toys").build();

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(cat1, cat2));

        List<CategoryResponse> response = categoryService.getAllCategories();

        assertEquals(2, response.size());
        assertEquals("Books", response.get(0).getName());
        assertEquals("Toys", response.get(1).getName());
    }
}
