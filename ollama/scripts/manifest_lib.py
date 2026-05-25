#!/usr/bin/env python3
"""Minimal parser for ollama/models.manifest.yaml (no PyYAML dependency)."""

from __future__ import annotations

import json
import re
import sys
from pathlib import Path


def _strip_quotes(value: str) -> str:
    value = value.strip()
    if (value.startswith('"') and value.endswith('"')) or (
        value.startswith("'") and value.endswith("'")
    ):
        return value[1:-1]
    return value


def load_manifest(path: Path) -> dict:
    text = path.read_text(encoding="utf-8")
    result: dict = {"models": []}
    current: dict | None = None

    for raw_line in text.splitlines():
        line = raw_line.split("#", 1)[0].rstrip()
        if not line.strip():
            continue

        if re.match(r"^\s*-\s+name:", line):
            if current:
                result["models"].append(current)
            name = _strip_quotes(line.split(":", 1)[1])
            current = {"name": name}
            continue

        if current is not None and re.match(r"^\s+\w+:", line):
            key, value = line.strip().split(":", 1)
            value = value.strip()
            if value in ("true", "false"):
                current[key] = value == "true"
            else:
                current[key] = _strip_quotes(value)
            continue

        if re.match(r"^\w+:", line):
            key, value = line.split(":", 1)
            key = key.strip()
            value = value.strip()
            if key == "models" and not value:
                result["models"] = []
                current = None
            elif value in ("true", "false"):
                result[key] = value == "true"
            else:
                result[key] = _strip_quotes(value)
            continue

    if current:
        result["models"].append(current)

    return result


def main() -> None:
    if len(sys.argv) < 3:
        print("Usage: manifest_lib.py <manifest.yaml> <command>", file=sys.stderr)
        print("Commands: json | names | required | all", file=sys.stderr)
        sys.exit(2)

    manifest_path = Path(sys.argv[1])
    command = sys.argv[2]
    data = load_manifest(manifest_path)

    if command == "json":
        print(json.dumps(data, indent=2))
    elif command == "names":
        for model in data.get("models", []):
            print(model["name"])
    elif command == "required":
        for model in data.get("models", []):
            if model.get("required"):
                print(model["name"])
    elif command == "all":
        for model in data.get("models", []):
            print(model["name"])
    else:
        print(f"Unknown command: {command}", file=sys.stderr)
        sys.exit(2)


if __name__ == "__main__":
    main()
