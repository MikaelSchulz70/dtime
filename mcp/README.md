# dtime-mcp

Spring Boot MCP server (**Spring AI 2**, streamable HTTP) that exposes **read-only** tools backed entirely by **GET** [`/api/...`](/backend/src/main/java/se/dtime/restcontroller/) endpoints on **dtime-backend**.

Authentication is **Authentik OAuth2 client_credentials** → **`Authorization: Bearer`** on REST calls.

Full-stack startup (dev and production): [root README — Running the stack](../README.md#running-the-stack). Helper script: [`../start-mcp.sh`](../start-mcp.sh).

## Ports (no overlap with backend / frontend)

| Service | Default | Notes |
|--------|---------|--------|
| dtime-backend | **8443** | HTTPS — see `backend` `application-dev.yml` / `application-docker.yml` |
| dtime-backend (prod profile) | **8080** + **8443** | HTTP connector + HTTPS — keep MCP off **8080** if you run prod locally |
| Frontend webpack dev | **3000** | `FRONTEND_PORT` in `frontend/webpack.config.js` |
| dtime-mcp | **8081** | `SERVER_PORT` or Docker `MCP_PORT` — see root `docker-compose.yml` header |

## Prerequisites

1. **dtime-backend** with Authentik interactive login unchanged (existing browser/OIDC session flow).
2. **Authentik**: create a confidential **OAuth2 Provider** application (or OAuth2 client under your app) usable with **`client_credentials`**. Prefer a dedicated MCP client so you can scope and rotate secrets independently.
3. **Backend** ([`oauth.authentik.machine-jwt`](../backend/src/main/resources/application.yml)): set `enabled=true` and list allowed machine client identifiers in `authorized-client-ids` (values must match the **`client_id` / `azp` / `sub`** claim Authentik puts on tokens—often the provider client UUID configured in Authentik).
4. **TLS**: local dev backend usually runs on **`https://localhost:8443`**. Align `MCP_BACKEND_URL` accordingly.

## Authentik setup (read-only MCP)

The MCP uses a **confidential machine client** (`client_credentials`), not interactive user login.

**Same provider as browser (recommended):** link the MCP Authentik application to the existing **dtime** OAuth2 provider; machine JWT can use the same JWKS as [`oauth.authentik.jwk-set-uri`](../backend/src/main/resources/application-dev.yml).

**Separate MCP provider (e.g. slug `dtmcp`):** set **`oauth.authentik.machine-jwt.jwk-set-uri`** (env `OAUTH_AUTHENTIK_MACHINE_JWT_JWK_SET_URI`) to that provider’s JWKS, e.g. `http://localhost:9000/application/o/dtmcp/jwks/`. The dev profile in this repo is wired for that layout by default.

### Steps in Authentik (wording varies by version)

1. Open **Applications** and find the **OAuth2/OpenID Provider** already used for dtime (the **slug** in URLs is `dtime` in default dev YAML: `/application/o/dtime/...`).
2. **Create a new Application** (e.g. `dtime-mcp`) and **link it to that existing provider** — do not create a second provider unless you will also change `OAUTH_AUTHENTIK_JWK_SET_URI` on the backend.
3. Set the OAuth client type to **Confidential** and save the **Client ID** and **Client secret**.
4. On the **OAuth2/OpenID Provider**, enable the **Client credentials** grant (often under advanced / protocol settings). Without this, token requests fail with `unsupported_grant_type` or similar.
5. Optionally configure scopes; if required, set [`MCP_OAUTH_SCOPE`](src/main/resources/application.yml) for the MCP process.

### Values to copy into dtime

| From Authentik | Where it goes |
|----------------|----------------|
| Token URL (same as browser), e.g. `http://localhost:9000/application/o/token/` | `MCP_OAUTH_TOKEN_URI`; already matches `OAUTH_AUTHENTIK_TOKEN_URI` locally |
| New MCP app **Client ID** | `MCP_OAUTH_CLIENT_ID` |
| New MCP app **Client secret** | `MCP_OAUTH_CLIENT_SECRET` |
| JWKS URL for that provider | Same provider: leave `OAUTH_AUTHENTIK_JWK_SET_URI` only. Separate provider: `OAUTH_AUTHENTIK_MACHINE_JWT_JWK_SET_URI` (e.g. `.../o/dtmcp/jwks/`) |
| **Allowlist** — decode token `client_id`, `azp`, `sub` (backend uses first non-blank) | `OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS` (comma-separated) |

Enable machine JWT on the backend: `OAUTH_AUTHENTIK_MACHINE_JWT_ENABLED=true`.

### Helper scripts (repo root optional `.env`)

| Script | Purpose |
|--------|---------|
| [`scripts/fetch-client-credentials-token.sh`](scripts/fetch-client-credentials-token.sh) | `curl` token endpoint; prints JSON |
| [`scripts/print-access-token-claims.sh`](scripts/print-access-token-claims.sh) | Decode JWT payload; highlights allowlist claims |
| [`scripts/mcp-smoke-test.sh`](scripts/mcp-smoke-test.sh) | Token → claims → `GET` backend `/api/users/paged` with Bearer → MCP `/actuator/health` |

Example (with env or `.env`):

```bash
export MCP_OAUTH_TOKEN_URI=http://localhost:9000/application/o/token/
export MCP_OAUTH_CLIENT_ID=<mcp-confidential-client-id>
export MCP_OAUTH_CLIENT_SECRET=<secret>
./mcp/scripts/fetch-client-credentials-token.sh | tee /tmp/tok.json
export ACCESS_TOKEN="$(jq -r .access_token /tmp/tok.json)"
./mcp/scripts/print-access-token-claims.sh
```

### Machine vs interactive users

**Authentik users** sign in through the browser OIDC flow. The **MCP** authenticates as one fixed service client; it does not impersonate an Authentik end-user. Per-user MCP access would require a different design (e.g. token exchange or user tokens).

## Configuration (environment variables)

With **no env set**, local `mvn spring-boot:run` uses Spring profile **`dev`**: defaults match `backend` `application-dev.yml` (`https://localhost:8443` + `http://localhost:9000/application/o/token/`).

Docker Compose sets **`SPRING_PROFILES_ACTIVE=docker,prod`** for `dtime-mcp`, with defaults `https://dtime-backend:8443` and **`MCP_OAUTH_TOKEN_URI=http://host.docker.internal:9000/application/o/token/`** (Authentik on the host). If Authentik runs as another compose service, point `MCP_OAUTH_TOKEN_URI` at that hostname instead. Do not rely on backend’s `OAUTH_AUTHENTIK_TOKEN_URI` when it uses `localhost`, as that breaks from inside the MCP container.

| Variable | Purpose |
|----------|---------|
| `SERVER_PORT` | MCP listen port (default **8081**) |
| `MCP_BACKEND_URL` | Backend base URL, e.g. `https://localhost:8443` |
| `MCP_OAUTH_TOKEN_URI` | Authentik token URL, e.g. `http://localhost:9000/application/o/token/` |
| `MCP_OAUTH_CLIENT_ID` | MCP confidential client identifier |
| `MCP_OAUTH_CLIENT_SECRET` | Client secret (`client_secret_post`) |
| `MCP_OAUTH_SCOPE` | Optional scope string passed to token endpoint |
| `MCP_VALIDATE_ON_STARTUP` | If `true` (default), fail fast when token issuance fails |
| `MCP_BACKEND_TRUST_INSECURE_SSL` | When `true` (default in **dev** / **docker** profiles), HTTPS calls to the backend trust any certificate (fixes PKIX against the dev `dtime_new.p12`). Use `false` when the backend uses a public CA. **Never** enable toward untrusted hosts. |

### Backend (machine JWT)

| Variable | Purpose |
|----------|---------|
| `OAUTH_AUTHENTIK_MACHINE_JWT_ENABLED` | Accept Bearer JWTs on `/api/**` in parallel with cookies |
| `OAUTH_AUTHENTIK_MACHINE_JWT_JWK_SET_URI` | JWKS for MCP tokens when Authentik provider ≠ browser (e.g. `dtmcp`); falls back to `OAUTH_AUTHENTIK_JWK_SET_URI` if empty |
| `OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS` | Comma-separated client ids permitted for machine access (see [`AuthentikMachineJwtProperties`](../backend/src/main/java/se/dtime/config/AuthentikMachineJwtProperties.java)) |

You can still set `oauth.authentik.machine-jwt.authorized-client-ids` as a **YAML list** in a profile-specific backend file; the comma-separated env var is merged in `@PostConstruct` when set.

## Quick start (development, local)

**Prerequisites:** [dtime-backend](../backend/README.md) running; [Authentik](../infra/README.md); machine JWT configured (see below).

```bash
# From repo root — copy MCP_* and OAUTH_AUTHENTIK_MACHINE_JWT_* from .env.example into .env
cd mcp
export MCP_BACKEND_URL=https://localhost:8443
export MCP_OAUTH_TOKEN_URI=http://localhost:9000/application/o/token/
export MCP_OAUTH_CLIENT_ID=...
export MCP_OAUTH_CLIENT_SECRET=...
mvn spring-boot:run
```

- Spring profile **`dev`** is the default (`spring.profiles.active: dev` in [application.yml](src/main/resources/application.yml)).
- **`MCP_BACKEND_TRUST_INSECURE_SSL`** defaults to **true** in dev (self-signed backend cert).

On **dtime-backend** (`dev` profile defaults `OAUTH_AUTHENTIK_MACHINE_JWT_ENABLED` to **true**):

```bash
# Decode token claims for allowlist (from repo root):
./mcp/scripts/print-access-token-claims.sh   # needs MCP_OAUTH_* and ACCESS_TOKEN

export OAUTH_AUTHENTIK_MACHINE_JWT_ENABLED=true
export OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS=<azp-or-client_id-from-token>
# If MCP uses Authentik provider slug dtmcp:
export OAUTH_AUTHENTIK_MACHINE_JWT_JWK_SET_URI=http://localhost:9000/application/o/dtmcp/jwks/
# Restart backend, then restart MCP.
```

**Verify:** `./mcp/scripts/mcp-smoke-test.sh` or `./start-mcp.sh`

- Health: http://localhost:8081/actuator/health  
- MCP (streamable): **POST** http://localhost:8081/mcp ([Spring AI MCP server docs](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html))
- Cursor: add to `~/.cursor/mcp.json` or `.cursor/mcp.json`: `"url": "http://localhost:8081/mcp"`

## Consumers

| Client | MCP URL | Notes |
|--------|---------|--------|
| **Cursor** | `http://localhost:8081/mcp` | Streamable HTTP; no MCP endpoint auth in v1 |
| **ollama-mcp-bridge** (admin Chat) | `http://dtime-mcp:8081/mcp` | Inside Docker Compose; see [ollama/README.md](../ollama/README.md) |

No MCP Java changes are required for v1 when the bridge points at the existing streamable HTTP endpoint. The bridge uses the same read-only tools as Cursor; backend access is still the **machine** OAuth client (admin API scope), not the signed-in browser user.

### Troubleshooting `API unauthorized (401)`

| Symptom | Fix |
|--------|-----|
| MCP logs `Obtained new access token` then **401** on `/api/...` | Backend is not accepting the Bearer JWT: set `OAUTH_AUTHENTIK_MACHINE_JWT_ENABLED=true` and add the MCP client id to `OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS`, then **restart backend**. |
| MCP startup fails with backend Bearer message | Same as above; run `print-access-token-claims.sh` to see the allowlist value. |
| **403** on admin endpoints | Token is valid but client id not allowlisted (empty roles). |
| PKIX / certificate errors | Use `dev` profile (`MCP_BACKEND_TRUST_INSECURE_SSL` defaults true) or import the dev keystore. |

## Docker

### Development

From repository root (backend must be healthy first):

```bash
# Backend + MCP (Authentik on host → host.docker.internal token URL by default)
docker compose --profile mcp up -d

# Or full stack including frontend dev container + MCP
docker compose --profile full-stack --profile mcp up -d
```

Set in `.env`: `MCP_OAUTH_CLIENT_ID`, `MCP_OAUTH_CLIENT_SECRET`, `OAUTH_AUTHENTIK_MACHINE_JWT_*`, etc. (see [`.env.example`](../.env.example)).

### Production

```bash
# From repo root — production backend + MCP only
docker compose --profile production --profile mcp up -d

# Or after ./deploy.sh --env production, start MCP separately:
docker compose --profile mcp up -d dtime-mcp
```

MCP container uses `SPRING_PROFILES_ACTIVE=docker,prod`, `MCP_BACKEND_URL=https://dtime-backend:8443`, and `MCP_OAUTH_TOKEN_URI` pointing at reachable Authentik (default `http://host.docker.internal:9000/...`).

### Build image only

```bash
docker build -t dtime-mcp:latest mcp/
```

## Tools

All tools delegate to **`BackendApiClient`**, which only performs **HTTP GET**:

- `getPagedUsers`, `getUser`
- `getPagedAccounts`, `getAccount`
- `getPagedTasks`, `getTask`
- `getTaskContributors`
- `getUserTimeReport` — **admin**; `view` must match backend `TimeReportView` (`WEEK`, `MONTH`, …)
- `getVacationReport`, `getSystemConfig`

Bulk `GET /api/users` etc. without pagination are deliberately **not** exposed (LLM/context safety).

## License

Same as the root dtime project.
