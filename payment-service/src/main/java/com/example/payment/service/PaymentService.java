package com.example.payment.service;

import com.example.payment.entity.Account;
import com.example.payment.entity.PaymentTransaction;
import com.example.payment.repository.AccountRepository;
import com.example.payment.repository.PaymentTransactionRepository;
import com.example.shared.event.OrderEvent;
import com.example.shared.model.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private final AccountRepository accountRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public PaymentService(AccountRepository accountRepository, 
                         PaymentTransactionRepository transactionRepository,
                         KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void processPayment(OrderEvent orderEvent) {
        String transactionId = UUID.randomUUID().toString();
        
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionId(transactionId);
        transaction.setOrderId(orderEvent.getOrderId());
        transaction.setCustomerId(orderEvent.getCustomerId());
        transaction.setAmount(orderEvent.getTotalAmount());
        transaction.setStatus(PaymentTransaction.TransactionStatus.PENDING);
        
        transactionRepository.save(transaction);
        
        Account account = accountRepository.findById(orderEvent.getCustomerId()).orElse(null);
        OrderStatus responseStatus;
        
        if (account != null && account.getBalance().compareTo(orderEvent.getTotalAmount()) >= 0) {
            // Reserve funds (deduct from balance)
            account.setBalance(account.getBalance().subtract(orderEvent.getTotalAmount()));
            accountRepository.save(account);
            responseStatus = OrderStatus.ACCEPT;
            logger.info("Payment reserved for order: {} - Amount: {}", orderEvent.getOrderId(), orderEvent.getTotalAmount());
        } else {
            responseStatus = OrderStatus.REJECT;
            logger.warn("Payment rejected for order: {} - Insufficient funds", orderEvent.getOrderId());
        }
        
        // Send response to Kafka
        OrderEvent responseEvent = new OrderEvent(
            orderEvent.getOrderId(),
            orderEvent.getCustomerId(),
            orderEvent.getProducts(),
            orderEvent.getTotalAmount(),
            responseStatus,
            "payment-service"
        );
        responseEvent.setTransactionId(transactionId);
        
        kafkaTemplate.send("payment-events", orderEvent.getOrderId(), responseEvent);
    }

    @Transactional
    public void handleFinalStatus(OrderEvent orderEvent) {
        PaymentTransaction transaction = transactionRepository.findByOrderId(orderEvent.getOrderId()).orElse(null);
        if (transaction == null) {
            logger.warn("Payment transaction not found for order: {}", orderEvent.getOrderId());
            return;
        }

        if (orderEvent.getStatus() == OrderStatus.CONFIRMATION) {
            transaction.setStatus(PaymentTransaction.TransactionStatus.COMMITTED);
            logger.info("Payment committed for order: {}", orderEvent.getOrderId());
        } else if (orderEvent.getStatus() == OrderStatus.ROLLBACK || orderEvent.getStatus() == OrderStatus.REJECTED) {
            transaction.setStatus(PaymentTransaction.TransactionStatus.ROLLED_BACK);
            
            // Restore funds if they were reserved
            Account account = accountRepository.findById(orderEvent.getCustomerId()).orElse(null);
            if (account != null) {
                account.setBalance(account.getBalance().add(orderEvent.getTotalAmount()));
                accountRepository.save(account);
            }
            logger.info("Payment rolled back for order: {}", orderEvent.getOrderId());
        }
        
        transactionRepository.save(transaction);
    }
}