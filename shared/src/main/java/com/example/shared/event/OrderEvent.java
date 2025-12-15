package com.example.shared.event;

import com.example.shared.model.OrderStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public class OrderEvent {
    @JsonProperty("orderId")
    private String orderId;
    
    @JsonProperty("customerId")
    private String customerId;
    
    @JsonProperty("products")
    private Map<String, Integer> products; // productId -> quantity
    
    @JsonProperty("totalAmount")
    private BigDecimal totalAmount;
    
    @JsonProperty("status")
    private OrderStatus status;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("source")
    private String source;
    
    @JsonProperty("transactionId")
    private String transactionId;

    public OrderEvent() {
        this.timestamp = Instant.now();
    }

    public OrderEvent(String orderId, String customerId, Map<String, Integer> products, 
                     BigDecimal totalAmount, OrderStatus status, String source) {
        this();
        this.orderId = orderId;
        this.customerId = customerId;
        this.products = products;
        this.totalAmount = totalAmount;
        this.status = status;
        this.source = source;
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
    public void setStatus(OrderStatus status) { this.status = status; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}