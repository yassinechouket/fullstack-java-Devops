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

        stage('Login to AWS ECR') {
			steps {
				withAWS(credentials: 'aws-creds', region: "${AWS_DEFAULT_REGION}") {
					sh "make build-image-login"
                }
            }
        }

        stage('Push Image to ECR') {
			steps {
				withAWS(credentials: 'aws-creds', region: 'eu-west-3') {
					sh 'make build-image-push'
        		}
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
