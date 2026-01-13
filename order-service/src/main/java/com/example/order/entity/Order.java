package com.example.order.entity;

import com.example.shared.model.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    private String orderId;
    
    @Column(nullable = false)
    private String customerId;
    
    @ElementCollection
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<String, Integer> products;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column
    private Instant updatedAt;
    
    @Column
    private String paymentTransactionId;
    
    @Column
    private String stockTransactionId;
    
    @Column
    private Boolean paymentAccepted;
    
    @Column
    private Boolean stockAccepted;

    public Order() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters and setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public Map<String, Integer> getProducts() { return products; }
    public void setProducts(Map<String, Integer> products) { this.products = products; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; this.updatedAt = Instant.now(); }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(String paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; }

    public String getStockTransactionId() { return stockTransactionId; }
    public void setStockTransactionId(String stockTransactionId) { this.stockTransactionId = stockTransactionId; }

    public Boolean getPaymentAccepted() { return paymentAccepted; }
    public void setPaymentAccepted(Boolean paymentAccepted) { this.paymentAccepted = paymentAccepted; }

    public Boolean getStockAccepted() { return stockAccepted; }
    public void setStockAccepted(Boolean stockAccepted) { this.stockAccepted = stockAccepted; }
}