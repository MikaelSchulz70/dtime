#!/bin/bash

# DTime Deployment Script
# Builds, packages, and deploys the DTime application

set -e  # Exit on any error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m'

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
ENVIRONMENT="production"
BUILD_IMAGES=true
START_SERVICES=true
STOP_EXISTING=true
REGISTRY=""
TAG="latest"
BACKUP_DATABASE=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --env|-e)
            ENVIRONMENT="$2"
            shift 2
            ;;
        --no-build)
            BUILD_IMAGES=false
            shift
            ;;
        --no-start)
            START_SERVICES=false
            shift
            ;;
        --no-stop)
            STOP_EXISTING=false
            shift
            ;;
        --backup-db)
            BACKUP_DATABASE=true
            shift
            ;;
        --registry|-r)
            REGISTRY="$2"
            shift 2
            ;;
        --tag|-t)
            TAG="$2"
            shift 2
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Deploy DTime application with Docker"
            echo ""
            echo "Options:"
            echo "  --env, -e ENV       Deployment environment (development|production) [default: production]"
            echo "  --no-build          Skip building Docker images"
            echo "  --no-start          Skip starting services"
            echo "  --no-stop           Don't stop existing services"
            echo "  --backup-db         Backup database before deployment"
            echo "  --registry, -r REG  Registry prefix for images"
            echo "  --tag, -t TAG       Image tag to deploy [default: latest]"
            echo "  --help, -h          Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                                    # Production deployment"
            echo "  $0 --env development                  # Development deployment"
            echo "  $0 --no-build --tag v1.0.0           # Deploy existing v1.0.0 images"
            echo "  $0 --backup-db --registry myregistry.com/"
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Validate environment
if [[ "$ENVIRONMENT" != "development" && "$ENVIRONMENT" != "production" ]]; then
    print_error "Environment must be 'development' or 'production'"
    exit 1
fi

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

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose not found. Please install Docker Compose."
    exit 1
fi

print_header "🚀 DTime Application Deployment"
print_status "Environment: $ENVIRONMENT"
print_status "Build images: $BUILD_IMAGES"
print_status "Start services: $START_SERVICES"
print_status "Registry: ${REGISTRY:-<local>}"
print_status "Tag: $TAG"
echo ""

# Check for .env file
if [ ! -f ".env" ]; then
    print_warning ".env file not found. Creating from template..."
    if [ -f ".env.example" ]; then
        cp .env.example .env
        print_warning "Please edit .env file with your configuration before continuing"
        print_status "Press Enter to continue after editing .env file..."
        read -r
    else
        print_error ".env.example file not found. Please create .env file manually."
        exit 1
    fi
fi

# Database backup
if [ "$BACKUP_DATABASE" = true ]; then
    print_header "💾 Database Backup"
    
    # Check if database container is running
    if docker-compose ps | grep -q "dtime-postgres.*Up"; then
        print_status "Creating database backup..."
        BACKUP_DIR="./database/backups"
        mkdir -p "$BACKUP_DIR"
        BACKUP_FILE="$BACKUP_DIR/backup_$(date +%Y%m%d_%H%M%S).sql"
        
        docker-compose exec -T dtime-db pg_dump -U dtime dtime > "$BACKUP_FILE"
        print_success "Database backup created: $BACKUP_FILE"
    else
        print_warning "Database container not running, skipping backup"
    fi
    echo ""
fi

# Stop existing services
if [ "$STOP_EXISTING" = true ]; then
    print_header "🛑 Stopping Existing Services"
    
    if [ "$ENVIRONMENT" = "development" ]; then
        docker-compose --profile full-stack down
    else
        docker-compose --profile production down
    fi
    
    print_success "Existing services stopped"
    echo ""
fi

# Build images
if [ "$BUILD_IMAGES" = true ]; then
    print_header "🔨 Building Docker Images"
    
    BUILD_ARGS=""
    if [ -n "$REGISTRY" ]; then
        BUILD_ARGS="$BUILD_ARGS --registry $REGISTRY"
    fi
    if [ "$TAG" != "latest" ]; then
        BUILD_ARGS="$BUILD_ARGS --tag $TAG"
    fi
    
    ./build-docker.sh $BUILD_ARGS
    
    print_success "Docker images built successfully"
    echo ""
fi

# Start services
if [ "$START_SERVICES" = true ]; then
    print_header "🚀 Starting Services"
    
    # Set environment-specific backend URL if not already configured
    if [ -z "${FRONTEND_BACKEND_URL}" ]; then
        if [ "$ENVIRONMENT" = "development" ]; then
            export FRONTEND_BACKEND_URL="https://localhost:8443"
            print_status "Auto-configured backend URL for local development: $FRONTEND_BACKEND_URL"
        else
            export FRONTEND_BACKEND_URL="https://localhost:8443"
            print_warning "Using localhost for production - set FRONTEND_BACKEND_URL in .env for production domain"
        fi
    else
        print_status "Using configured backend URL: $FRONTEND_BACKEND_URL"
    fi
    
    if [ "$ENVIRONMENT" = "development" ]; then
        print_status "Starting development environment..."
        docker-compose --profile full-stack up -d
    else
        print_status "Starting production environment..."
        docker-compose --profile production up -d
    fi
    
    print_success "Services started successfully"
    
    # Wait for services to be healthy
    print_status "Waiting for services to be ready..."
    sleep 10
    
    # Check service status
    print_status "Service status:"
    docker-compose ps
    
    echo ""
    print_header "🎉 Deployment Complete!"
    
    if [ "$ENVIRONMENT" = "development" ]; then
        echo "Development environment is ready:"
        echo "  • Frontend (Dev): http://localhost:3000"
        echo "  • Backend API:    http://localhost:8080"
        echo "  • Database:       localhost:5432"
    else
        echo "Production environment is ready:"
        echo "  • Frontend:       http://localhost:3000"
        echo "  • Backend API:    http://localhost:8080"
        echo "  • Database:       localhost:5432"
    fi
    
    echo ""
    echo "Useful commands:"
    echo "  docker-compose logs -f              # View all logs"
    echo "  docker-compose ps                   # Check service status"
    echo "  docker-compose restart <service>    # Restart a service"
    echo "  docker-compose down                 # Stop all services"
fi

print_success "Deployment script completed successfully!"