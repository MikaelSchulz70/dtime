#!/usr/bin/env bash
# Decode JWT access_token payload (second segment) and print JSON. Highlights client_id, azp, sub for backend allowlist.
# Usage: ACCESS_TOKEN=... ./print-access-token-claims.sh
#    or: ./print-access-token-claims.sh '<jwt>'

set -euo pipefail
TOKEN="${1:-${ACCESS_TOKEN:-}}"
if [[ -z "${TOKEN}" ]]; then
  echo "Usage: ACCESS_TOKEN=<jwt> $0   or   $0 <jwt>" >&2
  exit 1
fi

python3 - "$TOKEN" <<'PY'
import json, sys, base64
token = sys.argv[1]
parts = token.split(".")
if len(parts) < 2:
    sys.exit("Not a JWT (expected at least header.payload)")
def b64url_decode(segment: str) -> bytes:
    pad = "=" * (-len(segment) % 4)
    return base64.urlsafe_b64decode(segment + pad)
payload = json.loads(b64url_decode(parts[1]))
print(json.dumps(payload, indent=2))
print("\n--- backend allowlist (match first non-blank of these claims) ---", flush=True)
for k in ("client_id", "azp", "sub"):
    v = payload.get(k)
    if v:
        print(f"  {k}={v}")
PY
