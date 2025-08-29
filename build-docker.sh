#!/bin/bash

# DTime Docker Build Script
# Builds all Docker images for the DTime application

set -e  # Exit on any error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
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

print_header() {
    echo -e "${PURPLE}========================================${NC}"
    echo -e "${PURPLE}$1${NC}"
    echo -e "${PURPLE}========================================${NC}"
}

# Default values
BUILD_BACKEND=true
BUILD_FRONTEND=true
BUILD_DATABASE=true
NO_CACHE=false
PUSH_IMAGES=false
TAG="latest"
REGISTRY=""

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --backend-only)
            BUILD_BACKEND=true
            BUILD_FRONTEND=false
            BUILD_DATABASE=false
            shift
            ;;
        --frontend-only)
            BUILD_BACKEND=false
            BUILD_FRONTEND=true
            BUILD_DATABASE=false
            shift
            ;;
        --database-only)
            BUILD_BACKEND=false
            BUILD_FRONTEND=false
            BUILD_DATABASE=true
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
            echo "Build Docker images for DTime application"
            echo ""
            echo "Options:"
            echo "  --backend-only      Build only backend image"
            echo "  --frontend-only     Build only frontend image"
            echo "  --database-only     Build only database image"
            echo "  --no-cache          Build without using Docker cache"
            echo "  --push              Push images to registry after building"
            echo "  --tag, -t TAG       Tag for the images (default: latest)"
            echo "  --registry, -r REG  Registry prefix (e.g., myregistry.com/)"
            echo "  --help, -h          Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                                    # Build all images"
            echo "  $0 --backend-only --no-cache         # Build only backend without cache"
            echo "  $0 --tag v1.0.0 --push               # Build all, tag as v1.0.0 and push"
            echo "  $0 --registry myregistry.com/ --push # Build and push to custom registry"
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
if [ ! -f "docker-compose.yml" ]; then
    print_error "Please run this script from the project root directory (where docker-compose.yml exists)"
    exit 1
fi

# Check if Docker is available
if ! command -v docker &> /dev/null; then
    print_error "Docker not found. Please install Docker."
    exit 1
fi

# Check if Docker daemon is running
if ! docker info >/dev/null 2>&1; then
    print_error "Docker daemon is not running. Please start Docker."
    exit 1
fi

# Build cache options
CACHE_OPTION=""
if [ "$NO_CACHE" = true ]; then
    CACHE_OPTION="--no-cache"
    print_warning "Building without cache - this may take longer"
fi

# Image naming function
get_image_name() {
    local service_name="$1"
    if [ -n "$REGISTRY" ]; then
        echo "${REGISTRY}dtime-${service_name}:${TAG}"
    else
        echo "dtime-${service_name}:${TAG}"
    fi
}

print_header "🐳 DTime Docker Image Build"
print_status "Build configuration:"
print_status "  Backend:   $BUILD_BACKEND"
print_status "  Frontend:  $BUILD_FRONTEND"
print_status "  Database:  $BUILD_DATABASE"
print_status "  Tag:       $TAG"
print_status "  Registry:  ${REGISTRY:-<none>}"
print_status "  Push:      $PUSH_IMAGES"
print_status "  No cache:  $NO_CACHE"
echo ""

# Build Database Image
if [ "$BUILD_DATABASE" = true ]; then
    print_header "🗄️  Building Database Image"
    
    DB_IMAGE=$(get_image_name "postgres")
    print_status "Building database image: $DB_IMAGE"
    
    docker build $CACHE_OPTION \
        -t "$DB_IMAGE" \
        -f database/Dockerfile \
        database/
    
    print_success "Database image built successfully: $DB_IMAGE"
    
    if [ "$PUSH_IMAGES" = true ]; then
        print_status "Pushing database image..."
        docker push "$DB_IMAGE"
        print_success "Database image pushed: $DB_IMAGE"
    fi
    echo ""
fi

# Build Backend Image
if [ "$BUILD_BACKEND" = true ]; then
    print_header "☕ Building Backend Image"
    
    BACKEND_IMAGE=$(get_image_name "backend")
    print_status "Building backend image: $BACKEND_IMAGE"
    
    docker build $CACHE_OPTION \
        -t "$BACKEND_IMAGE" \
        -f backend/Dockerfile \
        backend/
    
    print_success "Backend image built successfully: $BACKEND_IMAGE"
    
    if [ "$PUSH_IMAGES" = true ]; then
        print_status "Pushing backend image..."
        docker push "$BACKEND_IMAGE"
        print_success "Backend image pushed: $BACKEND_IMAGE"
    fi
    echo ""
fi

# Build Frontend Images (both dev and prod)
if [ "$BUILD_FRONTEND" = true ]; then
    print_header "⚛️  Building Frontend Images"
    
    # Production Frontend
    FRONTEND_PROD_IMAGE=$(get_image_name "frontend")
    print_status "Building production frontend image: $FRONTEND_PROD_IMAGE"
    
    docker build $CACHE_OPTION \
        -t "$FRONTEND_PROD_IMAGE" \
        -f frontend/Dockerfile \
        frontend/
    
    print_success "Production frontend image built successfully: $FRONTEND_PROD_IMAGE"
    
    # Development Frontend
    FRONTEND_DEV_IMAGE=$(get_image_name "frontend-dev")
    print_status "Building development frontend image: $FRONTEND_DEV_IMAGE"
    
    docker build $CACHE_OPTION \
        -t "$FRONTEND_DEV_IMAGE" \
        -f frontend/Dockerfile.dev \
        frontend/
    
    print_success "Development frontend image built successfully: $FRONTEND_DEV_IMAGE"
    
    if [ "$PUSH_IMAGES" = true ]; then
        print_status "Pushing frontend images..."
        docker push "$FRONTEND_PROD_IMAGE"
        docker push "$FRONTEND_DEV_IMAGE"
        print_success "Frontend images pushed"
    fi
    echo ""
fi

# Display build summary
print_header "📊 Build Summary"

if [ "$BUILD_DATABASE" = true ]; then
    DB_IMAGE=$(get_image_name "postgres")
    DB_SIZE=$(docker images --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep "$DB_IMAGE" | awk '{print $2}' || echo "N/A")
    print_status "Database:     $DB_IMAGE ($DB_SIZE)"
fi

if [ "$BUILD_BACKEND" = true ]; then
    BACKEND_IMAGE=$(get_image_name "backend")
    BACKEND_SIZE=$(docker images --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep "$BACKEND_IMAGE" | awk '{print $2}' || echo "N/A")
    print_status "Backend:      $BACKEND_IMAGE ($BACKEND_SIZE)"
fi

if [ "$BUILD_FRONTEND" = true ]; then
    FRONTEND_PROD_IMAGE=$(get_image_name "frontend")
    FRONTEND_DEV_IMAGE=$(get_image_name "frontend-dev")
    FRONTEND_PROD_SIZE=$(docker images --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep "$FRONTEND_PROD_IMAGE" | awk '{print $2}' || echo "N/A")
    FRONTEND_DEV_SIZE=$(docker images --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}" | grep "$FRONTEND_DEV_IMAGE" | awk '{print $2}' || echo "N/A")
    print_status "Frontend:     $FRONTEND_PROD_IMAGE ($FRONTEND_PROD_SIZE)"
    print_status "Frontend Dev: $FRONTEND_DEV_IMAGE ($FRONTEND_DEV_SIZE)"
fi

echo ""
print_header "🎉 Docker Build Complete!"

echo ""
echo "Next steps:"
echo "  # Start development stack:"
echo "  docker-compose --profile full-stack up -d"
echo ""
echo "  # Start production stack:"
echo "  docker-compose --profile production up -d"
echo ""
echo "  # View running containers:"
echo "  docker-compose ps"
echo ""

if [ "$PUSH_IMAGES" = true ]; then
    echo "  # Images have been pushed to registry and are ready for deployment!"
else
    echo "  # To push images to registry, run with --push flag"
fi