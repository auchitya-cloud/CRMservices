package com.example.payment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    private String customerId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal balance;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column
    private Instant updatedAt;

    public Account() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Account(String customerId, BigDecimal balance) {
        this();
        this.customerId = customerId;
        this.balance = balance;
    }

    // Getters and setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; this.updatedAt = Instant.now(); }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}