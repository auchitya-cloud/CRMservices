#!/bin/bash

echo "🔧 Fixing Demo UI deployment..."

# First, let's check if Docker is running
if ! docker info &> /dev/null; then
    echo "❌ Docker is not running. Please start Docker Desktop first."
    exit 1
fi

# Build and push new demo-ui image
echo "🏗️ Building demo-ui image..."
cd ../demo-ui
docker build --platform linux/amd64 -t 331867785866.dkr.ecr.us-east-1.amazonaws.com/intern-project/order-service:demo-ui .

echo "📤 Pushing to ECR..."
docker push 331867785866.dkr.ecr.us-east-1.amazonaws.com/intern-project/order-service:demo-ui

echo "🔄 Updating deployment..."
kubectl set image deployment/demo-ui demo-ui=331867785866.dkr.ecr.us-east-1.amazonaws.com/intern-project/order-service:demo-ui -n auchitya-platform

echo "⏳ Waiting for rollout..."
kubectl rollout status deployment/demo-ui -n auchitya-platform

echo "📊 Checking pod status..."
kubectl get pods -n auchitya-platform | grep demo-ui

echo "✅ Demo UI fix complete!"