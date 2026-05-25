#!/usr/bin/env bash
# Start ollama-mcp-bridge in Docker.
#
# Backend on host (IntelliJ dev profile, ./start-backend.sh): detected automatically — starts
# dtime-mcp + bridge only; MCP calls https://host.docker.internal:8443.
#
# Backend in Docker: when nothing healthy on :8443, starts dtime-backend + dtime-mcp (MCP uses
# https://dtime-backend:8443 on the compose network).
#
# CHAT_BACKEND=host|docker|auto (default auto):
#   auto  — use host backend if :8443 is healthy, else start Docker backend
#   host  — require IntelliJ/host backend; never start dtime-backend container
#   docker — always start/use Docker backend
#
# Optional host MCP (IntelliJ / mvn on :8081):
#   BRIDGE_MCP_CONFIG=bridge-config.host-mcp.json ./start-ollama-bridge.sh
#
# Native Ollama on :11434 is required for Chat (brew services start ollama).
set -euo pipefail
cd "$(dirname "$0")"

OLLAMA_HOST="${OLLAMA_HOST:-http://127.0.0.1:${OLLAMA_PORT:-11434}}"
BACKEND_HEALTH_URL="${BACKEND_HEALTH_URL:-https://localhost:8443/actuator/health}"
CHAT_BACKEND="${CHAT_BACKEND:-auto}"

export BRIDGE_MCP_CONFIG="${BRIDGE_MCP_CONFIG:-bridge-config.json}"

if ! curl -sf "${OLLAMA_HOST}/api/tags" >/dev/null 2>&1; then
  echo "WARNING: Ollama not reachable at ${OLLAMA_HOST}. Chat will fail until Ollama is started." >&2
  echo "  brew services start ollama" >&2
fi

host_backend_healthy() {
  curl -sfk "${BACKEND_HEALTH_URL}" >/dev/null 2>&1
}

docker_backend_container() {
  docker ps -a --format '{{.Names}}' 2>/dev/null | grep -qx 'dtime-backend'
}

start_bridge() {
  # Bridge registers MCP tools only at startup — wait for MCP before starting it.
  echo "Waiting for dtime-mcp health..."
  for _ in $(seq 1 60); do
    if docker inspect dtime-mcp --format '{{.State.Health.Status}}' 2>/dev/null | grep -qx healthy; then
      break
    fi
    sleep 2
  done
  docker compose --profile ollama up -d --force-recreate dtime-ollama-bridge
}

if [[ "$BRIDGE_MCP_CONFIG" != "bridge-config.json" ]]; then
  echo "Using ${BRIDGE_MCP_CONFIG} — MCP must already run on host :8081."
  if ! curl -sf http://127.0.0.1:8081/actuator/health >/dev/null 2>&1; then
    echo "ERROR: MCP is not reachable at http://127.0.0.1:8081/actuator/health" >&2
    exit 1
  fi
  start_bridge
  echo "Bridge: curl -s http://127.0.0.1:8082/health"
  exit 0
fi

use_host_backend=false
start_docker_backend=false

case "$CHAT_BACKEND" in
  host)
    if host_backend_healthy; then
      use_host_backend=true
    else
      echo "ERROR: CHAT_BACKEND=host but backend is not healthy at ${BACKEND_HEALTH_URL}." >&2
      echo "  Start dtime-backend in IntelliJ (dev profile) or run ./start-backend.sh" >&2
      exit 1
    fi
    ;;
  docker)
    start_docker_backend=true
    export MCP_DOCKER_BACKEND_URL="https://dtime-backend:8443"
    ;;
  auto)
    if host_backend_healthy; then
      use_host_backend=true
    else
      start_docker_backend=true
      export MCP_DOCKER_BACKEND_URL="https://dtime-backend:8443"
    fi
    ;;
  *)
    echo "ERROR: CHAT_BACKEND must be host, docker, or auto (got: ${CHAT_BACKEND})" >&2
    exit 1
    ;;
esac

if [[ "$use_host_backend" == true ]]; then
  export MCP_DOCKER_BACKEND_URL="https://host.docker.internal:8443"
  echo "Backend on host (${BACKEND_HEALTH_URL}) — starting dtime-mcp → ${MCP_DOCKER_BACKEND_URL}"
  if docker_backend_container; then
    echo "Note: dtime-backend container exists but host :8443 is healthy; MCP uses host.docker.internal (IntelliJ/dev)."
  fi
  docker compose --profile ollama up -d --force-recreate dtime-mcp
else
  echo "Starting dtime-backend + dtime-mcp (Docker) — MCP → ${MCP_DOCKER_BACKEND_URL}"
  docker compose --profile ollama up -d dtime-backend
  docker compose --profile ollama up -d --force-recreate dtime-mcp
fi

start_bridge
echo "Bridge: curl -s http://127.0.0.1:8082/health"
