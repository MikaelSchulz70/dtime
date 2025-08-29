#!/bin/bash

# DTime Package Script
# Packages the entire application for distribution

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
VERSION="latest"
OUTPUT_DIR="./dist"
INCLUDE_IMAGES=true
INCLUDE_SOURCE=true
CREATE_ARCHIVE=true
REGISTRY=""

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --version|-v)
            VERSION="$2"
            shift 2
            ;;
        --output|-o)
            OUTPUT_DIR="$2"
            shift 2
            ;;
        --no-images)
            INCLUDE_IMAGES=false
            shift
            ;;
        --no-source)
            INCLUDE_SOURCE=false
            shift
            ;;
        --no-archive)
            CREATE_ARCHIVE=false
            shift
            ;;
        --registry|-r)
            REGISTRY="$2"
            shift 2
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Package DTime application for distribution"
            echo ""
            echo "Options:"
            echo "  --version, -v VER   Version for the package [default: latest]"
            echo "  --output, -o DIR    Output directory [default: ./dist]"
            echo "  --no-images         Don't export Docker images"
            echo "  --no-source         Don't include source code"
            echo "  --no-archive        Don't create tar.gz archive"
            echo "  --registry, -r REG  Registry prefix for images"
            echo "  --help, -h          Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                                    # Package everything"
            echo "  $0 --version v1.0.0 --no-source      # Package only Docker images"
            echo "  $0 --output ./release                 # Custom output directory"
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
    print_error "Please run this script from the project root directory"
    exit 1
fi

# Check required tools
if ! command -v docker &> /dev/null; then
    print_error "Docker not found. Please install Docker."
    exit 1
fi

if [ "$CREATE_ARCHIVE" = true ] && ! command -v tar &> /dev/null; then
    print_error "tar not found. Please install tar or use --no-archive."
    exit 1
fi

print_header "📦 DTime Application Packaging"
print_status "Version: $VERSION"
print_status "Output directory: $OUTPUT_DIR"
print_status "Include Docker images: $INCLUDE_IMAGES"
print_status "Include source code: $INCLUDE_SOURCE"
print_status "Create archive: $CREATE_ARCHIVE"
print_status "Registry: ${REGISTRY:-<local>}"
echo ""

# Create output directory
PACKAGE_DIR="$OUTPUT_DIR/dtime-$VERSION"
rm -rf "$PACKAGE_DIR"
mkdir -p "$PACKAGE_DIR"

print_status "Created package directory: $PACKAGE_DIR"

# Include source code and configuration
if [ "$INCLUDE_SOURCE" = true ]; then
    print_header "📄 Including Source Code and Configuration"
    
    # Copy essential files
    cp docker-compose.yml "$PACKAGE_DIR/"
    cp .env.example "$PACKAGE_DIR/"
    cp README.md "$PACKAGE_DIR/" 2>/dev/null || true
    cp DOCKER.md "$PACKAGE_DIR/" 2>/dev/null || true
    
    # Copy scripts
    mkdir -p "$PACKAGE_DIR/scripts"
    cp build-docker.sh "$PACKAGE_DIR/scripts/"
    cp build-backend-docker.sh "$PACKAGE_DIR/scripts/"
    cp build-frontend-docker.sh "$PACKAGE_DIR/scripts/"
    cp deploy.sh "$PACKAGE_DIR/scripts/"
    chmod +x "$PACKAGE_DIR/scripts/"*.sh
    
    # Copy database scripts
    if [ -d "database/scripts" ]; then
        cp -r database/scripts "$PACKAGE_DIR/database-scripts"
    fi
    
    # Copy source code (optional - for rebuilding)
    if [ -d "backend" ]; then
        print_status "Copying backend source..."
        cp -r backend "$PACKAGE_DIR/"
        # Remove target directory if it exists
        rm -rf "$PACKAGE_DIR/backend/target"
    fi
    
    if [ -d "frontend" ]; then
        print_status "Copying frontend source..."
        cp -r frontend "$PACKAGE_DIR/"
        # Remove build directories
        rm -rf "$PACKAGE_DIR/frontend/node_modules"
        rm -rf "$PACKAGE_DIR/frontend/dist"
    fi
    
    print_success "Source code and configuration included"
    echo ""
fi

# Export Docker images
if [ "$INCLUDE_IMAGES" = true ]; then
    print_header "🐳 Exporting Docker Images"
    
    mkdir -p "$PACKAGE_DIR/images"
    
    # Define image names
    if [ -n "$REGISTRY" ]; then
        BACKEND_IMAGE="${REGISTRY}dtime-backend:$VERSION"
        FRONTEND_IMAGE="${REGISTRY}dtime-frontend:$VERSION"
        FRONTEND_DEV_IMAGE="${REGISTRY}dtime-frontend-dev:$VERSION"
        DB_IMAGE="${REGISTRY}dtime-postgres:$VERSION"
    else
        BACKEND_IMAGE="dtime-backend:$VERSION"
        FRONTEND_IMAGE="dtime-frontend:$VERSION"
        FRONTEND_DEV_IMAGE="dtime-frontend-dev:$VERSION"
        DB_IMAGE="dtime-postgres:$VERSION"
    fi
    
    # Check if images exist and export them
    if docker image inspect "$BACKEND_IMAGE" >/dev/null 2>&1; then
        print_status "Exporting backend image..."
        docker save "$BACKEND_IMAGE" | gzip > "$PACKAGE_DIR/images/dtime-backend-$VERSION.tar.gz"
        print_success "Backend image exported"
    else
        print_warning "Backend image $BACKEND_IMAGE not found, skipping"
    fi
    
    if docker image inspect "$FRONTEND_IMAGE" >/dev/null 2>&1; then
        print_status "Exporting frontend production image..."
        docker save "$FRONTEND_IMAGE" | gzip > "$PACKAGE_DIR/images/dtime-frontend-$VERSION.tar.gz"
        print_success "Frontend production image exported"
    else
        print_warning "Frontend image $FRONTEND_IMAGE not found, skipping"
    fi
    
    if docker image inspect "$FRONTEND_DEV_IMAGE" >/dev/null 2>&1; then
        print_status "Exporting frontend development image..."
        docker save "$FRONTEND_DEV_IMAGE" | gzip > "$PACKAGE_DIR/images/dtime-frontend-dev-$VERSION.tar.gz"
        print_success "Frontend development image exported"
    else
        print_warning "Frontend dev image $FRONTEND_DEV_IMAGE not found, skipping"
    fi
    
    if docker image inspect "$DB_IMAGE" >/dev/null 2>&1; then
        print_status "Exporting database image..."
        docker save "$DB_IMAGE" | gzip > "$PACKAGE_DIR/images/dtime-postgres-$VERSION.tar.gz"
        print_success "Database image exported"
    else
        print_warning "Database image $DB_IMAGE not found, skipping"
    fi
    
    echo ""
fi

# Create load script for images
cat > "$PACKAGE_DIR/load-images.sh" << 'EOF'
#!/bin/bash

echo "Loading DTime Docker images..."

if [ -d "images" ]; then
    for image_file in images/*.tar.gz; do
        if [ -f "$image_file" ]; then
            echo "Loading $(basename "$image_file")..."
            docker load < "$image_file"
        fi
    done
    echo "All images loaded successfully!"
else
    echo "No images directory found."
fi
EOF

chmod +x "$PACKAGE_DIR/load-images.sh"

# Create deployment instructions
cat > "$PACKAGE_DIR/DEPLOYMENT.md" << EOF
# DTime Deployment Guide

This package contains the DTime application v$VERSION ready for deployment.

## Quick Start

1. **Load Docker images** (if included):
   \`\`\`bash
   ./load-images.sh
   \`\`\`

2. **Configure environment**:
   \`\`\`bash
   cp .env.example .env
   # Edit .env with your configuration
   \`\`\`

3. **Deploy**:
   \`\`\`bash
   # Production deployment
   ./scripts/deploy.sh --env production

   # Development deployment  
   ./scripts/deploy.sh --env development
   \`\`\`

## Package Contents

- \`docker-compose.yml\` - Docker Compose configuration
- \`.env.example\` - Environment variables template
- \`scripts/\` - Build and deployment scripts
- \`images/\` - Pre-built Docker images (if included)
- \`backend/\` - Backend source code (if included)
- \`frontend/\` - Frontend source code (if included)
- \`database-scripts/\` - Database initialization scripts

## Manual Deployment

1. Load images: \`./load-images.sh\`
2. Configure: \`cp .env.example .env && vim .env\`
3. Start: \`docker-compose --profile production up -d\`

## Accessing the Application

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Database: localhost:5432

For more details, see DOCKER.md
EOF

# Create archive
if [ "$CREATE_ARCHIVE" = true ]; then
    print_header "🗜️  Creating Distribution Archive"
    
    ARCHIVE_NAME="dtime-$VERSION.tar.gz"
    ARCHIVE_PATH="$OUTPUT_DIR/$ARCHIVE_NAME"
    
    print_status "Creating archive: $ARCHIVE_NAME"
    
    # Create tar.gz archive
    tar -czf "$ARCHIVE_PATH" -C "$OUTPUT_DIR" "dtime-$VERSION"
    
    # Get archive size
    ARCHIVE_SIZE=$(du -h "$ARCHIVE_PATH" | cut -f1)
    
    print_success "Archive created: $ARCHIVE_PATH ($ARCHIVE_SIZE)"
    echo ""
fi

# Display package summary
print_header "📋 Package Summary"

PACKAGE_SIZE=$(du -sh "$PACKAGE_DIR" | cut -f1)
print_status "Package directory: $PACKAGE_DIR ($PACKAGE_SIZE)"

if [ "$CREATE_ARCHIVE" = true ]; then
    ARCHIVE_SIZE=$(du -h "$ARCHIVE_PATH" | cut -f1)
    print_status "Archive: $ARCHIVE_PATH ($ARCHIVE_SIZE)"
fi

echo ""
print_status "Package contents:"
find "$PACKAGE_DIR" -type f | head -20 | while read -r file; do
    echo "  - ${file#$PACKAGE_DIR/}"
done

if [ "$(find "$PACKAGE_DIR" -type f | wc -l)" -gt 20 ]; then
    echo "  ... and $(($(find "$PACKAGE_DIR" -type f | wc -l) - 20)) more files"
fi

echo ""
print_header "✅ Packaging Complete!"

echo ""
echo "To deploy this package on another system:"
echo "  1. Extract: tar -xzf $ARCHIVE_NAME"
echo "  2. Configure: cd dtime-$VERSION && cp .env.example .env"
echo "  3. Deploy: ./scripts/deploy.sh"
echo ""

print_success "Package ready for distribution!"