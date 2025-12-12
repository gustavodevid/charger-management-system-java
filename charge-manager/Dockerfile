# Charge Manager Dockerfile
# Multi-stage build for optimized image size

# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Copy parent pom
COPY pom.xml ./pom.xml

# Copy module poms
COPY charge-manager/pom.xml ./charge-manager/pom.xml
COPY charge-proxy/pom.xml ./charge-proxy/pom.xml

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl charge-manager -am -B

# Copy source code
COPY charge-manager/src ./charge-manager/src

# Build the application
RUN mvn clean package -pl charge-manager -am -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built jar
COPY --from=build /app/charge-manager/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Expose ports (8080 for embedded server, 8081 for SOAP endpoint)
EXPOSE 8080 8081

# Health check via actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

