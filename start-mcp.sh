#!/bin/bash
# Quick reference for running dtime-mcp (requires Authentik client_credentials + backend machine JWT enabled).

set -e

BACKEND_HEALTH_URL="${BACKEND_HEALTH_URL:-https://localhost:8443/actuator/health}"

echo ""
echo "dtime-mcp — Prerequisites (see mcp/README.md and README.md#running-the-stack)"
echo "------------------------------------------------------------------------------"
echo "1. dtime-backend running with Authentik OAuth (typically https://localhost:8443)."
echo "2. In Authentik: confidential OAuth client for MCP using client_credentials; note client id UUID."
echo "3. Backend: oauth.authentik.machine-jwt.enabled=true and oauth.authentik.machine-jwt.authorized-client-ids including that client id."
echo ""

if curl -s -k -f "$BACKEND_HEALTH_URL" >/dev/null 2>&1; then
  echo "Backend health OK ($BACKEND_HEALTH_URL)"
else
  echo "Warning: Backend not reachable at $BACKEND_HEALTH_URL (ignored if intentional)."
fi

echo ""
echo "Typical Gradle/Maven env for local run:"
echo "  MCP_BACKEND_URL=https://localhost:8443"
echo "  MCP_OAUTH_TOKEN_URI=http://localhost:9000/application/o/token/   # your Authentik token URL"
echo "  MCP_OAUTH_CLIENT_ID=<authentik-mcp-client-uuid>"
echo "  MCP_OAUTH_CLIENT_SECRET=<secret>"
echo "  MCP_VALIDATE_ON_STARTUP=true"
echo ""
echo "Run: cd mcp && mvn spring-boot:run"
echo "  (profile dev: backend https://localhost:8443, token URL http://localhost:9000/application/o/token/ — same as backend application-dev.yml)"
echo "  Optional: SERVER_PORT=9081 mvn spring-boot:run  # if 8081 is taken (avoid 8443, 3000, 8080 when backend prod HTTP is on)"
echo ""
echo "After Authentik + backend machine JWT are configured, smoke test (from repo root with .env or exports):"
echo "  mcp/scripts/mcp-smoke-test.sh"
echo ""
MCP_PORT_DISPLAY="${SERVER_PORT:-8081}"
echo "MCP endpoint (STREAMABLE HTTP): POST http://localhost:${MCP_PORT_DISPLAY}/mcp — see Spring AI MCP client docs."
