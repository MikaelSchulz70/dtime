#!/bin/bash
# Run dtime-mcp locally (dev profile). Loads repo-root .env / .env.local like start-backend.sh.

set -e

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

BACKEND_HEALTH_URL="${BACKEND_HEALTH_URL:-https://localhost:8443/actuator/health}"

set -a
if [ -f ".env" ]; then
  print_status "Loading .env"
  source .env
fi
if [ -f ".env.local" ]; then
  print_status "Loading .env.local"
  source .env.local
fi
set +a

# Local mvn defaults (docker-oriented vars in .env.example are overridden here when unset)
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-dev}"
export MCP_BACKEND_URL="${MCP_BACKEND_URL:-https://localhost:8443}"
export MCP_OAUTH_TOKEN_URI="${MCP_OAUTH_TOKEN_URI:-http://localhost:9000/application/o/token/}"
export MCP_BACKEND_TRUST_INSECURE_SSL="${MCP_BACKEND_TRUST_INSECURE_SSL:-true}"

if [ -z "${MCP_OAUTH_CLIENT_ID:-}" ] || [ -z "${MCP_OAUTH_CLIENT_SECRET:-}" ]; then
  print_error "MCP_OAUTH_CLIENT_ID and MCP_OAUTH_CLIENT_SECRET must be set (see .env.example)."
  exit 1
fi

if [ -z "${OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS:-}" ]; then
  export OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS="${MCP_OAUTH_CLIENT_ID}"
  print_status "OAUTH_AUTHENTIK_MACHINE_JWT_AUTHORIZED_CLIENT_IDS not set — using MCP_OAUTH_CLIENT_ID for backend allowlist hint"
  print_warning "Restart dtime-backend with the same .env so machine JWT allowlist includes: ${MCP_OAUTH_CLIENT_ID}"
fi

if curl -s -k -f "$BACKEND_HEALTH_URL" >/dev/null 2>&1; then
  print_status "Backend health OK ($BACKEND_HEALTH_URL)"
else
  print_warning "Backend not reachable at $BACKEND_HEALTH_URL — start it first (./start-backend.sh)"
fi

MCP_PORT_DISPLAY="${SERVER_PORT:-8081}"
print_status "Starting MCP (profile ${SPRING_PROFILES_ACTIVE}, backend ${MCP_BACKEND_URL}, port ${MCP_PORT_DISPLAY})"
print_status "Smoke test after startup: mcp/scripts/mcp-smoke-test.sh"

cd mcp
exec mvn -q spring-boot:run
