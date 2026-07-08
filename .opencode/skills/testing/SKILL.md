---
name: testing
description: >
  Use when writing, running, or fixing tests in NoteScribe. Covers all test
  layers: Rust unit/integration, Kotlin unit (JUnit 4 + coroutines), and
  Compose UI tests (androidTest). Not for general feature development.
---

# 🔴 Testing in NoteScribe

## Hard Rules

- **Every public fn needs success + error test** — no exceptions
- **Test names are descriptive sentences** — no `test1`, no `//` comments
- **NO external state** — DB = in-memory, files = temp dirs
- **Mock only at UniFFI boundary** — never mock internal Rust
- **Edge cases required**: empty, wrong password, missing IDs, max-length, concurrent, special chars
- **Tests exempt from file/function length limits** — all other quality rules still apply

## Reference Documents

- `references/rust-testing.md` — Inline `#[cfg(test)]`, in-memory DB, crypto round-trip, proptest fuzzing, thread safety
- `references/kotlin-unit-testing.md` — JUnit 4, `runTest`, ViewModel + StateFlow testing, UniFFI mocking
- `references/ui-testing.md` — Compose UI Test, `createComposeRule`, interaction tests, dark mode, adaptive breakpoints

## Running Tests

```bash
cargo test                              # All Rust tests
./gradlew test                           # Kotlin unit tests
./gradlew connectedAndroidTest           # Instrumented UI tests
.opencode/skills/scripts/run-all-tests.sh  # All of the above
```

## Naming

```rust
// Rust
#[test]
fn test_create_note_assigns_uuid_and_timestamp() { }
```

```kotlin
// Kotlin
@Test
fun `initial state is loading`() { }
```

## Coverage

- Every ViewModel state: loading, success, error, empty
- Every composable: renders, interacts, responds to state
- SQL: zero rows, one row, multiple rows
- Encryption: round-trip, wrong password, empty, binary, format length check
