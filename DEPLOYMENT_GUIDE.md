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

### Admin Chat with Ollama (optional)

**Ollama runs natively on the host** (not in Docker). Install and start it before deploying Chat.

**Prerequisites on the server:**

```bash
# Install: https://ollama.com/download
# Mac: brew install ollama && brew services start ollama
# Linux: enable systemd service, then verify:
curl -s http://127.0.0.1:11434/api/tags
```

**Command:**

```bash
./deploy.sh --env production --with-chat
```

Or set `DEPLOY_OLLAMA=true` in `.env`.

**What starts:** backend, MCP, MCP bridge, and production frontend (nginx proxies `/api/ollama/` to the bridge).

**Models:** required models from [`ollama/models.manifest.yaml`](ollama/models.manifest.yaml) are pulled automatically via native `ollama pull`. Install optional models (e.g. `qwen3`) via **Chat → Models** or `./ollama/scripts/pull-models.sh --model qwen3`.

See [ollama/README.md](ollama/README.md#model-manifest-and-updates).

**For production on a server with domain:**
```bash
# In your .env file
FRONTEND_BACKEND_URL=https://your-domain.com

# Then deploy (add --with-chat if using admin Chat)
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
cd infra && docker-compose up -d postgres

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