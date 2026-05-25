#!/usr/bin/env bash
# Start admin Chat stack: native Ollama + MCP + bridge in Docker.
#
# Prerequisites: Postgres (cd infra && docker compose up -d postgres), .env with MCP_OAUTH_* configured.
# Ollama: brew install ollama && brew services start ollama
set -euo pipefail
cd "$(dirname "$0")"

OLLAMA_HOST="${OLLAMA_HOST:-http://127.0.0.1:${OLLAMA_PORT:-11434}}"

ensure_native_ollama() {
  if curl -sf "${OLLAMA_HOST}/api/tags" >/dev/null 2>&1; then
    return 0
  fi
  echo "ERROR: Ollama is not reachable at ${OLLAMA_HOST}." >&2
  echo "  Install: brew install ollama" >&2
  echo "  Start:   brew services start ollama   (or: ollama serve)" >&2
  exit 1
}

wait_for_tools() {
  echo "Waiting for bridge MCP tools (up to 3 min)..."
  for _ in $(seq 1 90); do
    if health="$(curl -sf http://127.0.0.1:8082/health 2>/dev/null)"; then
      tools="$(printf '%s' "$health" | python3 -c "import sys,json; print(json.load(sys.stdin).get('tools',0))" 2>/dev/null || echo 0)"
      if [[ "${tools:-0}" -gt 0 ]]; then
        echo "Bridge ready: ${tools} MCP tools."
        return 0
      fi
    fi
    sleep 2
  done
  return 1
}

ensure_native_ollama
./start-ollama-bridge.sh

echo "Ensuring required Ollama models (see ollama/models.manifest.yaml)..."
if ! ./ollama/scripts/pull-models.sh; then
  echo "WARNING: some required models could not be pulled. Check Ollama is running and try: ollama pull llama3.2" >&2
fi

if wait_for_tools; then
  exit 0
fi

echo "ERROR: bridge health still reports tools=0." >&2
echo "  curl -s http://127.0.0.1:8082/health" >&2
echo "  docker compose --profile ollama logs dtime-mcp --tail 80" >&2
echo "  docker compose --profile ollama logs dtime-ollama-bridge --tail 80" >&2
exit 1
