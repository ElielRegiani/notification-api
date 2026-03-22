# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy as builder

WORKDIR /app

# Copy gradle files
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

# Build the application
RUN chmod +x ./gradlew && ./gradlew clean build -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the jar from builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run application with production settings
ENTRYPOINT ["java", "-jar", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75", "app.jar"]
