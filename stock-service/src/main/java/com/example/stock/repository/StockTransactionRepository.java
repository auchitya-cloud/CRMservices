package com.example.stock.repository;

import com.example.stock.entity.StockTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, String> {
    Optional<StockTransaction> findByOrderId(String orderId);
}