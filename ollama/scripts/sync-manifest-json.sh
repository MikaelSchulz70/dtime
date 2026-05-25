#!/usr/bin/env bash
# Sync ollama/models.manifest.yaml -> frontend/public/ollama-models.manifest.json
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
MANIFEST="${ROOT}/ollama/models.manifest.yaml"
OUT="${ROOT}/frontend/public/ollama-models.manifest.json"
LIB="${ROOT}/ollama/scripts/manifest_lib.py"

if [[ ! -f "$MANIFEST" ]]; then
  echo "ERROR: missing $MANIFEST" >&2
  exit 1
fi

python3 "$LIB" "$MANIFEST" json > "$OUT"
echo "Wrote $OUT"
