package com.example.productservice.repository;

import com.example.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category WHERE p.active = true",
           countQuery = "SELECT count(p) FROM Product p WHERE p.active = true")
    Page<Product> findAllActive(Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category WHERE p.category.id = :categoryId AND p.active = true",
           countQuery = "SELECT count(p) FROM Product p WHERE p.category.id = :categoryId AND p.active = true")
    Page<Product> findByCategoryId(@Param("categoryId") UUID categoryId, Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active = true",
           countQuery = "SELECT count(p) FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active = true")
    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    @Query(value = "SELECT p FROM Product p JOIN FETCH p.category WHERE p.category.id = :categoryId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active = true",
           countQuery = "SELECT count(p) FROM Product p WHERE p.category.id = :categoryId AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.active = true")
    Page<Product> findByCategoryIdAndNameContainingIgnoreCase(@Param("categoryId") UUID categoryId, @Param("name") String name, Pageable pageable);
}
