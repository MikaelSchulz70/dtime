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

Docker Compose sets **`SPRING_PROFILES_ACTIVE=docker,prod`** for `dtime-mcp`, with default backend **`https://host.docker.internal:8443`** (IntelliJ/dev on the host) and **`MCP_OAUTH_TOKEN_URI=http://host.docker.internal:9000/application/o/token/`** (Authentik on the host). [`start-ollama-bridge.sh`](../start-ollama-bridge.sh) sets `MCP_BACKEND_URL=https://dtime-backend:8443` when it starts the Docker backend. Do not rely on backend’s `OAUTH_AUTHENTIK_TOKEN_URI` when it uses `localhost`, as that breaks from inside containers.

| Variable | Purpose |
|----------|---------|
| `SERVER_PORT` | MCP listen port (default **8081**) |
| `MCP_BACKEND_URL` | Backend base URL, e.g. `https://localhost:8443` |
| `MCP_OAUTH_TOKEN_URI` | Authentik token URL, e.g. `http://localhost:9000/application/o/token/` |
| `MCP_OAUTH_CLIENT_ID` | MCP confidential client identifier |
| `MCP_OAUTH_CLIENT_SECRET` | Client secret (`client_secret_post`) |
| `MCP_OAUTH_SCOPE` | Optional scope string passed to token endpoint |
| `MCP_VALIDATE_ON_STARTUP` | If `true` (default), run connectivity probe at startup (non-fatal unless `MCP_FAIL_ON_UNAVAILABLE=true`) |
| `MCP_BACKEND_CONNECTIVITY_CHECK_INTERVAL_MS` | Retry interval while Authentik/backend unavailable (default **30000**) |
| `MCP_FAIL_ON_UNAVAILABLE` | If `true`, exit when Authentik/backend unreachable at startup (legacy strict mode) |
| `MCP_HTTP_LOG_ACCESS` | Log inbound `POST /mcp` from Ollama bridge / Cursor (default **true**) |
| `MCP_BACKEND_TRUST_INSECURE_SSL` | When `true` (default in **dev** / **docker** profiles), HTTPS calls to the backend trust any certificate (fixes PKIX against the dev `dtime_new.p12`). Use `false` when the backend uses a public CA. **Never** enable toward untrusted hosts. |

### Backend (machine JWT)

| Variable | Purpose |
|----------|---------|
| `OAUTH_AUTHENTIK_MACHINE_JWT_ENABLED` | Accept Bearer JWTs on `/api/**` in parallel with cookies |
| `OAUTH_AUTHENTIK_MACHINE_JWT_JWK_SET_URI` | JWKS for MCP tokens when Authentik provider ≠ browser (e.g. `dtmcp`); falls back to `OAUTH_AUTHENTIK_JWK_SET_URI` if empty |
| `OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS` | Comma-separated client ids permitted for machine access (see [`AuthentikMachineJwtProperties`](../backend/src/main/java/se/dtime/config/AuthentikMachineJwtProperties.java)) |

You can still set `oauth.authentik.machine-jwt.authorized-client-ids` as a **YAML list** in a profile-specific backend file; the comma-separated env var is merged in `@PostConstruct` when set.

### Resilient startup and health

By default MCP **stays up** when Authentik or dtime-backend is temporarily down (OAuth client id/secret must still be configured):

- Startup logs `WARN` in **degraded mode** and retries every `MCP_BACKEND_CONNECTIVITY_CHECK_INTERVAL_MS` (default 30s).
- Scheduled connectivity checks **reuse** the cached Authentik access token until it nears expiry (~60s skew); a new token is fetched only when the cache is empty or stale (not every 30s).
- **`GET /actuator/health`** stays **UP**; details include `authentik: ok|unavailable` and `backend: ok|unavailable`.
- Tool calls fail until backend connectivity is restored (expected).

Strict legacy behavior: `MCP_FAIL_ON_UNAVAILABLE=true` (exit if dependencies unreachable at startup).

### Ollama / bridge access logs

When `MCP_HTTP_LOG_ACCESS=true` (default), each request to **`/mcp`** logs at **INFO**:

```text
MCP HTTP POST /mcp from 172.18.0.5 User-Agent=python-httpx/...
MCP HTTP POST /mcp -> 200 (42ms)
```

Tool execution logs at **INFO**, e.g. `MCP tool getUserCount -> GET /api/users/paged?...` and `MCP tool getUserCount completed (38ms)`.

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
| **ollama-mcp-bridge** (admin Chat) | `http://dtime-mcp:8081/mcp` | **8 tools** via include filter in [`bridge-config.json`](../ollama/bridge-config.json) — omits user timesheet tools. See [ollama/README.md](../ollama/README.md). |

The bridge exposes a **chat-optimized subset** of MCP tools (8 today) to keep Ollama prompts small; Cursor gets the full tool list (~22). Backend access is still the **machine** OAuth client (admin API scope), not the signed-in browser user. For report/time-period questions, see [Examples](#examples-mcp-tool-calls) below.

### Troubleshooting `API unauthorized (401)`

| Symptom | Fix |
|--------|-----|
| MCP logs `Obtained new access token` then **401** on `/api/...` | Backend is not accepting the Bearer JWT: set `OAUTH_AUTHENTIK_MACHINE_JWT_ENABLED=true` and add the MCP client id to `OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS`, then **restart backend**. |
| MCP startup fails with backend Bearer message | Same as above; run `print-access-token-claims.sh` to see the allowlist value. Or start backend first — MCP now retries in degraded mode unless `MCP_FAIL_ON_UNAVAILABLE=true`. |
| **403** on admin endpoints | Token is valid but client id not allowlisted (empty roles). |
| MCP logs `localhost:9000` token errors in Docker | `.env` has `MCP_OAUTH_TOKEN_URI=localhost` — compose uses **`MCP_DOCKER_OAUTH_TOKEN_URI`** (`host.docker.internal`) for `dtime-mcp`. Recreate: `docker compose --profile mcp up -d --force-recreate dtime-mcp` |

## Docker

MCP can run **without** starting `dtime-backend` in Compose. Point `MCP_BACKEND_URL` at a reachable backend (compose service or host). MCP stays up in **degraded mode** and retries if the backend is down (`MCP_FAIL_ON_UNAVAILABLE=false` by default).

### MCP only (independent)

```bash
# Backend already running (IntelliJ, or dtime-backend started separately)
docker compose --profile mcp up -d dtime-mcp
```

With backend on the **host** from inside the MCP container:

```bash
# In .env
MCP_BACKEND_URL=https://host.docker.internal:8443
```

### MCP + backend in Docker

```bash
docker compose --profile mcp up -d dtime-backend dtime-mcp

# Or full stack including frontend dev container
docker compose --profile full-stack --profile mcp up -d
```

`docker compose --profile mcp up -d` (no service names) starts **MCP + ollama-mcp-bridge** only — not the backend. Add `dtime-backend` when you need it.

### Bridge + MCP in Docker (default for Chat)

```bash
./start-ollama-bridge.sh
# auto (default): IntelliJ/host backend if :8443 healthy, else Docker backend + MCP
# CHAT_BACKEND=host|docker to force a mode
```

Bridge only (when `dtime-mcp` is already running):

```bash
BRIDGE_MCP_CONFIG=bridge-config.json docker compose --profile ollama up -d dtime-ollama-bridge
```

Optional host MCP: `BRIDGE_MCP_CONFIG=bridge-config.host-mcp.json ./start-ollama-bridge.sh`

See [ollama/README.md](../ollama/README.md).

Set in `.env`: `MCP_OAUTH_CLIENT_ID`, `MCP_OAUTH_CLIENT_SECRET`, `OAUTH_AUTHENTIK_MACHINE_JWT_*`, etc. (see [`.env.example`](../.env.example)).

### Production

```bash
# Production backend + MCP (frontend-prod pulls in backend via depends_on)
docker compose --profile production --profile mcp up -d

# MCP only (backend already running)
docker compose --profile mcp up -d dtime-mcp
```

MCP container uses `SPRING_PROFILES_ACTIVE=docker,prod`, default `MCP_BACKEND_URL=https://host.docker.internal:8443` (IntelliJ/dev), and `MCP_OAUTH_TOKEN_URI` pointing at reachable Authentik (default `http://host.docker.internal:9000/...`).

### Build image only

```bash
docker build -t dtime-mcp:latest mcp/
```

## Tools

All tools delegate to **`BackendApiClient`**, which only performs **HTTP GET**. Implementations live under [`mcp/src/main/java/se/dtime/mcp/service/`](src/main/java/se/dtime/mcp/service/) (one `@Service` per API area). After a rebuild, the Ollama bridge health check reports **`tools`: 22** (`curl -s http://127.0.0.1:8082/health`).

**Period-based reads:** reports, time sheets, vacations, and timereport status use explicit path segments (`year`, `month`, or ISO `week`). Omit `year`/`month` on MCP tools to use the current period.

### Examples (MCP tool calls)

| Question | Tool | Arguments |
|----------|------|-------------|
| How many users in DTime? | `getUserCount` | no arguments → read `totalElements` |
| Org-wide hours per user in May 2026 | `getReport` | `year=2026`, `month=5`, `type=USER` |
| Current month (default period) | `getReport` | `type=USER` — omit `year`/`month` |
| Yearly org-wide report | `getYearlyReport` | `year=2026`, `type=USER` |
| One user's total hours in May 2026 | `getReport` | `type=USER`, find user in `userReports` |
| One user's day-by-day timesheet (Cursor / full MCP) | `getUserTimeReportForMonth` | `userId` from `getPagedUsers`, `year=2026`, `month=5` — **not** in admin Chat bridge |
| Vacation balances for March 2026 | `getVacationReport` | `year=2026`, `month=3` |
| Unclosed reports for last month | `getUnclosedUsers` | `year` and `month` for target month |

Admin Chat uses **`getReport` only** for hours (no user timesheet tools) to avoid tool confusion. Use `getReport` with `type=USER` for org-wide or per-user **totals**, not day-by-day grids.

### Users (`/api/users`)

| Tool | Backend |
|------|---------|
| `getUserCount` | `GET /api/users/paged?page=0&size=1` → returns `{ "totalElements": N }` only |
| `getAllUsers` | `GET /api/users` — optional `active` |
| `getPagedUsers` | `GET /api/users/paged` |
| `getUser` | `GET /api/users/{id}` |

### Accounts (`/api/account`)

| Tool | Backend |
|------|---------|
| `getAllAccounts` | `GET /api/account` — optional `active` |
| `getPagedAccounts` | `GET /api/account/paged` |
| `getAccount` | `GET /api/account/{id}` |

### Tasks (`/api/task`)

| Tool | Backend |
|------|---------|
| `getAllTasks` | `GET /api/task` — optional `active` |
| `getPagedTasks` | `GET /api/task/paged` |
| `getTask` | `GET /api/task/{id}` |

### Task contributors (`/api/taskcontributor`)

| Tool | Backend |
|------|---------|
| `getTaskContributors` | `GET /api/taskcontributor/{userId}` |
| `getCurrentTaskContributors` | `GET /api/taskcontributor/currentTaskContributors` |

### Special days (`/api/specialday`)

| Tool | Backend |
|------|---------|
| `getAllSpecialDays` | `GET /api/specialday` |
| `getSpecialDayYears` | `GET /api/specialday/years` |
| `getSpecialDaysByYear` | `GET /api/specialday/year/{year}` |
| `getSpecialDay` | `GET /api/specialday/{id}` |

### Time reports (`/api/timereport`)

Only **`/user`** (admin, requires `userId`) and **`/vacations`** are exposed. The logged-in-user sheet `GET /api/timereport` is not available via MCP (see [Not exposed](#not-exposed-by-design)).

| Tool | Backend |
|------|---------|
| `getUserTimeReportForMonth` | `GET /api/timereport/user/{userId}/year/{year}/month/{month}` — **Full MCP / Cursor only** |
| `getUserTimeReportForWeek` | `GET /api/timereport/user/{userId}/year/{year}/week/{week}` — **Full MCP / Cursor only** |
| `getVacationReport` | `GET /api/timereport/vacations/year/{year}/month/{month}` |

### Time report status (`/api/timereportstatus`)

| Tool | Backend |
|------|---------|
| `getUnclosedUsers` | `GET /api/timereportstatus/year/{year}/month/{month}` |

### Reports (`/api/report`) — admin aggregates

| Tool | Backend |
|------|---------|
| `getReport` | `GET /api/report/year/{year}/month/{month}?type=` — monthly; omit `year`/`month` for current month |
| `getYearlyReport` | `GET /api/report/year/{year}?type=` |
| `getBillableTaskTypeReport` | `GET /api/report/billable-task-type` — `fromDate`, `toDate` |

### System (`/api/system`)

| Tool | Backend |
|------|---------|
| `getSystemConfig` | `GET /api/system/config` |
| `getMailEnabled` | `GET /api/system/mail/enabled` |

Bulk list tools (`getAllUsers`, `getAllAccounts`, `getAllTasks`) may return large JSON payloads; tool descriptions recommend **paged** tools when possible.

### Not exposed (by design)

| Endpoint | Reason |
|----------|--------|
| `/api/auth/**` | OIDC redirect flows, not data APIs |
| `/api/session` | Requires interactive browser user principal |
| `GET /api/timereport/year/{year}/month/{month}` | Logged-in user's sheet; machine JWT has no real end-user |
| `GET /api/report/user/year/{year}/month/{month}` | Interactive user scope; use admin `getReport` with `type=USER` for org-wide data |

## License

Same as the root dtime project.
