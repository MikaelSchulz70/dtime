# DTime Application

A modern time tracking application with completely separated frontend and backend services, designed for Docker deployment.

## Project Structure

```
dtime/
├── backend/                 # Spring Boot API service
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                # React application
│   ├── src/
│   ├── public/
│   ├── Dockerfile           # Production build
│   ├── Dockerfile.dev       # Development build
│   ├── docker-entrypoint.sh # Runtime config injection
│   ├── package.json
│   └── webpack.config.js
├── database/                # PostgreSQL setup
│   ├── Dockerfile
│   ├── scripts/
│   └── backups/
├── build-docker.sh          # Main build script
├── build-backend-docker.sh  # Backend-only build
├── build-frontend-docker.sh # Frontend-only build
├── deploy.sh                # Deployment script
├── package.sh               # Distribution packaging
├── docker-compose.yml       # Complete stack with profiles
├── .env.example             # Environment template
└── README.md
```

## Quick Start

### 🐳 Docker Deployment (Recommended)

**1. Setup Environment**
```bash
cp .env.example .env
# Edit .env with your configuration
```

**2. Build and Deploy**
```bash
# Build all Docker images
./build-docker.sh

# Deploy development environment
./deploy.sh --env development

# Deploy production environment  
./deploy.sh --env production
```

**3. Access Application**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Database: localhost:5432

### 🔧 Development Mode

**Option A: Docker Development Stack**
```bash
# Start full development environment
docker-compose --profile full-stack up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

**Option B: Local Development**
```bash
# 1. Start database only
docker-compose up -d dtime-db

# 2. Start backend
cd backend && mvn spring-boot:run

# 3. Start frontend
cd frontend && npm install && npm start
```

## Build Scripts

### 🔨 Main Build Script
```bash
./build-docker.sh [OPTIONS]

# Examples:
./build-docker.sh                    # Build all images
./build-docker.sh --backend-only     # Build only backend
./build-docker.sh --frontend-only    # Build only frontend
./build-docker.sh --tag v1.0.0       # Tag images with version
./build-docker.sh --push             # Push to registry
```

### 🚀 Deployment Script
```bash
./deploy.sh [OPTIONS]

# Examples:
./deploy.sh --env production         # Production deployment
./deploy.sh --env development        # Development deployment
./deploy.sh --backup-db              # Backup database first
./deploy.sh --no-build               # Skip building images
```

### 📦 Packaging Script
```bash
./package.sh [OPTIONS]

# Examples:
./package.sh --version v1.0.0       # Create distribution package
./package.sh --no-source            # Package only Docker images
./package.sh --output ./releases    # Custom output directory
```

## Environment Configuration

All configuration is handled via runtime environment variables (no build-time secrets).

### Backend Environment Variables
```bash
# Database
POSTGRES_DB=dtime
POSTGRES_USER=dtime
POSTGRES_PASSWORD=your_secure_password

# Email Configuration
MAIL_USERNAME=your-email@example.com
MAIL_PASSWORD=your_email_app_password

# Security
SECURITY_CSRF_ENABLED=true
```

### Frontend Environment Variables
```bash
# Backend API URL
REACT_APP_BACKEND_URL=http://localhost:8080

# Environment
NODE_ENV=production
```

## Technology Stack

### Backend
- Spring Boot 3.4.7
- Java 21
- PostgreSQL 14
- Hibernate/JPA
- Maven
- Docker multi-stage builds

### Frontend
- React 18.3.1
- Webpack 5.97.1
- Bootstrap 5.3.8
- Axios for API calls
- Runtime environment injection
- Nginx for production serving

### Infrastructure
- Docker & Docker Compose
- PostgreSQL official image
- Multi-stage Docker builds
- Runtime configuration injection
- Comprehensive build automation

## Default Admin Credentials

After first deployment:
- **Username**: `admin@dtime.se`
- **Password**: `admin123`

⚠️ **Change the admin password immediately after first login for security.**

## Production Deployment

### Prerequisites
- Docker & Docker Compose
- 2GB+ RAM
- 10GB+ disk space

### Security Checklist
- ✅ Change default admin password
- ✅ Set secure database passwords
- ✅ Enable CSRF protection (`SECURITY_CSRF_ENABLED=true`)
- ✅ Use HTTPS with reverse proxy
- ✅ Configure firewall rules
- ✅ Regular database backups

### Deployment Options

**Option 1: Full Docker Stack**
```bash
# Production deployment
./deploy.sh --env production --backup-db

# Monitor deployment
docker-compose logs -f
docker-compose ps
```

**Option 2: Custom Registry**
```bash
# Build and push to registry
./build-docker.sh --registry registry.company.com/ --push

# Deploy from registry
./deploy.sh --registry registry.company.com/ --no-build
```

**Option 3: Distribution Package**
```bash
# Create distribution package
./package.sh --version v1.0.0

# Deploy on target system
tar -xzf dist/dtime-v1.0.0.tar.gz
cd dtime-v1.0.0
./load-images.sh
./scripts/deploy.sh
```
