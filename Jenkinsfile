pipeline {
	agent any

    environment {
		AWS_ACCOUNT_ID      = "742460038063"
        AWS_DEFAULT_REGION  = "eu-west-3"
        AWS_ECR_DOMAIN      = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_DEFAULT_REGION}.amazonaws.com"
        IMAGE_TAG           = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
    }

    stages {

		stage('Checkout Code') {
			steps {
				checkout scm
            }
        }

        stage('Build Java App') {
			steps {
				echo "Building Spring Boot application..."
                sh "make build"
            }
        }

        stage('Build Docker Image') {
			steps {
				echo "Building Docker image..."
                sh "make build-image"
            }
        }

        stage('Login to AWS ECR & Push Image') {
			steps {
				withAWS(credentials: 'aws-creds', region: "${AWS_DEFAULT_REGION}") {
					echo "Logging into ECR..."
                    sh "make build-image-login"
                    echo "Pushing Docker image to ECR..."
                    sh "make build-image-push"
                }
            }
        }

        stage('Deploy to Kubernetes') {
			steps {
				echo "Deploying to Kubernetes with IMAGE_TAG=${IMAGE_TAG}"

                sh """
                    sed -i 's#\${IMAGE_TAG}#${IMAGE_TAG}#g' k8s/BackendDeployment.yaml

                    kubectl apply -f k8s/ZookeeperDeployment.yaml
                    kubectl apply -f k8s/KafkaDeployment.yaml
                    kubectl apply -f k8s/postgresDeployment.yaml
                    kubectl apply -f k8s/BackendDeployment.yaml
                """
            }
        }
    }
}