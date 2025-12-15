package com.example.payment.config;

import com.example.payment.entity.Account;
import com.example.payment.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final AccountRepository accountRepository;

    public DataInitializer(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create test accounts
        if (accountRepository.count() == 0) {
            accountRepository.save(new Account("customer-1", new BigDecimal("1000.00")));
            accountRepository.save(new Account("customer-2", new BigDecimal("500.00")));
            accountRepository.save(new Account("customer-3", new BigDecimal("50.00")));
        }
    }
}