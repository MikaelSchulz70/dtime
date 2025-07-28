#!/bin/bash

# Simple Backend Startup Script
# Run from the backend directory

echo "🚀 Starting DTime Backend (from backend directory)..."

# Set development environment variables
export DATABASE_URL=${DATABASE_URL:-"jdbc:postgresql://localhost:5432/dtime"}
export DATABASE_USERNAME=${DATABASE_USERNAME:-"dtime"}
export DATABASE_PASSWORD=${DATABASE_PASSWORD:-"dtime_dev_password"}
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-"dev"}
export SECURITY_CSRF_ENABLED=${SECURITY_CSRF_ENABLED:-"false"}
export MAIL_USERNAME=${MAIL_USERNAME:-"dev@example.com"}
export MAIL_PASSWORD=${MAIL_PASSWORD:-"dummy_password"}

echo "Configuration:"
echo "  Database: $DATABASE_URL"
echo "  Profile: $SPRING_PROFILES_ACTIVE"
echo "  CSRF: $SECURITY_CSRF_ENABLED"
echo ""

# Check if database is accessible
if ! nc -z localhost 5432 2>/dev/null; then
    echo "⚠️  WARNING: Database not accessible on localhost:5432"
    echo "   Make sure to start the database first:"
    echo "   cd ../database && make start"
    echo ""
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 21 or ensure it's in your PATH."
    exit 1
fi

# Check if JAR file exists
JAR_FILE="target/dtime-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found at $JAR_FILE"
    echo "   Please build the application first:"
    echo "   mvn clean package -DskipTests"
    echo "   Or run: ../start-backend.sh (which will build automatically)"
    exit 1
fi

echo "Starting Spring Boot application..."
java -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar "$JAR_FILE"