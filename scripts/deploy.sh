#!/bin/bash
# Deploy to EKS cluster

set -e

AWS_REGION=${AWS_REGION:-us-east-1}
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_REGISTRY="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
CLUSTER_NAME=${CLUSTER_NAME:-your-eks-cluster}

echo "=== Updating kubeconfig ==="
aws eks update-kubeconfig --name $CLUSTER_NAME --region $AWS_REGION

echo "=== Replacing ECR registry in manifests ==="
cd "$(dirname "$0")/../k8s"

# Create temp directory for processed manifests
mkdir -p /tmp/k8s-deploy
cp -r . /tmp/k8s-deploy/

# Replace ECR placeholder
find /tmp/k8s-deploy -name "*.yaml" -exec sed -i.bak "s|\${ECR_REGISTRY}|${ECR_REGISTRY}|g" {} \;

echo "=== Deploying to Kubernetes ==="
kubectl apply -k /tmp/k8s-deploy/

echo "=== Waiting for deployments ==="
kubectl -n order-platform rollout status deployment/zookeeper --timeout=120s
kubectl -n order-platform rollout status deployment/kafka --timeout=180s
kubectl -n order-platform rollout status deployment/order-service --timeout=120s
kubectl -n order-platform rollout status deployment/payment-service --timeout=120s
kubectl -n order-platform rollout status deployment/stock-service --timeout=120s
kubectl -n order-platform rollout status deployment/kafka-ui --timeout=120s
kubectl -n order-platform rollout status deployment/demo-ui --timeout=60s

echo "=== Deployment complete! ==="
kubectl -n order-platform get pods
kubectl -n order-platform get svc
kubectl -n order-platform get ingress
