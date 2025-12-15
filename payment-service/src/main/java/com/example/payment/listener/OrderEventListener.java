package com.example.payment.listener;

import com.example.payment.service.PaymentService;
import com.example.shared.event.OrderEvent;
import com.example.shared.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventListener.class);
    
    private final PaymentService paymentService;

    public OrderEventListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "orders", groupId = "payment-service-group")
    public void handleOrderEvent(OrderEvent event) {
        logger.info("Received order event: {} - {}", event.getOrderId(), event.getStatus());
        
        if (event.getStatus() == OrderStatus.NEW) {
            paymentService.processPayment(event);
        } else if (event.getStatus() == OrderStatus.CONFIRMATION || 
                   event.getStatus() == OrderStatus.ROLLBACK || 
                   event.getStatus() == OrderStatus.REJECTED) {
            paymentService.handleFinalStatus(event);
        }
    }
}