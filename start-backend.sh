#!/bin/bash

# DTime Backend Startup Script
# This script sets up environment variables and starts the Spring Boot backend

set -e  # Exit on any error

echo "🚀 Starting DTime Backend..."

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the right directory
if [ ! -f "backend/pom.xml" ]; then
    if [ -f "pom.xml" ]; then
        print_status "Already in backend directory"
        BACKEND_DIR="."
    else
        print_error "Please run this script from the project root directory (where backend/ folder exists)"
        exit 1
    fi
else
    BACKEND_DIR="backend"
fi

# Check if database is running
print_status "Checking if database is running..."
if ! nc -z localhost 5432 2>/dev/null; then
    print_warning "Database not accessible on localhost:5432"
    echo "Starting database..."
    
    if [ -f "database/docker-compose.yml" ]; then
        cd database
        docker-compose up -d
        print_status "Waiting for database to be ready..."
        
        # Wait up to 60 seconds for database
        for i in {1..60}; do
            if nc -z localhost 5432 2>/dev/null; then
                print_success "Database is ready!"
                break
            fi
            echo -n "."
            sleep 1
        done
        
        if ! nc -z localhost 5432 2>/dev/null; then
            print_error "Database failed to start after 60 seconds"
            exit 1
        fi
        
        cd ..
    else
        print_error "Database docker-compose.yml not found. Please ensure database is running manually."
        exit 1
    fi
else
    print_success "Database is already running"
fi

# Load environment variables from .env.local if it exists
if [ -f ".env.local" ]; then
    print_status "Loading environment variables from .env.local"
    set -a  # Automatically export all variables
    source .env.local
    set +a
fi

# Set required environment variables with defaults
export DATABASE_URL=${DATABASE_URL:-"jdbc:postgresql://localhost:5432/dtime"}
export DATABASE_USERNAME=${DATABASE_USERNAME:-"dtime"}
export DATABASE_PASSWORD=${DATABASE_PASSWORD:-"dtime_dev_password"}

# Development-friendly defaults
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-"dev"}
export SECURITY_CSRF_ENABLED=${SECURITY_CSRF_ENABLED:-"false"}

# Email settings (use dummy values for development if not set)
export MAIL_USERNAME=${MAIL_USERNAME:-"dev@example.com"}
export MAIL_PASSWORD=${MAIL_PASSWORD:-"dummy_password"}
export MAIL_HOST=${MAIL_HOST:-"smtp.gmail.com"}
export MAIL_PORT=${MAIL_PORT:-"587"}
export MAIL_DEBUG=${MAIL_DEBUG:-"false"}

# Print configuration
echo ""
print_status "Backend Configuration:"
echo "  Database URL: $DATABASE_URL"
echo "  Database User: $DATABASE_USERNAME"
echo "  Database Password: [${#DATABASE_PASSWORD} chars]"
echo "  Spring Profile: $SPRING_PROFILES_ACTIVE"
echo "  CSRF Enabled: $SECURITY_CSRF_ENABLED"
echo "  Mail Host: $MAIL_HOST"
echo "  Mail Debug: $MAIL_DEBUG"
echo ""

# Check if Java is available
if ! command -v java &> /dev/null; then
    print_error "Java not found. Please install Java 21 or ensure it's in your PATH."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | sed '/^1\./s///' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    print_error "Java 21 or higher is required. Found Java $JAVA_VERSION"
    exit 1
fi

# Change to backend directory
cd $BACKEND_DIR

# Check if JAR file exists
JAR_FILE="target/dtime-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    print_status "JAR file not found. Building application..."
    
    # Check if Maven is available for building
    if ! command -v mvn &> /dev/null; then
        print_error "Maven not found and JAR file doesn't exist. Please install Maven to build the application or provide a pre-built JAR."
        exit 1
    fi
    
    # Build the application
    print_status "Running Maven build..."
    mvn clean package -DskipTests
    
    if [ ! -f "$JAR_FILE" ]; then
        print_error "Build failed. JAR file was not created."
        exit 1
    fi
    
    print_success "Build completed successfully!"
fi

# Option to rebuild
if [ "$1" = "--clean" ] || [ "$1" = "-c" ]; then
    print_status "Performing clean build..."
    if ! command -v mvn &> /dev/null; then
        print_error "Maven not found. Cannot perform clean build."
        exit 1
    fi
    mvn clean package -DskipTests
fi

# Option to run tests
if [ "$1" = "--test" ] || [ "$1" = "-t" ]; then
    print_status "Running tests..."
    if ! command -v mvn &> /dev/null; then
        print_error "Maven not found. Cannot run tests."
        exit 1
    fi
    mvn test
fi

# Start the Spring Boot application
print_status "Starting Spring Boot application..."
print_warning "Press Ctrl+C to stop the application"
echo ""

# Export environment variables for the Java process
export DATABASE_URL
export DATABASE_USERNAME  
export DATABASE_PASSWORD
export MAIL_USERNAME
export MAIL_PASSWORD
export MAIL_HOST
export MAIL_PORT
export MAIL_DEBUG
export SECURITY_CSRF_ENABLED
export SPRING_PROFILES_ACTIVE

# Start with development-friendly settings and detailed database logging
exec java \
    -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} \
    -Xdebug \
    -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 \
    -Dlogging.level.se.dtime=DEBUG \
    -Dlogging.level.com.zaxxer.hikari=DEBUG \
    -Dlogging.level.org.postgresql=DEBUG \
    -Dlogging.level.liquibase=DEBUG \
    -Dspring.datasource.hikari.connection-test-query="SELECT 1" \
    -jar "$JAR_FILE"