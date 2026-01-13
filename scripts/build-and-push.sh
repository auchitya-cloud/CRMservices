#!/bin/bash
# Build and push all Docker images to ECR

set -e

AWS_REGION=${AWS_REGION:-us-east-1}
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
TAG=${1:-latest}

echo "=== Building services ==="
cd "$(dirname "$0")/.."
mvn clean package -DskipTests

echo "=== Logging into ECR ==="
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY

echo "=== Building and pushing Docker images ==="
for service in order-service payment-service stock-service demo-ui; do
    echo "Building $service..."
    docker build -t $ECR_REGISTRY/$service:$TAG $service/
    docker push $ECR_REGISTRY/$service:$TAG
    echo "$service pushed successfully!"
done

echo "=== All images pushed to ECR ==="
