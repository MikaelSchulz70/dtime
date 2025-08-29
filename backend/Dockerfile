# Backend Dockerfile
FROM openjdk:21-jdk-slim as build

# Install dependencies for build process
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy Maven files first (for better caching)
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw ./

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:21-jdk-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Create non-root user
RUN groupadd -r dtime && useradd -r -g dtime dtime

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Create logs directory and set ownership
RUN mkdir -p /app/logs && chown -R dtime:dtime /app

# Switch to non-root user
USER dtime

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Set JVM options for container environment
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]