AWS_ACCOUNT_ID := 742460038063
AWS_DEFAULT_REGION := eu-west-3
AWS_ECR_DOMAIN := $(AWS_ACCOUNT_ID).dkr.ecr.$(AWS_DEFAULT_REGION).amazonaws.com
GIT_SHA := $(shell git rev-parse HEAD)
BUILD_IMAGE := $(AWS_ECR_DOMAIN)/staging
BUILD_TAG := $(if $(BUILD_TAG),$(BUILD_TAG),latest)



.DEFAULT_GOAL := build


test:
	mvn test



build:
	mvn clean package -DskipTests




build-image:
	docker buildx build --platform "linux/amd64" --tag "$(BUILD_IMAGE):$(GIT_SHA)-build" --target "build" .
	docker buildx build --cache-from "$(BUILD_IMAGE):$(GIT_SHA)-build" --platform "linux/amd64" --tag "$(BUILD_IMAGE):$(GIT_SHA)" .

build-image-login:
	aws ecr get-login-password --region $(AWS_DEFAULT_REGION) | docker login --username AWS --password-stdin $(AWS_ECR_DOMAIN)

build-image-push: build-image-login
	docker image push $(BUILD_IMAGE):$(GIT_SHA)

build-image-pull: build-image-login
	docker image pull $(BUILD_IMAGE):$(GIT_SHA)

output-image:
	@echo "$(BUILD_IMAGE):$(GIT_SHA)"


deploy:
	$env:AWS_ACCOUNT_ID=$(AWS_ACCOUNT_ID); \
	$env:AWS_DEFAULT_REGION=$(AWS_DEFAULT_REGION); \
	$env:AWS_ECR_DOMAIN=$(AWS_ECR_DOMAIN); \
	.\deploy.ps1




down:
	docker compose down --remove-orphans --volumes

up: down
	docker compose up --detach