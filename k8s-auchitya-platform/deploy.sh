#!/bin/bash

echo "🚀 Deploying Auchitya Platform to EKS..."

# Check if kubectl is configured
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ kubectl is not configured or cluster is not accessible"
    exit 1
fi

# Check if namespace exists, create if not
if ! kubectl get namespace auchitya-platform &> /dev/null; then
    echo "📦 Creating namespace..."
    kubectl apply -f namespace.yaml
else
    echo "✅ Namespace auchitya-platform already exists"
fi

# Deploy in order
echo "🐘 Deploying Zookeeper..."
kubectl apply -f zookeeper.yaml

echo "⏳ Waiting for Zookeeper to be ready..."
kubectl wait --for=condition=available --timeout=120s deployment/zookeeper -n auchitya-platform

echo "📨 Deploying Kafka..."
kubectl apply -f kafka.yaml

echo "⏳ Waiting for Kafka to be ready..."
kubectl wait --for=condition=available --timeout=180s deployment/kafka -n auchitya-platform

echo "🔧 Deploying Services..."
kubectl apply -f services.yaml

echo "⏳ Waiting for services to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/order-service -n auchitya-platform
kubectl wait --for=condition=available --timeout=300s deployment/payment-service -n auchitya-platform
kubectl wait --for=condition=available --timeout=300s deployment/stock-service -n auchitya-platform
kubectl wait --for=condition=available --timeout=120s deployment/demo-ui -n auchitya-platform
kubectl wait --for=condition=available --timeout=120s deployment/kafka-ui -n auchitya-platform

echo "🌐 Deploying Ingress..."
kubectl apply -f ingress.yaml

echo "📊 Deployment Status:"
kubectl get pods -n auchitya-platform

echo "🔗 Getting Load Balancer URL..."
kubectl get svc -n ingress-nginx | grep LoadBalancer

echo "✅ Deployment complete!"
echo ""
echo "🌐 Access URLs:"
echo "Demo UI: http://<LOAD_BALANCER_IP>/"
echo "Kafka UI: http://<LOAD_BALANCER_IP>/kafka-ui/"
echo "Order API: http://<LOAD_BALANCER_IP>/api/orders"
echo "Payment API: http://<LOAD_BALANCER_IP>/api/payments"
echo "Stock API: http://<LOAD_BALANCER_IP>/api/stocks"