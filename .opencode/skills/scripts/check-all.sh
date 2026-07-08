#!/usr/bin/env bash
set -euo pipefail

echo "═══════════════════════════════════════════════"
echo "  🔴 NoteScribe — Check All"
echo "═══════════════════════════════════════════════"
echo ""

cd "$(git rev-parse --show-toplevel)"
SCRIPT_DIR="$(dirname "$0")"
PASSED=0
FAILED=0

run_check() {
  local name="$1"
  local script="$2"
  echo "───────────────────────────────────────────────"
  echo "  [$name]"
  echo "───────────────────────────────────────────────"
  if bash "$script"; then
    echo "  ✅ $name PASSED"
    PASSED=$((PASSED + 1))
  else
    echo "  ❌ $name FAILED"
    FAILED=$((FAILED + 1))
  fi
  echo ""
}

run_check "Rust Quality" "$SCRIPT_DIR/check-rust-quality.sh"
run_check "Kotlin Quality" "$SCRIPT_DIR/check-kotlin-quality.sh"
run_check "Architecture" "$SCRIPT_DIR/check-architecture.sh"

echo "═══════════════════════════════════════════════"
echo "  📊 Results: $PASSED passed, $FAILED failed"
echo "═══════════════════════════════════════════════"

if [ "$FAILED" -gt 0 ]; then
  exit 1
fi
