# DTime Frontend

React SPA for the DTime time tracking application.

See also [root README — Running the stack](../README.md#running-the-stack) for full-system dev and production steps.

## URLs

| Mode | Frontend | Backend API (browser) |
|------|----------|------------------------|
| **Development** (`npm start`) | https://localhost:3000 | https://localhost:8443 |
| **Docker dev** (`full-stack` profile) | http://localhost:3000 | https://localhost:8443 (set `FRONTEND_BACKEND_URL`) |
| **Docker production** (`production` profile) | http://localhost:3000 | Public URL in `FRONTEND_BACKEND_URL` |

The webpack dev server proxies `/api/*` to the backend URL configured in [webpack.config.js](webpack.config.js).

## Development (local)

**Prerequisites:** Backend running on **https://localhost:8443** ([backend/README.md](../backend/README.md)); Authentik if using OAuth.

```bash
cd frontend
npm install
npm start
```

Opens https://localhost:3000 with hot reload.

## Development (Docker)

From repository root:

```bash
docker compose --profile full-stack up -d
# backend + dtime-frontend (dev image)
```

Or:

```bash
./deploy.sh --env development
```

## Production

### Build static assets

```bash
npm run build
# output in build/
```

### Docker (recommended)

From repository root:

```bash
./build-docker.sh --frontend-only
./deploy.sh --env production
```

Starts `dtime-frontend-prod` (nginx on port 3000 → container port 80) with profile **`production`**.

```bash
docker compose --profile production up -d
```

Set in `.env`:

```bash
FRONTEND_BACKEND_URL=https://your-public-backend-host:8443
```

Runtime injection via [docker-entrypoint.sh](docker-entrypoint.sh) and [public/config.js](public/config.js).

### Manual

Serve the `build/` folder with any static web server; ensure `REACT_APP_BACKEND_URL` / `config.js` points at your API.

## Environment variables

| Variable | Purpose |
|----------|---------|
| `REACT_APP_BACKEND_URL` / `FRONTEND_BACKEND_URL` | Backend base URL as seen by the **browser** |
| `NODE_ENV` | `development` or `production` |

Do not use `http://localhost:8080` for local Authentik dev — the backend listens on **HTTPS 8443**.

## Available scripts

| Command | Description |
|---------|-------------|
| `npm start` | Dev server (port 3000) |
| `npm test` | Tests (watch mode) |
| `npm run build` | Production bundle |

## Authentication

Browser login uses **Authentik OIDC** via the backend (`/oauth2/authorization/authentik`). Configure the backend and Authentik per [root README](../README.md#authentik-setup-local-development).

## Technology stack

React 18, Webpack 5, Bootstrap 5, Axios, React Router 5, Nginx (production image).
