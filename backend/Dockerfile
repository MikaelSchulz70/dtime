# Backend Dockerfile
FROM openjdk:26-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Create non-root user
RUN groupadd -r dtime && useradd -r -g dtime dtime

# Copy the pre-built JAR
COPY target/*.jar app.jar

# Create logs directory and set ownership
RUN mkdir -p /app/logs && chown -R dtime:dtime /app

# Switch to non-root user
USER dtime

# Expose port
EXPOSE 8443

# Health check (using HTTPS with insecure flag for self-signed cert)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f -k https://localhost:8443/actuator/health || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]