FROM gradle:8.5.0-jdk21-alpine AS builder

WORKDIR /app
COPY . .
RUN gradle build -x test

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache \
    curl \
    py3-pip \
    bash \
    docker-cli

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
