package com.example.stock.service;

import com.example.stock.entity.Product;
import com.example.stock.entity.StockTransaction;
import com.example.stock.repository.ProductRepository;
import com.example.stock.repository.StockTransactionRepository;
import com.example.shared.event.OrderEvent;
import com.example.shared.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class StockService {
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    
    private final ProductRepository productRepository;
    private final StockTransactionRepository transactionRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public StockService(ProductRepository productRepository, 
                       StockTransactionRepository transactionRepository,
                       KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void processStock(OrderEvent orderEvent) {
        String transactionId = UUID.randomUUID().toString();
        
        StockTransaction transaction = new StockTransaction();
        transaction.setTransactionId(transactionId);
        transaction.setOrderId(orderEvent.getOrderId());
        transaction.setProducts(orderEvent.getProducts());
        transaction.setStatus(StockTransaction.TransactionStatus.PENDING);
        
        transactionRepository.save(transaction);
        
        boolean stockAvailable = true;
        
        // Check and reserve stock
        for (Map.Entry<String, Integer> entry : orderEvent.getProducts().entrySet()) {
            String productId = entry.getKey();
            Integer requestedQuantity = entry.getValue();
            
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || product.getQuantity() < requestedQuantity) {
                stockAvailable = false;
                logger.warn("Insufficient stock for product: {} - Requested: {}, Available: {}", 
                    productId, requestedQuantity, product != null ? product.getQuantity() : 0);
                break;
            }
        }
        
        OrderStatus responseStatus;
        if (stockAvailable) {
            // Reserve stock (deduct from inventory)
            for (Map.Entry<String, Integer> entry : orderEvent.getProducts().entrySet()) {
                String productId = entry.getKey();
                Integer requestedQuantity = entry.getValue();
                
                Product product = productRepository.findById(productId).get();
                product.setQuantity(product.getQuantity() - requestedQuantity);
                productRepository.save(product);
            }
            responseStatus = OrderStatus.ACCEPT;
            logger.info("Stock reserved for order: {}", orderEvent.getOrderId());
        } else {
            responseStatus = OrderStatus.REJECT;
            logger.warn("Stock rejected for order: {} - Insufficient inventory", orderEvent.getOrderId());
        }
        
        // Send response to Kafka
        OrderEvent responseEvent = new OrderEvent(
            orderEvent.getOrderId(),
            orderEvent.getCustomerId(),
            orderEvent.getProducts(),
            orderEvent.getTotalAmount(),
            responseStatus,
            "stock-service"
        );
        responseEvent.setTransactionId(transactionId);
        
        kafkaTemplate.send("stock-events", orderEvent.getOrderId(), responseEvent);
    }

    @Transactional
    public void handleFinalStatus(OrderEvent orderEvent) {
        StockTransaction transaction = transactionRepository.findByOrderId(orderEvent.getOrderId()).orElse(null);
        if (transaction == null) {
            logger.warn("Stock transaction not found for order: {}", orderEvent.getOrderId());
            return;
        }

        if (orderEvent.getStatus() == OrderStatus.CONFIRMATION) {
            transaction.setStatus(StockTransaction.TransactionStatus.COMMITTED);
            logger.info("Stock committed for order: {}", orderEvent.getOrderId());
        } else if (orderEvent.getStatus() == OrderStatus.ROLLBACK || orderEvent.getStatus() == OrderStatus.REJECTED) {
            transaction.setStatus(StockTransaction.TransactionStatus.ROLLED_BACK);
            
            // Restore stock if it was reserved
            for (Map.Entry<String, Integer> entry : orderEvent.getProducts().entrySet()) {
                String productId = entry.getKey();
                Integer quantity = entry.getValue();
                
                Product product = productRepository.findById(productId).orElse(null);
                if (product != null) {
                    product.setQuantity(product.getQuantity() + quantity);
                    productRepository.save(product);
                }
            }
            logger.info("Stock rolled back for order: {}", orderEvent.getOrderId());
        }
        
        transactionRepository.save(transaction);
    }
}