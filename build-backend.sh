#!/bin/bash

# DTime Backend Build Script
# Builds the Spring Boot application JAR file

set -e  # Exit on any error

echo "🔨 Building DTime Backend..."

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

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    print_error "Maven not found. Please install Maven or ensure it's in your PATH."
    exit 1
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    print_error "Java not found. Please install Java 21 or ensure it's in your PATH."
    exit 1
fi

# Change to backend directory
cd $BACKEND_DIR

# Clean build option
CLEAN_BUILD=false
RUN_TESTS=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --clean|-c)
            CLEAN_BUILD=true
            shift
            ;;
        --test|-t)
            RUN_TESTS=true
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --clean, -c     Perform clean build"
            echo "  --test, -t      Run tests before building"
            echo "  --help, -h      Show this help message"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Run tests if requested
if [ "$RUN_TESTS" = true ]; then
    print_status "Running tests..."
    mvn test
    print_success "Tests completed successfully!"
fi

# Build the application
if [ "$CLEAN_BUILD" = true ]; then
    print_status "Performing clean build..."
    mvn clean package -DskipTests
else
    print_status "Building application..."
    mvn package -DskipTests
fi

# Check if build was successful
JAR_FILE="target/dtime-1.0.0.jar"
if [ -f "$JAR_FILE" ]; then
    JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
    print_success "Build completed successfully!"
    print_status "JAR file: $JAR_FILE (${JAR_SIZE})"
    echo ""
    echo "To run the application:"
    echo "  java -jar $JAR_FILE"
    echo "  Or use: ./start-backend.sh"
else
    print_error "Build failed. JAR file was not created."
    exit 1
fi