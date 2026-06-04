package com.example.orderservice.repository;

import com.example.orderservice.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.items WHERE o.userId = :userId",
           countQuery = "SELECT count(o) FROM Order o WHERE o.userId = :userId")
    Page<Order> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);
    
    @Query(value = "SELECT o FROM Order o JOIN FETCH o.items",
           countQuery = "SELECT count(o) FROM Order o")
    Page<Order> findAllWithItems(Pageable pageable);
}
