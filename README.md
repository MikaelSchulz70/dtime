# DTime Application

This project has been restructured with separate frontend and backend directories.

## Project Structure

```
dtime/
├── backend/          # Spring Boot application
│   ├── src/
│   │   ├── main/java/
│   │   ├── main/resources/
│   │   └── test/
│   └── pom.xml
├── frontend/         # React application
│   ├── src/
│   ├── public/
│   ├── package.json
│   └── webpack.config.js
├── database/         # PostgreSQL Docker setup
│   ├── Dockerfile
│   ├── scripts/
│   ├── backups/
│   └── Makefile
├── docker-compose.yml      # Full stack
├── docker-compose.db.yml   # Database only
└── README.md
```

## Building the Application

### Backend (Spring Boot)

**Option A: Use startup script (Recommended - no Maven required)**
```bash
# Build once, then run with Java
./build-backend.sh        # Build JAR file
./start-backend.sh        # Run the application
```

**Option B: Maven development mode**
```bash
cd backend
mvn spring-boot:run       # Requires Maven
```

### Frontend (React)
```bash
cd frontend
npm install
npm run build    # Production build
npm start        # Development server
```

## Technology Stack

### Backend
- Spring Boot 3.4.7
- Java 21
- PostgreSQL
- Hibernate/JPA
- Maven

### Frontend
- React 18.3.1
- Webpack 5.97.1
- Axios
- React Router 5.3.4
- React Bootstrap

## Environment Setup

### Required Environment Variables

Copy `.env.example` to `.env.local` and configure:

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/dtime
DATABASE_USERNAME=dtime
DATABASE_PASSWORD=your_secure_password

# Email
MAIL_USERNAME=your-email@example.com
MAIL_PASSWORD=your_email_app_password

# Security (recommended for production)
SECURITY_CSRF_ENABLED=true
```

## Development

### Prerequisites
- Java 21
- Node.js 16+
- PostgreSQL
- Maven

### Setup Steps

1. **Database Setup (Recommended: Docker)**
   ```bash
   # Copy environment template
   cp .env.example .env.local
   # Edit .env.local with your credentials
   
   # Start PostgreSQL with Docker
   docker-compose -f docker-compose.db.yml up -d
   
   # Or use the database Makefile
   cd database && make start
   ```

   **Alternative: Local PostgreSQL**
   ```bash
   createdb dtime
   ```

2. **Backend Setup**
   
   **Option A: Use the startup script (Recommended - minimal Maven dependency)**
   ```bash
   # Build once (requires Maven)
   ./build-backend.sh
   
   # Run anytime (only requires Java)
   ./start-backend.sh
   
   # Or build and run together
   ./start-backend.sh --clean    # Builds if needed
   ```

   **Option B: Direct JAR execution (no startup script)**
   ```bash
   # Build first
   ./build-backend.sh
   
   # Set environment and run
   cd backend
   export DATABASE_PASSWORD=dtime_dev_password
   export MAIL_PASSWORD=dummy_password
   java -jar target/dtime-1.0.0.jar
   ```

   **Option C: Maven development mode (requires Maven always)**
   ```bash
   cd backend
   export DATABASE_PASSWORD=dtime_dev_password
   export MAIL_PASSWORD=dummy_password
   mvn spring-boot:run
   ```

3. **Frontend Setup**
   ```bash
   cd frontend
   npm install
   npm start
   ```

The database runs on port 5432, backend on port 8080, frontend development server on port 3000.

### Quick Start with Docker

```bash
# Database only (recommended for development)
docker-compose -f docker-compose.db.yml up -d

# Full stack (all services)
docker-compose --profile full-stack up -d
```

## Production Deployment

### Security Checklist
- ✅ Set all environment variables
- ✅ Enable CSRF protection (`SECURITY_CSRF_ENABLED=true`)
- ✅ Use HTTPS in production
- ✅ Secure database credentials
- ✅ Configure firewall rules

### Deployment Steps

1. **Build Frontend**
   ```bash
   cd frontend
   npm run build
   ```

2. **Copy Assets to Backend**
   ```bash
   ./build-frontend.sh
   ```

3. **Build Backend**
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```

4. **Run Application**
   ```bash
   java -jar backend/target/dtime-1.0.0.jar
   ```

### Docker Deployment (Recommended)

```dockerfile
# Example Dockerfile for production
FROM openjdk:21-jre-slim
COPY backend/target/dtime-1.0.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```
