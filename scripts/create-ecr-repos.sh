#!/bin/bash
# Create ECR repositories for all services

AWS_REGION=${AWS_REGION:-us-east-1}

REPOS=("order-service" "payment-service" "stock-service" "demo-ui")

for repo in "${REPOS[@]}"; do
    echo "Creating ECR repository: $repo"
    aws ecr create-repository \
        --repository-name "$repo" \
        --region "$AWS_REGION" \
        --image-scanning-configuration scanOnPush=true \
        --encryption-configuration encryptionType=AES256 \
        2>/dev/null || echo "Repository $repo already exists"
done

echo "ECR repositories ready!"
aws ecr describe-repositories --region "$AWS_REGION" --query 'repositories[*].repositoryUri' --output table
