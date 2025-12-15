#!/bin/bash

echo "Building Kafka Order Platform..."

# Build the project
./mvnw clean package -DskipTests

# Build Docker images
echo "Building Docker images..."
docker build -t order-service:1.0.0 order-service/
docker build -t payment-service:1.0.0 payment-service/
docker build -t stock-service:1.0.0 stock-service/

echo "Build completed successfully!"