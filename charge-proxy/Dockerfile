# Charge Proxy Dockerfile
# Multi-stage build with Tomcat 10.1 (Jakarta EE 10)

# Stage 1: Build
FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app

# Copy parent pom
COPY pom.xml ./pom.xml

# Copy module poms
COPY charge-manager/pom.xml ./charge-manager/pom.xml
COPY charge-proxy/pom.xml ./charge-proxy/pom.xml

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -pl charge-proxy -am -B

# Copy source code
COPY charge-proxy/src ./charge-proxy/src

# Build the WAR
RUN mvn clean package -pl charge-proxy -am -DskipTests -B

# Stage 2: Runtime with Tomcat
FROM tomcat:10.1-jdk17-temurin

# Install curl for health checks
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file
COPY --from=build /app/charge-proxy/target/charge-proxy.war /usr/local/tomcat/webapps/ROOT.war

# Create logs directory
RUN mkdir -p /usr/local/tomcat/logs

# Expose port 8080 (Tomcat default)
EXPOSE 8080

# Health check via health servlet
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Run Tomcat
CMD ["catalina.sh", "run"]
