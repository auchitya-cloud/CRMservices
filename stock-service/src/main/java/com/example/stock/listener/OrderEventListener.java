package com.example.stock.listener;

import com.example.stock.service.StockService;
import com.example.shared.event.OrderEvent;
import com.example.shared.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);
    
    private final StockService stockService;

    public OrderEventListener(StockService stockService) {
        this.stockService = stockService;
    }

    @KafkaListener(topics = "orders", groupId = "stock-service-group")
    public void handleOrderEvent(OrderEvent event) {
        logger.info("Received order event: {} - {}", event.getOrderId(), event.getStatus());
        
        if (event.getStatus() == OrderStatus.NEW) {
            stockService.processStock(event);
        } else if (event.getStatus() == OrderStatus.CONFIRMATION || 
                   event.getStatus() == OrderStatus.ROLLBACK || 
                   event.getStatus() == OrderStatus.REJECTED) {
            stockService.handleFinalStatus(event);
        }
    }
}