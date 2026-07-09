#!/usr/bin/env bash
set -euo pipefail

echo "🔴 Checking Kotlin code quality..."
cd "$(git rev-parse --show-toplevel)"

SRC_DIR="app/src/main/java/com/uliteamr/notescribe"

# 1. Check for wildcard imports
echo "  → Checking wildcard imports..."
WILDCARDS=$(grep -rn 'import .*\.\*' "$SRC_DIR" --include='*.kt' || true)
if [ -n "$WILDCARDS" ]; then
  echo "❌ FAIL: Wildcard imports found:"
  echo "$WILDCARDS"
  exit 1
fi

# 2. Check for sealed class in Event files
echo "  → Checking Event files for sealed class..."
SEALED_EVENTS=$(grep -rn '^sealed class' "$SRC_DIR" --include='*Event*.kt' || true)
if [ -n "$SEALED_EVENTS" ]; then
  echo "❌ FAIL: sealed class found in Event files (must be sealed interface):"
  echo "$SEALED_EVENTS"
  exit 1
fi

# 3. Check for com.example package
echo "  → Checking for wrong package..."
EXAMPLE_PKG=$(grep -rn 'com\.example' "$SRC_DIR" --include='*.kt' || true)
if [ -n "$EXAMPLE_PKG" ]; then
  echo "❌ FAIL: com.example package found:"
  echo "$EXAMPLE_PKG"
  exit 1
fi

# 4. Check for hardcoded dp/sp for text
echo "  → Checking for hardcoded fontSize..."
FONTSIZE=$(grep -rn 'fontSize\s*=' "$SRC_DIR" --include='*.kt' || true)
if [ -n "$FONTSIZE" ]; then
  echo "⚠️  WARN: fontSize= found (should use MaterialTheme.typography):"
  echo "$FONTSIZE"
fi

# 5. Check for hardcoded colors
echo "  → Checking for hardcoded Color(...)..."
HARDCOLOR=$(grep -rn 'Color(0x' "$SRC_DIR" --include='*.kt' | grep -v 'theme/Color.kt' | head -5 || true)
if [ -n "$HARDCOLOR" ]; then
  echo "⚠️  WARN: Hardcoded colors outside theme/Color.kt:"
  echo "$HARDCOLOR"
fi

# 6. Check strings.xml has section dividers (GLOBAL + at least one screen)
echo "  → Checking strings.xml organization..."
STRINGS_FILE="app/src/main/res/values/strings.xml"
if [ -f "$STRINGS_FILE" ]; then
  GLOBAL_SECTION=$(grep -c 'GLOBAL' "$STRINGS_FILE" || true)
  SCREEN_SECTIONS=$(grep -c '=====' "$STRINGS_FILE" || true)
  if [ "$GLOBAL_SECTION" -eq 0 ] || [ "$SCREEN_SECTIONS" -lt 2 ]; then
    echo "⚠️  WARN: strings.xml missing section dividers. Add <!-- ===== GLOBAL ===== --> and per-screen sections."
  fi
fi

# 8. Run Android lint if available

# 6. Run Android lint if available
if command -v ./gradlew &> /dev/null; then
  echo "  → Running Android lint..."
  if ! ./gradlew lint 2>&1 | tail -10; then
    echo "❌ FAIL: Android lint found issues"
    exit 1
  fi
fi

echo "✅ Kotlin quality checks passed"
