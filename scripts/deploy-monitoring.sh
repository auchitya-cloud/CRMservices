#!/bin/bash

echo "🚀 Deploying Prometheus and Grafana monitoring stack..."

# Apply monitoring configurations
echo "📊 Deploying Prometheus..."
kubectl apply -f k8s/monitoring/prometheus-config.yaml
kubectl apply -f k8s/monitoring/prometheus.yaml

echo "📈 Deploying Grafana..."
kubectl apply -f k8s/monitoring/grafana-config.yaml
kubectl apply -f k8s/monitoring/grafana.yaml

# Wait for deployments to be ready
echo "⏳ Waiting for Prometheus to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/prometheus -n order-platform

echo "⏳ Waiting for Grafana to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/grafana -n order-platform

# Update ingress
echo "🌐 Updating ingress for monitoring access..."
kubectl apply -f k8s/ingress.yaml

echo "✅ Monitoring stack deployed successfully!"
echo ""
echo "🔗 Access URLs:"
echo "   Prometheus: http://your-domain/prometheus"
echo "   Grafana: http://your-domain/grafana"
echo ""
echo "🔐 Grafana credentials:"
echo "   Username: admin"
echo "   Password: admin123"
echo ""
echo "📊 To check status:"
echo "   kubectl get pods -n order-platform | grep -E '(prometheus|grafana)'"