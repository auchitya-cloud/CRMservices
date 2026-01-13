package com.example.order.listener;

import com.example.order.service.OrderService;
import com.example.shared.event.OrderEvent;
import com.example.shared.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);
    
    private final OrderService orderService;

    public OrderEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "payment-events", groupId = "order-service-group")
    public void handlePaymentEvent(OrderEvent event) {
        logger.info("Received payment event: {} - {}", event.getOrderId(), event.getStatus());
        
        if (event.getStatus() == OrderStatus.ACCEPT || event.getStatus() == OrderStatus.REJECT) {
            orderService.processPaymentResponse(event);
        }
    }

    @KafkaListener(topics = "stock-events", groupId = "order-service-group")
    public void handleStockEvent(OrderEvent event) {
        logger.info("Received stock event: {} - {}", event.getOrderId(), event.getStatus());
        
        if (event.getStatus() == OrderStatus.ACCEPT || event.getStatus() == OrderStatus.REJECT) {
            orderService.processStockResponse(event);
        }
    }
}