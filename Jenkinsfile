pipeline {
    agent any
    
    environment {
        AWS_REGION = 'us-east-1'  // Update with your region
        ECR_REGISTRY = ''  // Will be set dynamically
        AWS_ACCOUNT_ID = ''  // Will be set dynamically
    }
    
    stages {
        stage('Setup') {
            steps {
                script {
                    AWS_ACCOUNT_ID = sh(script: 'aws sts get-caller-identity --query Account --output text', returnStdout: true).trim()
                    ECR_REGISTRY = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
                }
            }
        }
        
        stage('Build Services') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Docker Login') {
            steps {
                sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}"
            }
        }
        
        stage('Build & Push Images') {
            parallel {
                stage('Order Service') {
                    steps {
                        dir('order-service') {
                            sh "docker build -t ${ECR_REGISTRY}/order-service:${BUILD_NUMBER} -t ${ECR_REGISTRY}/order-service:latest ."
                            sh "docker push ${ECR_REGISTRY}/order-service:${BUILD_NUMBER}"
                            sh "docker push ${ECR_REGISTRY}/order-service:latest"
                        }
                    }
                }
                stage('Payment Service') {
                    steps {
                        dir('payment-service') {
                            sh "docker build -t ${ECR_REGISTRY}/payment-service:${BUILD_NUMBER} -t ${ECR_REGISTRY}/payment-service:latest ."
                            sh "docker push ${ECR_REGISTRY}/payment-service:${BUILD_NUMBER}"
                            sh "docker push ${ECR_REGISTRY}/payment-service:latest"
                        }
                    }
                }
                stage('Stock Service') {
                    steps {
                        dir('stock-service') {
                            sh "docker build -t ${ECR_REGISTRY}/stock-service:${BUILD_NUMBER} -t ${ECR_REGISTRY}/stock-service:latest ."
                            sh "docker push ${ECR_REGISTRY}/stock-service:${BUILD_NUMBER}"
                            sh "docker push ${ECR_REGISTRY}/stock-service:latest"
                        }
                    }
                }
                stage('Demo UI') {
                    steps {
                        dir('demo-ui') {
                            sh "docker build -t ${ECR_REGISTRY}/demo-ui:${BUILD_NUMBER} -t ${ECR_REGISTRY}/demo-ui:latest ."
                            sh "docker push ${ECR_REGISTRY}/demo-ui:${BUILD_NUMBER}"
                            sh "docker push ${ECR_REGISTRY}/demo-ui:latest"
                        }
                    }
                }
            }
        }
        
        stage('Update K8s Manifests') {
            steps {
                script {
                    // Update image tags in K8s manifests
                    sh """
                        sed -i 's|\\\${ECR_REGISTRY}|${ECR_REGISTRY}|g' k8s/services/*.yaml
                    """
                }
            }
        }
        
        stage('Deploy via ArgoCD') {
            steps {
                // ArgoCD will auto-sync if configured, or trigger manually
                sh 'argocd app sync order-platform --prune || echo "ArgoCD will auto-sync"'
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}
