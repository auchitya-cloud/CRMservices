package com.example.order.service;

import com.example.order.entity.Order;
import com.example.order.repository.OrderRepository;
import com.example.shared.event.OrderEvent;
import com.example.shared.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public Order createOrder(String customerId, Map<String, Integer> products, BigDecimal totalAmount) {
        String orderId = UUID.randomUUID().toString();
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setCustomerId(customerId);
        order.setProducts(products);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.NEW);
        
        order = orderRepository.save(order);
        
        // Send order event to Kafka
        OrderEvent event = new OrderEvent(orderId, customerId, products, totalAmount, OrderStatus.NEW, "order-service");
        kafkaTemplate.send("orders", orderId, event);
        
        logger.info("Created new order: {}", orderId);
        return order;
    }

    @Transactional
    public void processPaymentResponse(OrderEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            logger.warn("Order not found: {}", event.getOrderId());
            return;
        }

        order.setPaymentTransactionId(event.getTransactionId());
        order.setPaymentAccepted(event.getStatus() == OrderStatus.ACCEPT);
        
        orderRepository.save(order);
        checkAndFinalizeOrder(order);
        
        logger.info("Processed payment response for order: {} - {}", event.getOrderId(), event.getStatus());
    }

    @Transactional
    public void processStockResponse(OrderEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElse(null);
        if (order == null) {
            logger.warn("Order not found: {}", event.getOrderId());
            return;
        }

        order.setStockTransactionId(event.getTransactionId());
        order.setStockAccepted(event.getStatus() == OrderStatus.ACCEPT);
        
        orderRepository.save(order);
        checkAndFinalizeOrder(order);
        
        logger.info("Processed stock response for order: {} - {}", event.getOrderId(), event.getStatus());
    }

    private void checkAndFinalizeOrder(Order order) {
        if (order.getPaymentAccepted() != null && order.getStockAccepted() != null) {
            OrderStatus finalStatus;
            
            if (order.getPaymentAccepted() && order.getStockAccepted()) {
                finalStatus = OrderStatus.CONFIRMATION;
            } else {
                finalStatus = order.getPaymentAccepted() || order.getStockAccepted() ? 
                    OrderStatus.ROLLBACK : OrderStatus.REJECTED;
            }
            
            order.setStatus(finalStatus);
            orderRepository.save(order);
            
            // Send final status to Kafka
            OrderEvent finalEvent = new OrderEvent(
                order.getOrderId(), 
                order.getCustomerId(), 
                order.getProducts(), 
                order.getTotalAmount(), 
                finalStatus, 
                "order-service"
            );
            kafkaTemplate.send("orders", order.getOrderId(), finalEvent);
            
            logger.info("Finalized order: {} with status: {}", order.getOrderId(), finalStatus);
        }
    }
}