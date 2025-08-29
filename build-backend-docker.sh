#!/bin/bash

# DTime Backend Docker Build Script
# Builds only the backend Docker image

set -e  # Exit on any error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Default values
NO_CACHE=false
PUSH_IMAGE=false
TAG="latest"
REGISTRY=""

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --no-cache)
            NO_CACHE=true
            shift
            ;;
        --push)
            PUSH_IMAGE=true
            shift
            ;;
        --tag|-t)
            TAG="$2"
            shift 2
            ;;
        --registry|-r)
            REGISTRY="$2"
            shift 2
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Build Docker image for DTime backend"
            echo ""
            echo "Options:"
            echo "  --no-cache          Build without using Docker cache"
            echo "  --push              Push image to registry after building"
            echo "  --tag, -t TAG       Tag for the image (default: latest)"
            echo "  --registry, -r REG  Registry prefix (e.g., myregistry.com/)"
            echo "  --help, -h          Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                                    # Build backend image"
            echo "  $0 --no-cache                        # Build without cache"
            echo "  $0 --tag v1.0.0 --push               # Build, tag as v1.0.0 and push"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Check if we're in the right directory
if [ ! -d "backend" ] || [ ! -f "backend/pom.xml" ]; then
    print_error "Please run this script from the project root directory (where backend/ folder exists)"
    exit 1
fi

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    print_error "Docker not found. Please install Docker."
    exit 1
fi

# Build cache options
CACHE_OPTION=""
if [ "$NO_CACHE" = true ]; then
    CACHE_OPTION="--no-cache"
    print_status "Building without cache - this may take longer"
fi

# Image naming
if [ -n "$REGISTRY" ]; then
    IMAGE_NAME="${REGISTRY}dtime-backend:${TAG}"
else
    IMAGE_NAME="dtime-backend:${TAG}"
fi

print_status "🐳 Building DTime Backend Docker Image"
print_status "Image: $IMAGE_NAME"
print_status "Cache: $([ "$NO_CACHE" = true ] && echo "disabled" || echo "enabled")"
echo ""

# Build the image
print_status "Building backend Docker image..."
docker build $CACHE_OPTION \
    -t "$IMAGE_NAME" \
    -f backend/Dockerfile \
    backend/

# Check if build was successful
if [ $? -eq 0 ]; then
    # Get image size
    IMAGE_SIZE=$(docker images --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep "$IMAGE_NAME" | awk '{print $2}')
    print_success "Backend Docker image built successfully!"
    print_status "Image: $IMAGE_NAME (${IMAGE_SIZE})"
    
    # Push if requested
    if [ "$PUSH_IMAGE" = true ]; then
        print_status "Pushing backend image to registry..."
        docker push "$IMAGE_NAME"
        print_success "Backend image pushed: $IMAGE_NAME"
    fi
    
    echo ""
    echo "Usage:"
    echo "  # Run backend container:"
    echo "  docker run -p 8080:8080 --name dtime-backend $IMAGE_NAME"
    echo ""
    echo "  # Or use docker-compose:"
    echo "  docker-compose --profile full-stack up -d"
else
    print_error "Backend Docker image build failed!"
    exit 1
fi