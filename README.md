# Smart Baggage Reclaim Management System

A containerized Spring Boot application with Kafka messaging and PostgreSQL database, demonstrating modern DevOps practices.

## 🚀 Quick Start

```bash
# Start all services
docker compose up -d

# Stop services
docker compose down -v
```

**Services Available:**
- Application: `http://localhost:8080`
- PostgreSQL: `localhost:5336`
- Kafka: `localhost:9092`

## 📋 Prerequisites

- Docker & Docker Compose
- AWS CLI (for ECR deployments)
- Make (optional)

## 🏗️ Architecture

**Stack:** Spring Boot (Java 21) + PostgreSQL 15 + Kafka 3.4 + Zookeeper

**Services:**
- Spring Boot application for business logic
- PostgreSQL for data persistence
- Kafka for event streaming
- Zookeeper for Kafka coordination

## 🎯 Key Design Decisions

### 1. Multi-Stage Dockerfile
**Why:** Reduce image size by ~70% and improve security.

- **Stage 1 (Build):** Maven build with dependency caching
- **Stage 2 (Runtime):** Alpine Linux + OpenJDK 21 only

**Benefits:** Smaller images, faster deployments, no build tools in production.

### 2. Docker Compose Setup
**Why:** Simple local development with service orchestration.

- Health checks prevent startup race conditions
- Custom ports avoid conflicts (PostgreSQL: 5336)
- Named volumes for data persistence
- Dependency management (Kafka waits for Zookeeper)

### 3. CI/CD Pipeline (GitHub Actions)
**Why:** Automated testing and deployment on every commit.

**Flow:**
```
Push/PR → Build Image → Push to ECR → Pull & Test → Cleanup
```

**Features:**
- Git SHA tagging for immutable artifacts
- AWS ECR for private registry
- BuildKit cache for faster builds
- Automatic testing with full stack

### 4. Image Tagging Strategy
**Format:** `{ECR_DOMAIN}/spring-kafka-service:{GIT_SHA}`

**Why:** Enables precise rollbacks, audit trails, and avoids "latest" ambiguity.

### 5. Makefile Automation
**Why:** Simplify complex Docker/AWS commands and ensure consistency.

**Key Commands:**
```bash
make build-image      # Build Docker images
make build-image-push # Push to ECR
make up               # Start services
make down             # Stop and cleanup
```

## 🔄 CI/CD Pipeline

**Triggers:** Push to `main` or any Pull Request

**Stages:**
1. **Build:** Multi-stage Docker build → Push to ECR with Git SHA
2. **Test:** Pull image → Spin up stack → Run tests → Cleanup

**Required Secrets:**
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_DEFAULT_REGION`

## ☁️ Infrastructure as Code (Terraform)

**Provisions:**
- ECR repository for images
- EKS for Deployment 
- VPC with security groups
- IAM roles 

**Usage:**
```bash
cd terraform/
terraform init
terraform apply
```

## 📁 Project Structure

```
├── .github/workflows/     # CI/CD pipelines
├── src/                   # Spring Boot source
├── terraform/             # IaC configurations
├── k8s/                   # Kubernetes manifests
├── Dockerfile             # Multi-stage build
├── docker-compose.yml     # Local orchestration
├── Makefile              # Build automation
└── pom.xml               # Maven dependencies
```
## 📝 Summary

This project demonstrates:
- ✅ Multi-stage Dockerfile (optimized builds)
- ✅ Docker Compose (local development)
- ✅ Kubernetes manifests (production deployment)
- ✅ GitHub Actions CI/CD (automated pipeline)
- ✅ Terraform IaC (AWS infrastructure)

<img width="811" height="610" alt="image" src="https://github.com/user-attachments/assets/fd89907b-fab6-4955-969a-9f9e20454a44" />

