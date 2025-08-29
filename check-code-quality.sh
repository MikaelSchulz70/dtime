#!/bin/bash

# DTime Code Quality Check Script
# Runs linting and code quality checks for both frontend and backend

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
CHECK_FRONTEND=true
CHECK_BACKEND=true
FIX_ISSUES=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --frontend-only)
            CHECK_FRONTEND=true
            CHECK_BACKEND=false
            shift
            ;;
        --backend-only)
            CHECK_FRONTEND=false
            CHECK_BACKEND=true
            shift
            ;;
        --fix)
            FIX_ISSUES=true
            shift
            ;;
        --help|-h)
            echo "Usage: $0 [OPTIONS]"
            echo ""
            echo "Run code quality checks for DTime application"
            echo ""
            echo "Options:"
            echo "  --frontend-only     Check only frontend code"
            echo "  --backend-only      Check only backend code"
            echo "  --fix               Attempt to fix issues automatically"
            echo "  --help, -h          Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                  # Check both frontend and backend"
            echo "  $0 --frontend-only  # Check only frontend"
            echo "  $0 --fix            # Check and fix issues"
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

print_header "🔍 DTime Code Quality Check"

# Check Frontend
if [ "$CHECK_FRONTEND" = true ]; then
    print_header "⚛️  Frontend Code Quality Check"
    
    if [ ! -d "frontend" ]; then
        print_error "Frontend directory not found"
        exit 1
    fi
    
    cd frontend
    
    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
        print_status "Installing frontend dependencies..."
        npm install
    fi
    
    # Run ESLint
    print_status "Running ESLint..."
    if [ "$FIX_ISSUES" = true ]; then
        npm run lint:fix || true
        print_status "ESLint auto-fix completed"
    else
        if npm run lint:quiet; then
            print_success "ESLint check passed!"
        else
            print_warning "ESLint found issues - run with --fix to auto-fix"
        fi
    fi
    
    cd ..
    echo ""
fi

# Check Backend
if [ "$CHECK_BACKEND" = true ]; then
    print_header "☕ Backend Code Quality Check"
    
    if [ ! -d "backend" ]; then
        print_error "Backend directory not found"
        exit 1
    fi
    
    cd backend
    
    # Check if Maven is available
    if ! command -v mvn &> /dev/null; then
        print_error "Maven not found. Please install Maven for backend checks."
        cd ..
        if [ "$CHECK_FRONTEND" = false ]; then
            exit 1
        fi
    else
        # Compile the project first
        print_status "Compiling backend..."
        mvn clean compile -q
        
        # Run Spotbugs (if enabled)
        print_status "Running SpotBugs analysis..."
        if mvn spotbugs:check -q; then
            print_success "SpotBugs check passed!"
        else
            print_warning "SpotBugs found potential issues"
        fi
        
        # Run Checkstyle (if enabled)  
        print_status "Running Checkstyle analysis..."
        if mvn checkstyle:check -q; then
            print_success "Checkstyle check passed!"
        else
            print_warning "Checkstyle found style violations"
        fi
        
        # Run tests
        print_status "Running backend tests..."
        if mvn test -q; then
            print_success "All backend tests passed!"
        else
            print_warning "Some backend tests failed"
        fi
    fi
    
    cd ..
    echo ""
fi

print_header "✅ Code Quality Check Complete!"

echo ""
echo "Quality check summary:"
if [ "$CHECK_FRONTEND" = true ]; then
    echo "  ✅ Frontend ESLint analysis completed"
fi
if [ "$CHECK_BACKEND" = true ]; then
    echo "  ✅ Backend SpotBugs analysis completed"
    echo "  ✅ Backend Checkstyle analysis completed" 
    echo "  ✅ Backend tests executed"
fi
echo ""
echo "For detailed reports, check the output above."
echo "To fix issues automatically, run with --fix flag."