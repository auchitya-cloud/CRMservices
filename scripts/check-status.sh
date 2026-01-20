#!/bin/bash

echo "🔍 Checking Auchitya Platform Status..."
echo "=================================="

# Check namespace
echo "📦 Namespace:"
kubectl get namespace auchitya-platform 2>/dev/null || echo "❌ Namespace not found"

echo ""
echo "🏃 Pods Status:"
kubectl get pods -n auchitya-platform -o wide

echo ""
echo "🔧 Services:"
kubectl get svc -n auchitya-platform

echo ""
echo "🌐 Ingress:"
kubectl get ingress -n auchitya-platform

echo ""
echo "🔗 Load Balancer URL:"
LB_URL=$(kubectl get svc -n ingress-nginx ingress-nginx-controller -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null)
if [ -n "$LB_URL" ]; then
    echo "Load Balancer: http://$LB_URL"
    echo ""
    echo "🌐 Access URLs:"
    echo "Demo UI: http://$LB_URL/"
    echo "Kafka UI: http://$LB_URL/kafka-ui/"
    echo "Order API: http://$LB_URL/api/orders"
    echo "Payment API: http://$LB_URL/api/payments"
    echo "Stock API: http://$LB_URL/api/stocks"
else
    echo "❌ Load Balancer not found or not ready"
fi

echo ""
echo "🏥 Health Checks:"
if [ -n "$LB_URL" ]; then
    echo "Testing health endpoints..."
    curl -s -o /dev/null -w "Order Service: %{http_code}\n" "http://$LB_URL/actuator/order/health" || echo "Order Service: Failed to connect"
    curl -s -o /dev/null -w "Payment Service: %{http_code}\n" "http://$LB_URL/actuator/payment/health" || echo "Payment Service: Failed to connect"
    curl -s -o /dev/null -w "Stock Service: %{http_code}\n" "http://$LB_URL/actuator/stock/health" || echo "Stock Service: Failed to connect"
fi

echo ""
echo "📊 Resource Usage:"
kubectl top pods -n auchitya-platform 2>/dev/null || echo "Metrics not available (metrics-server not installed)"