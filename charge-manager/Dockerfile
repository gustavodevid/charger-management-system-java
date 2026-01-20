# Charge Manager Dockerfile
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
RUN mvn dependency:go-offline -pl charge-manager -am -B

# Copy source code
COPY charge-manager/src ./charge-manager/src

# Build the WAR
RUN mvn clean package -pl charge-manager -am -DskipTests -B

# Stage 2: Runtime with Tomcat
FROM tomcat:10.1-jdk17-temurin

# Remove default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file
COPY --from=build /app/charge-manager/target/charge-manager.war /usr/local/tomcat/webapps/ROOT.war

# Create logs directory
RUN mkdir -p /usr/local/tomcat/logs

# Expose port 8080 (Tomcat default)
EXPOSE 8080

# Health check via health servlet
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Run Tomcat
CMD ["catalina.sh", "run"]
