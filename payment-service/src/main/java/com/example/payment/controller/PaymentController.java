package com.example.payment.controller;

import com.example.payment.entity.Account;
import com.example.payment.entity.PaymentTransaction;
import com.example.payment.repository.AccountRepository;
import com.example.payment.repository.PaymentTransactionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final AccountRepository accountRepository;

    public PaymentController(PaymentTransactionRepository paymentTransactionRepository,
                            AccountRepository accountRepository) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public ResponseEntity<List<PaymentTransaction>> getAllPayments() {
        return ResponseEntity.ok(paymentTransactionRepository.findAll());
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<PaymentTransaction> getPayment(@PathVariable String transactionId) {
        return paymentTransactionRepository.findById(transactionId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentTransaction> getPaymentByOrderId(@PathVariable String orderId) {
        return paymentTransactionRepository.findByOrderId(orderId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<Account>> getAllAccounts() {
        return ResponseEntity.ok(accountRepository.findAll());
    }

    @GetMapping("/accounts/{customerId}")
    public ResponseEntity<Account> getAccount(@PathVariable String customerId) {
        return accountRepository.findById(customerId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
