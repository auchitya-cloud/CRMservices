#!/bin/bash

echo "Setting up test data..."

# Wait for services to be ready
sleep 30

# Create test customer account (Payment Service)
curl -X POST http://localhost:8081/actuator/health

# Create test products (Stock Service)  
curl -X POST http://localhost:8082/actuator/health

# Create a test order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer-1",
    "products": {
      "product-1": 2,
      "product-2": 1
    },
    "totalAmount": 99.99
  }'

echo "Test data setup completed!"