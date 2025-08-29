# D-Time Docker Setup

This guide explains how to run the D-Time application using Docker Compose.

## Prerequisites

- Docker
- Docker Compose
- Git

## Quick Start

1. **Clone and setup environment:**
   ```bash
   git clone <repository-url>
   cd dtime
   cp .env.example .env
   ```

2. **Configure environment variables:**
   Edit the `.env` file to set your database credentials and other configuration:
   ```bash
   # Example configuration
   DATABASE_NAME=dtime
   DATABASE_USERNAME=dtime_user
   DATABASE_PASSWORD=your_secure_password
   FRONTEND_BACKEND_URL=http://localhost:8080
   ```

3. **Run the full stack:**
   ```bash
   # Start all services (database + backend + frontend)
   docker-compose --profile full-stack up -d
   
   # Or start just the database for local development
   docker-compose up -d dtime-db
   ```

4. **Access the application:**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Database: localhost:5432

## Environment Configuration

**🎯 Runtime Environment Variables**: All environment variables are loaded at container startup, not baked into the Docker images. This allows you to change configuration without rebuilding images.

### Database Configuration (.env file)
```bash
# Database settings
DATABASE_NAME=dtime                    # Database name
DATABASE_USERNAME=dtime_user           # Database username  
DATABASE_PASSWORD=your_secure_password # Database password
```

### Frontend Configuration (.env file)
```bash
# Backend URL as seen from frontend
FRONTEND_BACKEND_URL=http://localhost:8080  # For local development
FRONTEND_BACKEND_URL=http://dtime-backend:8080  # For Docker networking

# The frontend uses runtime environment injection:
# - Development: Uses webpack proxy + process.env
# - Production: Uses nginx proxy + runtime config.js generation
```

## Docker Services

### 1. Database (dtime-db)
- **Image:** postgres:15
- **Port:** 5432
- **Volume:** `postgres_data` for persistence
- **Init Scripts:** `./database/scripts/` for initial setup

### 2. Backend (dtime-backend)
- **Build:** `./backend/Dockerfile`
- **Port:** 8080
- **Profile:** `full-stack`
- **Health Check:** `/actuator/health`

### 3. Frontend (dtime-frontend)  
- **Build:** `./frontend/Dockerfile.dev` (development)
- **Port:** 3000
- **Profile:** `full-stack`
- **Hot Reload:** Source code mounted as volume in dev mode

## Usage Examples

### Development Setup
```bash
# Start only database
docker-compose up -d dtime-db

# Run backend and frontend locally
cd backend && ./mvnw spring-boot:run
cd frontend && npm start
```

### Full Docker Setup (Development)
```bash
# Start everything with development frontend
docker-compose --profile full-stack up -d

# View logs
docker-compose logs -f dtime-backend
docker-compose logs -f dtime-frontend

# Stop everything
docker-compose --profile full-stack down
```

### Production Setup
```bash
# Start with production frontend (nginx + static files)
docker-compose --profile production up -d

# Change environment variables without rebuilding
echo "FRONTEND_BACKEND_URL=http://new-backend:8080" >> .env
docker-compose --profile production restart dtime-frontend-prod
```

## Environment Files

### Root `.env`
Main configuration file for Docker Compose:
```bash
DATABASE_NAME=dtime
DATABASE_USERNAME=dtime
DATABASE_PASSWORD=dtime_dev_password
FRONTEND_BACKEND_URL=http://localhost:8080
```

### Backend `.env` 
Backend-specific configuration:
```bash
DATABASE_USERNAME=dtime_user
DATABASE_PASSWORD=your_password
SPRING_PROFILES_ACTIVE=docker
SECURITY_CSRF_ENABLED=false
MAIL_ENABLED=false
```

### Frontend `.env`
Frontend-specific configuration:
```bash
REACT_APP_BACKEND_URL=http://localhost:8080
NODE_ENV=development
```

## Networking

All services are connected via the `dtime-network` bridge network:
- `dtime-db`: PostgreSQL database
- `dtime-backend`: Spring Boot backend (depends on database)
- `dtime-frontend`: React frontend (depends on backend)

Internal communication uses service names:
- Backend connects to database via `dtime-db:5432`
- Frontend connects to backend via `dtime-backend:8080`

## Data Persistence

- Database data: `postgres_data` volume
- Backend logs: `./backend/logs` bind mount
- Frontend source: `./frontend/src` bind mount (dev mode only)

## Health Checks

- **Database:** `pg_isready` command
- **Backend:** `curl http://localhost:8080/actuator/health`
- **Frontend:** Built-in nginx health

## Runtime Environment Variables

### How it Works

**Backend**: Spring Boot natively supports runtime environment variables.

**Frontend**: Uses a two-stage approach:
1. **Development**: Webpack dev server reads `process.env.REACT_APP_*` variables
2. **Production**: Docker entrypoint script generates `/config.js` at startup with runtime values

### Changing Variables at Runtime

```bash
# Update environment variables
vim .env

# Restart specific service to pick up changes
docker-compose restart dtime-backend
docker-compose restart dtime-frontend-prod

# No image rebuild needed! 🎉
```

## Troubleshooting

### Common Issues

1. **Port conflicts:**
   ```bash
   # Check what's using ports 3000, 8080, 5432
   lsof -i :3000
   lsof -i :8080
   lsof -i :5432
   ```

2. **Environment variables not updating:**
   ```bash
   # Check if variables are loaded correctly
   docker-compose logs dtime-frontend-prod | grep "Generated runtime config"
   docker exec dtime-frontend-prod cat /usr/share/nginx/html/config.js
   ```

3. **Database connection fails:**
   - Check if database is ready: `docker-compose logs dtime-db`
   - Verify credentials in `.env` file
   - Wait for database health check to pass

4. **Frontend can't reach backend:**
   - Check `FRONTEND_BACKEND_URL` in `.env` file
   - Verify backend is running: `curl http://localhost:8080/actuator/health`
   - Check nginx proxy configuration

### Useful Commands

```bash
# View all containers
docker-compose ps

# Follow logs
docker-compose logs -f

# Restart a service
docker-compose restart dtime-backend

# Rebuild and restart
docker-compose up --build -d

# Clean everything
docker-compose down -v
docker system prune -a
```

## Security Notes

- Change default passwords in production
- Use strong database credentials
- Enable CSRF protection for production (`SECURITY_CSRF_ENABLED=true`)
- Use HTTPS in production
- Don't commit `.env` files to version control