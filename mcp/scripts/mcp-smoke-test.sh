#!/usr/bin/env bash
# End-to-end smoke: client_credentials token → print claims → GET backend /api with Bearer → MCP actuator health.
# Requires: backend reachable, machine JWT enabled, client id on allowlist, Python 3.
# Optional: repo-root .env with MCP_* and OAUTH_* variables.

set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
if [[ -f "$ROOT/.env" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ROOT/.env"
  set +a
fi

: "${MCP_OAUTH_TOKEN_URI:?Set MCP_OAUTH_TOKEN_URI}"
: "${MCP_OAUTH_CLIENT_ID:?Set MCP_OAUTH_CLIENT_ID}"
: "${MCP_OAUTH_CLIENT_SECRET:?Set MCP_OAUTH_CLIENT_SECRET}"

BACKEND="${MCP_BACKEND_URL:-https://localhost:8443}"
MCP_BASE="${MCP_HTTP_BASE:-http://localhost:${SERVER_PORT:-${MCP_PORT:-8081}}}"

echo "== 1) Token (client_credentials) =="
RESP="$("${SCRIPT_DIR}/fetch-client-credentials-token.sh")"
echo "$RESP" | python3 -m json.tool 2>/dev/null || echo "$RESP"
TOKEN="$(echo "$RESP" | python3 -c "import json,sys; print(json.load(sys.stdin).get('access_token',''))" 2>/dev/null || true)"
if [[ -z "${TOKEN}" ]]; then
  echo "ERROR: no access_token in response" >&2
  exit 1
fi

echo ""
echo "== 2) Access token claims (use for OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS) =="
ACCESS_TOKEN="${TOKEN}" "${SCRIPT_DIR}/print-access-token-claims.sh"

echo ""
echo "== 3) Backend API (Bearer) — ${BACKEND}/api/users/paged?page=0&size=1 =="
TMP="$(mktemp)"
code="$(curl -sS -o "${TMP}" -w "%{http_code}" -k -H "Authorization: Bearer ${TOKEN}" \
  "${BACKEND}/api/users/paged?page=0&size=1" || true)"
echo "HTTP ${code}"
head -c 400 "${TMP}" 2>/dev/null || true
echo ""
rm -f "${TMP}"

echo ""
echo "== 4) MCP actuator — ${MCP_BASE}/actuator/health =="
if curl -sS -f "${MCP_BASE}/actuator/health" | python3 -m json.tool 2>/dev/null; then
  :
else
  echo "(skipped or unreachable — start MCP: cd mcp && mvn spring-boot:run)"
fi

echo ""
echo "== 5) MCP streamable endpoint — POST ${MCP_BASE}/mcp =="
echo "Use your MCP HTTP client; actuator above is enough for infra smoke."
