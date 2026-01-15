pipeline {
    agent{
        docker {
            image 'balaji510/jenkins-agent:slim'
            args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }
    
    environment {
        AWS_REGION = 'us-east-1'
        ECR_REGISTRY = '331867785866.dkr.ecr.us-east-1.amazonaws.com/intern-project'
        AWS_ACCOUNT_ID = '331867785866'
    }
    
    stages {
        stage('Build Services') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Images'){
            steps{
                sh'''
                docker build -t order-service:${BUILD_NUMBER} ./order-service/.
                docker build -t payment-service:${BUILD_NUMBER} ./payment-service/.
                docker build -t stock-service:${BUILD_NUMBER} ./stock-service/.
                docker build -t demo-ui:${BUILD_NUMBER} ./demo-ui/.
                '''
            }
        }

        stage('Tagging Images'){
            steps{
                sh'''
                docker tag order-service:${BUILD_NUMBER} ${ECR_REGISTRY}/order-service:${BUILD_NUMBER}
                docker tag payment-service:${BUILD_NUMBER} ${ECR_REGISTRY}/payment-service:${BUILD_NUMBER}
                docker tag stock-service:${BUILD_NUMBER} ${ECR_REGISTRY}/stock-service:${BUILD_NUMBER}
                docker tag demo-ui:${BUILD_NUMBER} ${ECR_REGISTRY}/demo-ui:${BUILD_NUMBER}
                '''
            }
        }

        stage("Pushing Images to ECR"){
            steps{
                withCredentials([
                    [$class: 'AmazonWebServicesCredentialsGinding',
                     credentialsId: 'aws-cred',
                    ]
                ]){
                    sh'''
                    aws --version
                    echo Login_Done...
                    aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 331867785866.dkr.ecr.us-east-1.amazonaws.com
                    echo Pass_set...
                    docker push ${ECR_REGISTRY}/order-service:${BUILD_NUMBER}
                    docker push ${ECR_REGISTRY}/payment-service:${BUILD_NUMBER}
                    docker push ${ECR_REGISTRY}/stock-service:${BUILD_NUMBER}
                    docker push ${ECR_REGISTRY}/demo-ui:${BUILD_NUMBER}
                    echo done...
                    '''
                }
            }
        }
        stage('Update K8 Files') {
            environment {
                GIT_REPO_NAME = "ManifestFiles1"
                GIT_USER_NAME = "balaji-510"
            }
            steps {
                withCredentials([string(credentialsId: 'github1', variable: 'GITHUB_TOKEN')]) {
                    sh '''
                        git clone https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/ManifestFiles1 manifests
                        cd manifests
                        git config --global --add safe.directory "$WORKSPACE"
                        git config user.email "balaji_from_jenkins@gmail.com"
                        git config user.name "Balaji G"
                        sed -i "s|image: .*order-service:.*|image: ${ORDER_REPO_URI}:${BUILD_NUMBER}|" k8s/services/order-service.yaml
                        sed -i "s|image: .*payment-service:.*|image: ${PAYMENT_REPO_URI}:${BUILD_NUMBER}|" k8s/services/payment-service.yaml
                        sed -i "s|image: .*stock-service:.*|image: ${STOCK_REPO_URI}:${BUILD_NUMBER}|" k8s/services/stock-service.yaml
                        sed -i "s|image: .*demo-ui:.*|image: ${DEMOUI_REPO_URI}:${BUILD_NUMBER}|" k8s/services/demo-ui.yaml

                        git add k8s/services/*.yaml

                        git commit -m "Update deployment image to version ${BUILD_NUMBER}"
                        git push https://${GITHUB_TOKEN}@github.com/${GIT_USER_NAME}/${GIT_REPO_NAME} HEAD:main
                    '''
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}
