# Ollama + MCP bridge (DTime)

**Native Ollama** on the host; **MCP** (`dtime-mcp`) and **ollama-mcp-bridge** (`dtime-ollama-bridge`) run in **Docker**. Chat uses MCP tools against the DTime backend.

Ollama is not in Docker — use the native install for Metal/GPU performance on Mac.

## Architecture

| Component | Where | Host port | Purpose |
|-----------|--------|-----------|---------|
| Ollama | **Native on host** | **11434** | Model runtime (`ollama pull …`) |
| MCP | **`dtime-mcp` (Docker)** | **8081** | Streamable HTTP at `/mcp` |
| MCP bridge | **`dtime-ollama-bridge` (Docker)** | **8082** → **8000** | `/api/chat` + MCP; proxies other `/api/*` to Ollama |

```text
Browser → bridge (Docker :8082) → native Ollama (:11434)
              └→ dtime-mcp (Docker :8081) → dtime-backend → DTime API
```

Default bridge config: [`bridge-config.json`](bridge-config.json) → `http://dtime-mcp:8081/mcp` (Docker MCP on the compose network).

Optional [`bridge-config.host-mcp.json`](bridge-config.host-mcp.json) only if MCP runs via `mvn` on the host instead of `dtime-mcp`.

## Install Ollama (host)

**Mac (Homebrew):**

```bash
brew install ollama
brew services start ollama
```

**Linux / other:** [https://ollama.com/download](https://ollama.com/download) — enable the systemd service on servers.

Verify:

```bash
curl -s http://127.0.0.1:11434/api/tags
ollama pull llama3.2
```

## Chat dev (recommended)

From repo root (native Ollama + MCP + bridge in Docker; **detects backend on host or in Docker**):

```bash
./start-ollama-chat.sh
```

**Typical IntelliJ flow:** run backend with **dev** profile in IntelliJ, then `./start-ollama-chat.sh`. MCP in Docker calls `https://host.docker.internal:8443`. Set `CHAT_BACKEND=host` to refuse starting `dtime-backend` in Docker.

**All-Docker flow:** stop host backend, run `./start-ollama-chat.sh` (or `CHAT_BACKEND=docker`) — starts `dtime-backend` + `dtime-mcp` + bridge.

Then: `cd frontend && npm start` → admin **Chat** at `/chat`.

```bash
curl -s http://127.0.0.1:8082/health   # expect tools: 8
```

## Bridge + MCP in Docker (manual)

```bash
./start-ollama-bridge.sh
# auto: host backend if :8443 healthy, else Docker backend + MCP
```

Start **bridge only** when `dtime-mcp` is already up:

```bash
export BRIDGE_MCP_CONFIG=bridge-config.json
docker compose --profile ollama up -d dtime-ollama-bridge
```

## Optional: MCP on host (IntelliJ / mvn)

Not the default. Use only when `dtime-mcp` container is **not** running:

```bash
BRIDGE_MCP_CONFIG=bridge-config.host-mcp.json ./start-ollama-bridge.sh
```

PostgreSQL is **`dtime-postgres`** from **infra/** only (`cd infra && docker compose up -d postgres`).

## Memory and model choice

Native Ollama uses **host RAM** directly (no Docker VM cap). On Apple Silicon, Ollama uses **Metal** for much faster inference than CPU-only Docker.

| Situation | Suggestion |
|-----------|------------|
| Mac with 16+ GiB RAM | **`llama3.2`** or **`qwen3`** for better tool calling |
| Limited RAM | **`llama3.2:1b`** or **`llama3.2:3b`** |
| Chat errors about memory | Use a smaller model or free host RAM |

Set `OLLAMA_DEFAULT_MODEL` in `.env` or pick a model in the Chat UI.

## Model manifest and updates

Recommended models: [`models.manifest.yaml`](models.manifest.yaml). Sync to the Chat UI:

```bash
./ollama/scripts/sync-manifest-json.sh
```

| Task | Command |
|------|---------|
| Install required models (if missing) | `./ollama/scripts/pull-models.sh` |
| Install all manifest models | `./ollama/scripts/pull-models.sh --all` |
| Update models from registry | `./ollama/scripts/pull-models.sh --upgrade` |
| CI check | `./ollama/scripts/pull-models.sh --check` |
| One model | `./ollama/scripts/pull-models.sh --model qwen3` |

Admin **Chat → Models** tab can install/update/remove manifest models without the CLI.

**Ollama upgrades:** `brew upgrade ollama` (Mac) or update the Linux package. Manifest field `ollama_min_version` documents the tested minimum.

**Production with Chat:** install Ollama on the server, then `./deploy.sh --with-chat` or `DEPLOY_OLLAMA=true` in `.env`.

### Migrating from Docker Ollama

If models lived only in the old `dtime-ollama` container volume, re-pull on native Ollama:

```bash
ollama pull llama3.2
```

## Tool calling

The bridge registers DTime MCP tools on every `POST /api/chat`. Ollama decides whether to invoke them; the bridge executes tool calls and streams results back.

### Verify tools are connected

```bash
curl -s http://127.0.0.1:8082/health
```

| `tools` value | Meaning |
|---------------|---------|
| **8** | MCP connected — chat-optimized tool set |
| **0** | No MCP tools — the model will **hallucinate**. Fix MCP/bridge config before using Chat |

If MCP runs on the **host** (not `dtime-mcp`), set `BRIDGE_MCP_CONFIG=bridge-config.host-mcp.json`. Default Docker setup uses [`bridge-config.json`](bridge-config.json).

During a chat turn you should see:

1. Chat UI — **Calling: dtime.getUserCount** (or similar)
2. MCP logs — `MCP tool getUserCount -> GET /api/...`

### Model choice for tool use

`llama3.2` (default) often **skips tool calls**. For admin Chat with MCP, prefer:

```bash
ollama pull qwen3
```

Set `OLLAMA_DEFAULT_MODEL=qwen3` in `.env` or use the Chat UI.

See [Ollama tool calling](https://docs.ollama.com/capabilities/tool-calling).

### Performance

First chat turn is dominated by **Ollama prompt eval** (system prompt + tool schemas). Tuning in this repo:

| Setting | Where | Effect |
|---------|--------|--------|
| **8 chat tools** (include filter) | `bridge-config*.json` | Smaller prompt vs full MCP |
| Short system prompt | `docker-compose.bridge.yml` | Fewer input tokens |
| `MAX_TOOL_ROUNDS=3` | bridge env | Stops endless tool retries |
| Chat `options` | frontend `ChatService.js` | Caps reply length, keeps model warm |
| Native Ollama + Metal | host install | Much faster than Docker CPU-only |

After changing bridge config, recreate the bridge:

```bash
docker compose --profile ollama up -d --force-recreate dtime-ollama-bridge
curl -s http://127.0.0.1:8082/health   # expect tools: 8
```

## MCP tools and period-based `date`

The bridge calls **dtime-mcp** read-only tools on each chat turn. Factual answers must come from tool JSON, not invented numbers.

**Period rule:** pass `date` as any ISO day inside the target week/month (e.g. `2026-05-15` for May 2026). Omit `date` for the current period.

| Use case | MCP tool | Typical args |
|----------|----------|--------------|
| How many users | `getUserCount` | no args → read `totalElements` |
| Org-wide monthly per-user hours | `getReport` | `view=MONTH`, `type=USER`, `date` in that month |
| One user's total hours in a month | `getReport` | `type=USER`, find user in `userReports` |
| Vacations / unclosed users | `getVacationReport`, `getUnclosedUsers` | optional `date` in target month |

Admin Chat omits **`getUserTimeReport`** (day-by-day grid) to reduce tool confusion — full MCP / Cursor only. See [mcp/README.md](../mcp/README.md).

## Environment variables

See root [`.env.example`](../.env.example) (`OLLAMA_URL`, `OLLAMA_*`, `OLLAMA_BRIDGE_*`). Bridge image: `ghcr.io/jonigl/ollama-mcp-bridge:v0.11.2`.

| Variable | Default | Meaning |
|----------|---------|---------|
| `OLLAMA_URL` | `http://host.docker.internal:11434` | Bridge → native Ollama |
| `OLLAMA_PORT` | `11434` | Host listen port (health checks, scripts) |
| `BRIDGE_MCP_CONFIG` | `bridge-config.json` | MCP in Docker vs host |

## Security

- Keep **11434** (Ollama) and **8081** (MCP) on localhost / private network only.
- Only the **bridge** (8082) should be reached via the frontend proxy path.
- Chat is **admin-only** in the UI; MCP uses the **machine service account** (admin API scope).

## Smoke tests

```bash
curl -s http://127.0.0.1:11434/api/tags | head
curl -s http://127.0.0.1:8082/health
```
