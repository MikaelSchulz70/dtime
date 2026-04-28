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
