# Ollama + MCP bridge (DTime)

Local LLM (**Ollama**) plus **[ollama-mcp-bridge](https://github.com/jonigl/ollama-mcp-bridge)** so the admin Chat UI can call an Ollama-compatible API with **DTime MCP tools** attached.

## Compose layout

| File | Services |
|------|----------|
| [`docker-compose.yml`](docker-compose.yml) | `dtime-ollama` — run from **this directory** or via root `include` |
| [`docker-compose.bridge.yml`](docker-compose.bridge.yml) | `dtime-ollama-bridge` — merged from **repo root** only (needs `dtime-mcp`) |

The root [`docker-compose.yml`](../docker-compose.yml) includes both files so one command still starts the full stack.

## Architecture

| Service | Container | Host port | Purpose |
|---------|-----------|-----------|---------|
| Ollama | `dtime-ollama` | **11434** | Model runtime (`ollama pull …`) |
| MCP bridge | `dtime-ollama-bridge` | **8082** → container **8000** | `/api/chat` + MCP; other `/api/*` proxied to Ollama |
| MCP server | `dtime-mcp` | **8081** | Streamable HTTP at `/mcp` (defined in root compose) |

The frontend uses **`/api/ollama/...`** (webpack dev proxy or nginx) → bridge.

Bridge MCP config: [`bridge-config.json`](bridge-config.json) → `http://dtime-mcp:8081/mcp`.

## Memory and model choice

Ollama loads the whole model into RAM. If Chat returns an error like *“model requires more system memory (2.3 GiB) than is available (1.8 GiB)”*, the limit is almost always **Docker’s VM or free host RAM** — the `dtime-ollama` service in compose does **not** set a memory cap.

### Increase memory (recommended on a Mac with spare RAM)

Giving Docker more memory lets you run larger models (e.g. `llama3.2`) with better answers.

**Colima** (common on macOS):

```bash
colima stop
colima start --memory 8
```

Use `6` instead of `8` on machines with less physical RAM.

**Docker Desktop:** Settings → **Resources** → **Memory** (e.g. 8 GB) → Apply & Restart.

Then restart Ollama and pull the model you want:

```bash
docker restart dtime-ollama
docker exec dtime-ollama ollama pull llama3.2
```

Set `OLLAMA_DEFAULT_MODEL=llama3.2` in the repo `.env` and restart `npm start`, or type the model name in the Chat UI.

### Use a smaller model (when RAM stays ~2 GiB)

If you cannot grow Docker’s memory, use a smaller tag instead of the default `llama3.2`:

```bash
docker exec dtime-ollama ollama pull llama3.2
```

Other low-footprint options: `phi3:mini`, `qwen2.5:0.5b`, etc. (after `ollama pull <name>`).

### What to choose

| Situation | Suggestion |
|-----------|------------|
| Mac with 16+ GiB RAM, LLM used often | Increase Docker to **6–8 GiB**, use **`llama3.2`** |
| Docker VM ~2 GiB, or many containers running | Keep Docker size as-is, use **`llama3.2:1b`** or similar |
| Error in Chat UI | Ollama errors are shown in the red alert; fix RAM or switch model |

You can also free memory by stopping unused containers (`docker ps`, stop what you do not need) before starting Chat.

## Start Ollama only (this directory)

```bash
cd ollama
docker compose --profile ollama up -d
docker exec dtime-ollama ollama pull llama3.2
```

## Chat dev (frontend on host, Ollama + bridge in Docker)

When the backend/MCP run **locally** (`mvn spring-boot:run` on :8081) and Postgres is already on host :5432:

```bash
# From repo root
docker compose --profile ollama up -d dtime-ollama
BRIDGE_MCP_CONFIG=bridge-config.host-mcp.json docker compose --profile ollama up -d dtime-ollama-bridge
```

Then restart frontend: `cd frontend && npm start` → admin **Chat** at `/chat`.

Bridge health: `curl http://127.0.0.1:8082/health`

## Full stack (repo root)

Backend, MCP, frontend, Ollama, and bridge:

```bash
cd /path/to/dtime
docker compose --profile full-stack --profile mcp --profile ollama up -d
docker exec dtime-ollama ollama pull llama3.2
```

If compose `dtime-db` fails on port **5432**, another Postgres is already running. Either stop it or set `DATABASE_HOST_PORT=5433` in `.env` (see root `.env.example`). For chat-only dev you can skip `dtime-db` and use the **host MCP** flow above.

If `dtime-ollama` is missing, the stack did not finish starting. Check:

```bash
docker ps -a --filter name=dtime-ollama
docker compose --profile full-stack --profile mcp --profile ollama ps -a
```

Services use profiles **`ollama`**, **`full-stack`**, and **`mcp`**.

## Smoke tests

```bash
curl -s http://localhost:11434/api/tags | head
curl -s http://localhost:8082/health
```

## Environment variables

See root [`.env.example`](../.env.example) (`OLLAMA_*`, `OLLAMA_BRIDGE_*`). Bridge image: `ghcr.io/jonigl/ollama-mcp-bridge:v0.11.2`.

## Security

- Keep **11434** (Ollama) and **8081** (MCP) on localhost / Docker network only.
- Only the **bridge** (8082) should be reached via the frontend proxy path.
- Chat is **admin-only** in the UI; MCP uses the **machine service account** (admin API scope).
