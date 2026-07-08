#!/usr/bin/env bash
set -euo pipefail

echo "═══════════════════════════════════════════════"
echo "  🔴 NoteScribe — Full Strict Review Suite"
echo "═══════════════════════════════════════════════"
echo ""

cd "$(git rev-parse --show-toplevel)"
REPORT_DIR="/tmp/notescribe-review-$(date +%Y%m%d-%H%M%S)"
mkdir -p "$REPORT_DIR"
PASSED=0
FAILED=0

run_check() {
  local name="$1"
  local script="$2"
  echo "───────────────────────────────────────────────"
  echo "  [$name]"
  echo "───────────────────────────────────────────────"
  if bash "$script" 2>&1 | tee "$REPORT_DIR/${name//\//_}.log"; then
    echo "  ✅ $name PASSED"
    PASSED=$((PASSED + 1))
  else
    echo "  ❌ $name FAILED (see $REPORT_DIR/${name//\//_}.log)"
    FAILED=$((FAILED + 1))
  fi
  echo ""
}

SCRIPT_DIR="$(dirname "$0")"

run_check "Rust Quality" "$SCRIPT_DIR/check-rust-quality.sh"
run_check "Kotlin Quality" "$SCRIPT_DIR/check-kotlin-quality.sh"
run_check "Architecture" "$SCRIPT_DIR/check-architecture.sh"
run_check "Tests" "$SCRIPT_DIR/run-all-tests.sh"

echo "═══════════════════════════════════════════════"
echo "  📊 Results: $PASSED passed, $FAILED failed"
echo "  📁 Report: $REPORT_DIR"
echo "═══════════════════════════════════════════════"

if [ "$FAILED" -gt 0 ]; then
  exit 1
fi
