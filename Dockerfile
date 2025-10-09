FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src/main ./src/main
RUN mvn clean package -DskipTests


FROM alpine:3.19
RUN apk add --no-cache openjdk21 bash \
    && addgroup -S appgroup && adduser -S appuser -G appgroup \
    && rm -rf /var/cache/apk/*
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
USER appuser
CMD ["java", "-jar", "app.jar"]

