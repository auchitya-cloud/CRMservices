package com.example.stock.config;

import com.example.stock.entity.Product;
import com.example.stock.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create test products
        if (productRepository.count() == 0) {
            productRepository.save(new Product("product-1", "Laptop", 10));
            productRepository.save(new Product("product-2", "Mouse", 50));
            productRepository.save(new Product("product-3", "Keyboard", 25));
            productRepository.save(new Product("product-4", "Monitor", 5));
        }
    }
}