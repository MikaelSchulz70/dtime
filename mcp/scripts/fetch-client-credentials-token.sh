#!/usr/bin/env bash
# Request an OAuth2 access token (client_credentials) from Authentik. Prints raw JSON to stdout.
# Uses MCP_OAUTH_TOKEN_URI, MCP_OAUTH_CLIENT_ID, MCP_OAUTH_CLIENT_SECRET, optional MCP_OAUTH_SCOPE.
# Optional: load repo-root .env when present (same variables as docker-compose / README).

set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
if [[ -f "$ROOT/.env" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ROOT/.env"
  set +a
fi

: "${MCP_OAUTH_TOKEN_URI:?Set MCP_OAUTH_TOKEN_URI (e.g. http://localhost:9000/application/o/token/)}"
: "${MCP_OAUTH_CLIENT_ID:?Set MCP_OAUTH_CLIENT_ID}"
: "${MCP_OAUTH_CLIENT_SECRET:?Set MCP_OAUTH_CLIENT_SECRET}"

ARGS=(
  -sS -X POST "${MCP_OAUTH_TOKEN_URI}"
  -H "Content-Type: application/x-www-form-urlencoded"
  --data-urlencode "grant_type=client_credentials"
  --data-urlencode "client_id=${MCP_OAUTH_CLIENT_ID}"
  --data-urlencode "client_secret=${MCP_OAUTH_CLIENT_SECRET}"
)
if [[ -n "${MCP_OAUTH_SCOPE:-}" ]]; then
  ARGS+=(--data-urlencode "scope=${MCP_OAUTH_SCOPE}")
fi

curl "${ARGS[@]}"
