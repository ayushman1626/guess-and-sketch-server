# =========================================
# Stage 1: Build the application
# =========================================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Cache dependencies first (layers are cached if pom.xml doesn't change)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# =========================================
# Stage 2: Runtime image (minimal)
# =========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR
COPY --from=builder /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# JVM tuning for t2.micro (1 GB RAM)
# -Xmx512m caps heap to leave room for OS + metaspace
# -XX:+UseSerialGC is better than G1 for single-core free tier instances
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
