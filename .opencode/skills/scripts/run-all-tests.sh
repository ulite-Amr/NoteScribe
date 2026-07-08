#!/usr/bin/env bash
set -euo pipefail

echo "🔴 Running all tests..."
cd "$(git rev-parse --show-toplevel)"
FAILED=0

# 1. Rust tests
echo "  → cargo test..."
if cargo test 2>&1; then
  echo "  ✅ Rust tests passed"
else
  echo "  ❌ Rust tests failed"
  FAILED=$((FAILED + 1))
fi

# 2. Kotlin unit tests
echo "  → ./gradlew test..."
if ./gradlew test 2>&1 | tail -5; then
  echo "  ✅ Kotlin unit tests passed"
else
  echo "  ❌ Kotlin unit tests failed"
  FAILED=$((FAILED + 1))
fi

# 3. Android instrumented tests (if device/emulator available)
if command -v adb &> /dev/null && adb get-state 1>/dev/null 2>&1; then
  echo "  → ./gradlew connectedCheck..."
  if ./gradlew connectedCheck 2>&1 | tail -5; then
    echo "  ✅ Instrumented tests passed"
  else
    echo "  ❌ Instrumented tests failed"
    FAILED=$((FAILED + 1))
  fi
else
  echo "  ⏭️  Skipping instrumented tests (no device/emulator)"
fi

# 4. Rust clippy (additional check)
echo "  → cargo clippy..."
if cargo clippy --all-targets -- -D warnings 2>&1 | tail -3; then
  echo "  ✅ Clippy passed"
else
  echo "  ❌ Clippy found issues"
  FAILED=$((FAILED + 1))
fi

if [ "$FAILED" -gt 0 ]; then
  echo "❌ $FAILED test suite(s) failed"
  exit 1
fi

echo "✅ All tests passed"
