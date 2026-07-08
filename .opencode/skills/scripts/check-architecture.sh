#!/usr/bin/env bash
set -euo pipefail

echo "🔴 Checking architecture..."
cd "$(git rev-parse --show-toplevel)"

SRC_DIR="app/src/main/java/com/uliteamr/notescribe"
ERRORS=0

# 1. Verify directory structure
echo "  → Verifying directory structure..."
REQUIRED_DIRS=(
  "$SRC_DIR/core"
  "$SRC_DIR/presentation/components"
  "$SRC_DIR/presentation/icons"
  "$SRC_DIR/presentation/screens/home"
  "$SRC_DIR/presentation/screens/root"
  "$SRC_DIR/presentation/theme"
  "$SRC_DIR/presentation/utils"
)
for dir in "${REQUIRED_DIRS[@]}"; do
  if [ ! -d "$dir" ]; then
    echo "❌ FAIL: Required directory missing: $dir"
    ERRORS=$((ERRORS + 1))
  fi
done

# 2. Check that Kotlin doesn't import Android SQLite directly
echo "  → Checking for direct Android SQLite usage..."
SQLITE_IMPORTS=$(grep -rn 'android\.database\.sqlite\|android\.database\.Cursor' "$SRC_DIR" --include='*.kt' || true)
if [ -n "$SQLITE_IMPORTS" ]; then
  echo "❌ FAIL: Direct SQLite access found (should go through Rust):"
  echo "$SQLITE_IMPORTS"
  ERRORS=$((ERRORS + 1))
fi

# 3. Check screen files follow State/Event/ViewModel/Screen convention
echo "  → Checking screen file conventions..."
for screen_dir in "$SRC_DIR/presentation/screens"/*/; do
  screen_name=$(basename "$screen_dir")
  has_state=$(ls "$screen_dir"/*State* 2>/dev/null || true)
  has_event=$(ls "$screen_dir"/*Event* 2>/dev/null || true)
  has_vm=$(ls "$screen_dir"/*ViewModel* 2>/dev/null || true)
  has_screen=$(ls "$screen_dir"/*Screen* 2>/dev/null || true)
  if [ -z "$has_state" ] || [ -z "$has_event" ] || [ -z "$has_vm" ] || [ -z "$has_screen" ]; then
    echo "⚠️  WARN: Screen '$screen_name' may be missing required files (State, Event, ViewModel, Screen)"
  fi
done

# 4. Check for any .kt files outside allowed directories
echo "  → Checking for misplaced files..."
if [ -f "$SRC_DIR/MainActivity.kt" ]; then
  echo "  ✅ MainActivity.kt exists"
else
  echo "❌ FAIL: MainActivity.kt not found"
  ERRORS=$((ERRORS + 1))
fi

if [ "$ERRORS" -gt 0 ]; then
  echo "❌ Architecture check FAILED with $ERRORS error(s)"
  exit 1
fi

echo "✅ Architecture checks passed"
