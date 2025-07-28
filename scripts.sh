#!/bin/bash

# DTime Development Scripts Helper

echo "🔧 DTime Development Scripts"
echo "=========================="
echo ""

echo "📊 Database Management:"
echo "  cd database && make start     # Start PostgreSQL database"
echo "  cd database && make stop      # Stop PostgreSQL database"
echo "  cd database && make health    # Check database health"
echo "  cd database && make shell     # Connect to database"
echo "  cd database && make logs      # View database logs"
echo "  cd database && make help      # See all database commands"
echo ""

echo "🚀 Backend (Spring Boot):"
echo "  ./start-backend.sh            # Start backend (recommended)"
echo "  ./start-backend.sh --clean    # Start backend with clean build"
echo "  ./start-backend.sh --test     # Start backend after running tests"
echo "  cd backend && ./start.sh      # Simple start from backend directory"
echo ""

echo "⚛️  Frontend (React):"
echo "  cd frontend && npm start      # Start development server"
echo "  cd frontend && npm run build  # Build for production"
echo "  ./build-frontend.sh           # Build and copy to backend"
echo ""

echo "🏗️  Build Scripts:"
echo "  ./build-frontend.sh           # Build frontend and copy assets"
echo "  ./build-backend.sh            # Build backend JAR"
echo "  ./build-all.sh                # Build complete application"
echo ""

echo "🐳 Docker Options:"
echo "  cd database && docker-compose up -d    # Database only"
echo "  docker-compose --profile full-stack up -d  # Full stack"
echo ""

echo "📚 Documentation:"
echo "  cat README.md                 # Main project documentation"
echo "  cat database/README.md       # Database-specific documentation"
echo "  cat database/QUICKSTART.md   # Quick database setup"
echo ""

echo "💡 Quick Development Setup:"
echo "  1. cd database && make start  # Start database"
echo "  2. ./start-backend.sh         # Start backend"
echo "  3. cd frontend && npm start   # Start frontend"
echo ""