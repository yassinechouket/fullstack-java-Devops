pipeline {
	agent {
		docker {
			image 'aquasec/trivy:latest'
            args '-u root:root'    // allow file access inside container
        }
    }

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

        stage('Run Unit Tests') {
			steps {
				echo "Running unit tests..."
                sh "make test"
            }
        }

        stage('Build Java App') {
			steps {
				echo "Building Spring Boot application..."
                sh "make build"
            }
        }

        stage('Trivy Scan (Filesystem)') {
			steps {
				echo "Running Trivy filesystem scan..."
                sh """
                    trivy fs . \
                        --severity HIGH,CRITICAL \
                        --ignore-unfixed \
                        --exit-code 1 \
                        --output trivy-fs-report.txt
                """
            }
            post {
				always { archiveArtifacts artifacts: 'trivy-fs-report.txt' }
            }
        }

        stage('Build Docker Image') {
			steps {
				echo "Building Docker image..."
                sh "make build-image"
            }
        }

        stage('Trivy Scan (Docker Image)') {
			steps {
				echo "Running Trivy image scan..."
                sh """
                    trivy image ${AWS_ECR_DOMAIN}/spring-kafka-service:${IMAGE_TAG} \
                        --severity HIGH,CRITICAL \
                        --ignore-unfixed \
                        --exit-code 1 \
                        --output trivy-image-report.txt
                """
            }
            post {
				always { archiveArtifacts artifacts: 'trivy-image-report.txt' }
            }
        }

        stage('Login to AWS ECR') {
			steps {
				echo "Logging into AWS ECR..."
                sh "make build-image-login"
            }
        }

        stage('Push Image to ECR') {
			steps {
				echo "Pushing Docker image to ECR..."
                sh "make build-image-push"
            }
        }

        stage('Deploy to Kubernetes') {
			steps {
				echo "Deploying to Kubernetes with IMAGE_TAG=${IMAGE_TAG}"

                sh """
                    sed -i 's|\\${IMAGE_TAG}|${IMAGE_TAG}|g' k8s/backend.yaml

                    kubectl apply -f k8s/zookeeper.yaml
                    kubectl apply -f k8s/kafka.yaml
                    kubectl apply -f k8s/postgres.yaml
                    kubectl apply -f k8s/backend.yaml
                """
            }
        }
    }
}
