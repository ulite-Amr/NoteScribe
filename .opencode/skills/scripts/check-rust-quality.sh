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
UNSAFE_COUNT=$(python3 -c "
import os
count = 0
for root, dirs, files in os.walk('notescribe-core/src'):
    for f in files:
        if f.endswith('.rs'):
            path = os.path.join(root, f)
            with open(path) as fh:
                lines = fh.readlines()
            for i, line in enumerate(lines):
                if 'unsafe' in line and '// SAFETY:' not in line:
                    if i == 0 or '// SAFETY:' not in lines[i - 1]:
                        count += 1
print(count)
")
if [ "$UNSAFE_COUNT" -gt 0 ]; then
  echo "❌ FAIL: $UNSAFE_COUNT unsafe blocks without // SAFETY:"
  grep -rn 'unsafe' notescribe-core/src/ --include='*.rs' | while IFS=: read -r file num rest; do
    if [[ "$rest" != *"// SAFETY:"* ]]; then
      if [[ "$num" -gt 1 ]]; then
        prev=$(sed -n "$((num - 1))p" "$file")
        if [[ "$prev" == *"// SAFETY:"* ]]; then
          continue
        fi
      fi
      echo "$file:$num:$rest"
    fi
  done
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
