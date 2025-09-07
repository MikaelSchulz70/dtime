# DTime Deployment Guide

## Backend URL Configuration

The DTime application automatically configures the frontend to connect to the backend based on your deployment scenario.

### 🐳 Local Docker Development

**Command:**
```bash
./deploy.sh --env development
```

**What happens:**
- Frontend runs in Docker container (port 3000)
- Backend runs in Docker container (port 8443)
- Browser accesses: `https://localhost:3000`
- Frontend connects to backend via: `https://localhost:8443`
- **Auto-configured:** `FRONTEND_BACKEND_URL=https://localhost:8443`

### 🚀 Production Deployment

**Command:**
```bash
./deploy.sh --env production
```

**For production on a server with domain:**
```bash
# In your .env file
FRONTEND_BACKEND_URL=https://your-domain.com

# Then deploy
./deploy.sh --env production
```

**What happens:**
- Frontend runs in Docker container (port 80/443)
- Backend runs in Docker container (port 8443, behind reverse proxy)
- Browser accesses: `https://your-domain.com`
- Frontend connects to backend via: `https://your-domain.com`

### 🛠️ Local Development (Mixed)

**For frontend outside Docker:**
```bash
# Start only database in Docker
docker-compose up -d dtime-db

# Start backend in IntelliJ or:
cd backend && mvn spring-boot:run

# Start frontend locally:
cd frontend && npm start
```

**Configuration:**
```bash
# In your .env or environment
FRONTEND_BACKEND_URL=https://localhost:8443
```

## Configuration Options

### Automatic Configuration
- Leave `FRONTEND_BACKEND_URL` empty in `.env`
- Deploy script will auto-configure based on environment

### Manual Configuration
- Set `FRONTEND_BACKEND_URL` in your `.env` file
- Deploy script will use your configured value

### Examples

**Local Docker:**
```bash
FRONTEND_BACKEND_URL=https://localhost:8443
```

**Production with domain:**
```bash
FRONTEND_BACKEND_URL=https://api.your-company.com
```

**Production with subdirectory:**
```bash
FRONTEND_BACKEND_URL=https://your-company.com/dtime-api
```

## Verification

After deployment, check the logs:
```bash
docker-compose logs dtime-frontend
```

Look for: `Using configured backend URL: https://...`

## Troubleshooting

### Frontend can't reach backend
1. Check `FRONTEND_BACKEND_URL` in logs
2. Verify backend is accessible from browser at that URL
3. Check firewall/network connectivity
4. Verify SSL certificates if using HTTPS

### CORS errors
- Backend and frontend URLs must match exactly
- Include protocol (https://) and port if non-standard
- Check backend CORS configuration