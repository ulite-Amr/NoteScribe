#!/usr/bin/env bash
set -euo pipefail

echo "🔴 Checking Rust code quality..."
cd "$(git rev-parse --show-toplevel)"

# 1. Run clippy with warnings as errors
echo "  → cargo clippy..."
cargo clippy --all-targets -- -D warnings 2>&1 | tail -5

# 2. Check for unwrap/expect in non-test files
echo "  → Checking unwrap/expect in production code..."
UNWRAP_COUNT=$(grep -rn '\.unwrap()' notescribe-core/src/ \
  --include='*.rs' \
  --exclude='*test*' \
  --exclude='*tests*' \
  | grep -v '// SAFETY:' \
  | wc -l)
if [ "$UNWRAP_COUNT" -gt 0 ]; then
  echo "❌ FAIL: $UNWRAP_COUNT unwrap() calls found in production code"
  grep -rn '\.unwrap()' notescribe-core/src/ --include='*.rs' --exclude='*test*' --exclude='*tests*' | grep -v '// SAFETY:'
  exit 1
fi

# 3. Check for unsafe without SAFETY comment
echo "  → Checking unsafe usage..."
UNSAFE_COUNT=$(grep -rn 'unsafe' notescribe-core/src/ \
  --include='*.rs' \
  | grep -v '// SAFETY:' \
  | grep -v 'Cargo.toml' \
  | wc -l)
if [ "$UNSAFE_COUNT" -gt 0 ]; then
  echo "❌ FAIL: $UNSAFE_COUNT unsafe blocks without // SAFETY:"
  grep -rn 'unsafe' notescribe-core/src/ --include='*.rs' | grep -v '// SAFETY:'
  exit 1
fi

# 4. Check for todo! and unimplemented!
echo "  → Checking for todo!/unimplemented!..."
TODO_COUNT=$(grep -rn 'todo!()\|unimplemented!()' notescribe-core/src/ \
  --include='*.rs' \
  | grep -v '#\[cfg(test)\]' \
  | wc -l)
if [ "$TODO_COUNT" -gt 0 ]; then
  echo "❌ FAIL: $TODO_COUNT todo!/unimplemented!() in production code"
  grep -rn 'todo!()\|unimplemented!()' notescribe-core/src/ --include='*.rs' | grep -v '#\[cfg(test)\]'
  exit 1
fi

echo "✅ Rust quality checks passed"
