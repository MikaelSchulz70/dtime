# DTime Backend

Spring Boot REST API for time tracking, users, projects, and reports.

## Ports and profiles

| Profile | Typical use | HTTPS | Notes |
|---------|-------------|-------|--------|
| `dev` | Local IDE / `mvn spring-boot:run` | **8443** | [application-dev.yml](src/main/resources/application-dev.yml), CSRF off |
| `docker` | Root `docker-compose` backend service | **8443** | DB via `dtime-db` |
| `prod` | Production | **8443** (+ optional HTTP 8080 internally) | See [application-prod.yml](src/main/resources/application-prod.yml) |

## Development (local)

**Prerequisites:** PostgreSQL with `dtime` database; [Authentik](../infra/README.md) if using OAuth.

```bash
cd backend
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

- API base: **https://localhost:8443**
- Health: https://localhost:8443/actuator/health
- OAuth redirect (Authentik): `https://localhost:8443/login/oauth2/code/authentik`

Configure Authentik and env vars per [root README — Authentik setup](../README.md#authentik-setup-local-development) and [`.env.example`](../.env.example).

### Machine JWT (dtime-mcp)

For the MCP server’s `client_credentials` tokens, enable on the backend:

- `OAUTH_AUTHENTIK_MACHINE_JWT_ENABLED=true`
- `OAUTH_AUTHENTIK_MACHINE_JWT_JWK_SET_URI` — if MCP uses a separate Authentik provider (e.g. `dtmcp`)
- `OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS` — MCP token `azp` / `client_id` / `sub`

Dev defaults for machine JWT are in [application-dev.yml](src/main/resources/application-dev.yml). See [mcp/README.md](../mcp/README.md).

## Production (Docker)

From repository root:

```bash
docker compose --profile production up -d dtime-backend
# or
./deploy.sh --env production
```

Set `SPRING_PROFILES_ACTIVE`, database URL, OAuth, and `SECURITY_CSRF_ENABLED=true` via `.env` / compose (see [docker-compose.yml](../docker-compose.yml)).

## Docker (development stack)

```bash
# From repo root
docker compose --profile full-stack up -d dtime-backend
```

## Build

```bash
mvn clean package
# or
docker build -t dtime-backend:latest .
```

## Tests

```bash
mvn test
```
