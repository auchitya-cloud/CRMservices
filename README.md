# Order Microservice Platform

Event-driven microservices platform with Kafka for order processing.

## Architecture

```
┌─────────────┐     ┌─────────┐     ┌─────────────────┐     ┌───────────────┐
│ Order Svc   │────▶│  Kafka  │────▶│ Payment Service │────▶│ Stock Service │
│   :8080     │     │  :9092  │     │     :8081       │     │    :8082      │
└─────────────┘     └─────────┘     └─────────────────┘     └───────────────┘
                         │
                    ┌────┴────┐
                    │Kafka UI │
                    │  :8080  │
                    └─────────┘
```

## Services

| Service | Port | Description |
|---------|------|-------------|
| order-service | 8080 | Creates orders, publishes to Kafka |
| payment-service | 8081 | Processes payments from Kafka events |
| stock-service | 8082 | Updates inventory from Kafka events |
| kafka-ui | 8080 | Kafka monitoring dashboard |
| demo-ui | 80 | Demo frontend |

## Quick Start

### 1. Create ECR Repositories
```bash
chmod +x scripts/*.sh
./scripts/create-ecr-repos.sh
```

### 2. Build and Push Images
```bash
./scripts/build-and-push.sh
```

### 3. Deploy to EKS
```bash
export CLUSTER_NAME=your-cluster-name
./scripts/deploy.sh
```

### Or via ArgoCD
```bash
kubectl apply -f argocd/application.yaml
```

## Cost Optimization

- Single replica for all services (non-prod)
- Minimal resource requests/limits
- EmptyDir for Kafka storage (ephemeral)
- Lightweight base images

## Monitoring

Services expose Prometheus metrics at `/actuator/prometheus`. Your existing Prometheus/Grafana stack will auto-discover via annotations.

## Jenkins Pipeline

Configure Jenkins to use the `Jenkinsfile` for CI/CD. Ensure Jenkins has:
- AWS credentials configured
- Docker installed
- kubectl configured for EKS
