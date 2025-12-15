# Kafka Event-Driven Order Platform

A distributed order processing system using Spring Boot microservices and Kafka for event-driven communication.

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Order Service │    │ Payment Service │    │  Stock Service  │
│                 │    │                 │    │                 │
│ - Orchestrates  │    │ - Account mgmt  │    │ - Inventory mgmt│
│ - Saga pattern  │    │ - Local txn     │    │ - Local txn     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │      Kafka      │
                    │                 │
                    │ - orders        │
                    │ - payment-events│
                    │ - stock-events  │
                    └─────────────────┘
```

## Services

### Order Service
- Initiates orders with NEW status
- Orchestrates distributed transactions using Saga pattern
- Joins responses from payment and stock services
- Sends final status (CONFIRMATION/ROLLBACK/REJECTED)

### Payment Service
- Processes payment transactions
- Responds with ACCEPT/REJECT status
- Commits/rollbacks based on final order status

### Stock Service
- Manages inventory transactions
- Responds with ACCEPT/REJECT status
- Commits/rollbacks based on final order status

## Event Flow

1. Order Service → NEW order → Kafka
2. Payment & Stock Services → Process → ACCEPT/REJECT → Kafka
3. Order Service → Join responses → CONFIRMATION/ROLLBACK/REJECTED → Kafka
4. Payment & Stock Services → Commit/Rollback local transactions

## Features

- Spring Boot Actuator metrics
- Prometheus endpoints
- Structured JSON logging
- OpenTelemetry tracing support
- Docker containerization
- Kubernetes deployment manifests for EKS

## Quick Start

```bash
# Start Kafka
docker-compose up -d kafka zookeeper

# Build services
./mvnw clean package -DskipTests

# Run services
java -jar order-service/target/order-service-1.0.0.jar
java -jar payment-service/target/payment-service-1.0.0.jar
java -jar stock-service/target/stock-service-1.0.0.jar
```