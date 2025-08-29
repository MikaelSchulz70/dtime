#!/bin/bash

# DTime Frontend Docker Build Script
# Builds frontend Docker images (both dev and production)

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
BUILD_PROD=true
BUILD_DEV=true
NO_CACHE=false
PUSH_IMAGES=false
TAG="latest"
REGISTRY=""

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --prod-only)
            BUILD_PROD=true
            BUILD_DEV=false
            shift
            ;;
        --dev-only)
            BUILD_PROD=false
            BUILD_DEV=true
            shift
            ;;
        --no-cache)
            NO_CACHE=true
            shift
            ;;
        --push)
            PUSH_IMAGES=true
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
            echo "Build Docker images for DTime frontend"
            echo ""
            echo "Options:"
            echo "  --prod-only         Build only production image"
            echo "  --dev-only          Build only development image"
            echo "  --no-cache          Build without using Docker cache"
            echo "  --push              Push images to registry after building"
            echo "  --tag, -t TAG       Tag for the images (default: latest)"
            echo "  --registry, -r REG  Registry prefix (e.g., myregistry.com/)"
            echo "  --help, -h          Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                                    # Build both prod and dev images"
            echo "  $0 --prod-only --no-cache            # Build only production without cache"
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
if [ ! -d "frontend" ] || [ ! -f "frontend/package.json" ]; then
    print_error "Please run this script from the project root directory (where frontend/ folder exists)"
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

print_status "🐳 Building DTime Frontend Docker Images"
print_status "Production: $BUILD_PROD"
print_status "Development: $BUILD_DEV"
print_status "Tag: $TAG"
print_status "Registry: ${REGISTRY:-<none>}"
echo ""

# Build Production Frontend Image
if [ "$BUILD_PROD" = true ]; then
    if [ -n "$REGISTRY" ]; then
        PROD_IMAGE_NAME="${REGISTRY}dtime-frontend:${TAG}"
    else
        PROD_IMAGE_NAME="dtime-frontend:${TAG}"
    fi
    
    print_status "Building production frontend image: $PROD_IMAGE_NAME"
    
    docker build $CACHE_OPTION \
        -t "$PROD_IMAGE_NAME" \
        -f frontend/Dockerfile \
        frontend/
    
    if [ $? -eq 0 ]; then
        PROD_SIZE=$(docker images --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep "$PROD_IMAGE_NAME" | awk '{print $2}')
        print_success "Production frontend image built: $PROD_IMAGE_NAME (${PROD_SIZE})"
        
        if [ "$PUSH_IMAGES" = true ]; then
            print_status "Pushing production image..."
            docker push "$PROD_IMAGE_NAME"
            print_success "Production image pushed: $PROD_IMAGE_NAME"
        fi
    else
        print_error "Production frontend build failed!"
        exit 1
    fi
    echo ""
fi

# Build Development Frontend Image
if [ "$BUILD_DEV" = true ]; then
    if [ -n "$REGISTRY" ]; then
        DEV_IMAGE_NAME="${REGISTRY}dtime-frontend-dev:${TAG}"
    else
        DEV_IMAGE_NAME="dtime-frontend-dev:${TAG}"
    fi
    
    print_status "Building development frontend image: $DEV_IMAGE_NAME"
    
    docker build $CACHE_OPTION \
        -t "$DEV_IMAGE_NAME" \
        -f frontend/Dockerfile.dev \
        frontend/
    
    if [ $? -eq 0 ]; then
        DEV_SIZE=$(docker images --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep "$DEV_IMAGE_NAME" | awk '{print $2}')
        print_success "Development frontend image built: $DEV_IMAGE_NAME (${DEV_SIZE})"
        
        if [ "$PUSH_IMAGES" = true ]; then
            print_status "Pushing development image..."
            docker push "$DEV_IMAGE_NAME"
            print_success "Development image pushed: $DEV_IMAGE_NAME"
        fi
    else
        print_error "Development frontend build failed!"
        exit 1
    fi
    echo ""
fi

print_success "✅ Frontend Docker build complete!"
echo ""
echo "Usage:"
if [ "$BUILD_PROD" = true ]; then
    echo "  # Run production frontend:"
    echo "  docker run -p 3000:80 --name dtime-frontend $PROD_IMAGE_NAME"
fi
if [ "$BUILD_DEV" = true ]; then
    echo "  # Run development frontend:"
    echo "  docker run -p 3000:3000 --name dtime-frontend-dev $DEV_IMAGE_NAME"
fi
echo ""
echo "  # Or use docker-compose:"
echo "  docker-compose --profile full-stack up -d      # Development"
echo "  docker-compose --profile production up -d       # Production"