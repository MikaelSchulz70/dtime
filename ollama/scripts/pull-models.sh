#!/usr/bin/env bash
# Pull Ollama models declared in ollama/models.manifest.yaml into native Ollama on the host.
#
# Usage:
#   ./ollama/scripts/pull-models.sh              # required models if missing
#   ./ollama/scripts/pull-models.sh --all        # all manifest models if missing
#   ./ollama/scripts/pull-models.sh --upgrade    # re-pull listed models (updates)
#   ./ollama/scripts/pull-models.sh --check      # exit 1 if required models missing
#   ./ollama/scripts/pull-models.sh --model qwen3
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
MANIFEST="${ROOT}/ollama/models.manifest.yaml"
LIB="${ROOT}/ollama/scripts/manifest_lib.py"
OLLAMA_HOST="${OLLAMA_HOST:-http://127.0.0.1:${OLLAMA_PORT:-11434}}"

MODE="required"
SINGLE_MODEL=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --all) MODE="all"; shift ;;
    --upgrade) MODE="upgrade"; shift ;;
    --check) MODE="check"; shift ;;
    --model)
      SINGLE_MODEL="${2:?--model requires a name}"
      MODE="single"
      shift 2
      ;;
    -h|--help)
      sed -n '2,9p' "$0"
      exit 0
      ;;
    *) echo "Unknown option: $1" >&2; exit 2 ;;
  esac
done

if [[ ! -f "$MANIFEST" ]]; then
  echo "ERROR: missing $MANIFEST" >&2
  exit 1
fi

ensure_ollama_cli() {
  if ! command -v ollama >/dev/null 2>&1; then
    echo "ERROR: ollama CLI not found. Install: brew install ollama (Mac) or https://ollama.com/download" >&2
    exit 1
  fi
}

ensure_ollama_running() {
  if ! curl -sf "${OLLAMA_HOST}/api/tags" >/dev/null 2>&1; then
    echo "ERROR: Ollama is not reachable at ${OLLAMA_HOST}." >&2
    echo "  Mac: brew services start ollama" >&2
    echo "  Or: ollama serve" >&2
    exit 1
  fi
}

list_models() {
  if [[ -n "$SINGLE_MODEL" ]]; then
    echo "$SINGLE_MODEL"
    return
  fi
  case "$MODE" in
    required) python3 "$LIB" "$MANIFEST" required ;;
    all|upgrade) python3 "$LIB" "$MANIFEST" all ;;
    check) python3 "$LIB" "$MANIFEST" required ;;
    single) echo "$SINGLE_MODEL" ;;
  esac
}

model_installed() {
  local name="$1"
  ollama list 2>/dev/null \
    | awk 'NR>1 {print $1}' \
    | grep -Eq "^${name}(:|$)"
}

pull_one() {
  local name="$1"
  echo "Pulling $name ..."
  ollama pull "$name"
}

ensure_ollama_cli
ensure_ollama_running

if [[ "$MODE" == "check" ]]; then
  missing=0
  while IFS= read -r name; do
    [[ -z "$name" ]] && continue
    if model_installed "$name"; then
      echo "OK: $name"
    else
      echo "MISSING: $name" >&2
      missing=1
    fi
  done < <(list_models)
  exit "$missing"
fi

while IFS= read -r name; do
  [[ -z "$name" ]] && continue
  if [[ "$MODE" == "upgrade" || "$MODE" == "single" ]]; then
    pull_one "$name"
  elif model_installed "$name"; then
    echo "Skip (already installed): $name"
  else
    pull_one "$name"
  fi
done < <(list_models)

echo "Done."
