# 1. Build stage
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build


WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline


COPY src/main ./src/main
COPY src/test ./src/test

RUN mvn clean package -DskipTests


# 2. Runtime stage
FROM alpine:latest

# Install JRE + dockerize
ENV JAVA_HOME=/usr/lib/jvm/java-21-openjdk
ENV PATH="$JAVA_HOME/bin:${PATH}"
ENV DOCKERIZE_VERSION=v0.9.3

RUN apk update --no-cache \
    && apk add --no-cache openjdk21 wget bash \
    && wget -O - https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
       | tar xzf - -C /usr/local/bin \
    && apk del wget

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]