# DTime Infra

This folder contains a clean infrastructure setup with:

- PostgreSQL in `infra/postgres`
- Authentik + Redis in `infra/authentik`
- A root Docker Compose in `infra/docker-compose.yml` that starts everything together

## Quick start

1. Copy the shared env template:

   cp .env.example .env

2. Edit `.env` and set secure values for:
   - `POSTGRES_PASSWORD`
   - `AUTHENTIK_SECRET_KEY`

3. Start all services:

   docker compose up -d

4. Access Authentik:
   - http://localhost:9000
   - https://localhost:9443

## Authentik initial setup

After `docker compose up -d`, complete the first-time setup in your browser:

- Open `http://localhost:9000/if/flow/initial-setup/`
- Create the first admin account (email + password)
- Sign in with that account at `http://localhost:9000`

Notes:
- No default Authentik username/password is configured in this project.
- If initial setup was already completed once, this flow might no longer be available.
- To reset setup, run `docker compose down -v` and start again (this removes persisted data).

## Databases created

On first startup, Postgres runs `infra/postgres/scripts/01-init-database.sql` and creates:

- `dtime` (with schema/roles/grants for your app)
- `authentik` (used by Authentik)

## Notes

- Postgres data is persisted in Docker volume `infra_postgres_data`.
- Authentik media/certs/templates are persisted in Docker volumes (`infra_authentik_media`, `infra_authentik_templates`, `infra_authentik_certs`).
- To reset everything, stop containers and run `docker compose down -v`.

## Next: start DTime application

Infrastructure alone does not run the DTime UI or API. After Postgres and Authentik are up:

| Component | How to start | Documentation |
|-----------|----------------|---------------|
| **Backend** | `cd backend && SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run` or root `docker compose --profile full-stack` | [backend/README.md](../backend/README.md) |
| **Frontend** | `cd frontend && npm start` or same compose profile | [frontend/README.md](../frontend/README.md) |
| **MCP** (optional) | `cd mcp && mvn spring-boot:run` or `docker compose --profile mcp` | [mcp/README.md](../mcp/README.md) |

**Full checklist (dev and production):** [root README — Running the stack](../README.md#running-the-stack).

**OAuth for browser login:** create the DTime application in Authentik (provider slug e.g. `dtime`, redirect `https://localhost:8443/login/oauth2/code/authentik`) — [root README — Authentik setup](../README.md#authentik-setup-local-development).

**OAuth for MCP:** separate confidential client with `client_credentials` (provider slug e.g. `dtmcp`) — [mcp/README.md](../mcp/README.md#authentik-setup-read-only-mcp).

Copy shared secrets into the **repository root** `.env` from [`.env.example`](../.env.example) (not only `infra/.env`).
