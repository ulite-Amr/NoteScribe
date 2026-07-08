# NoteScribe

## Architecture

Hybrid Android app with a Rust core library bridged via UniFFI.

```
┌─────────────────────────────────────┐
│  Kotlin / Jetpack Compose (UI)      │
│  MVI: State + Events + ViewModel    │
│  Adaptive: compact/medium/expanded  │
├─────────────────────────────────────┤
│  UniFFI-generated Kotlin bindings   │
│  (JNA → libnotescribe_core.so)      │
├─────────────────────────────────────┤
│  Rust core (notescribe-core/)       │
│  SQLite, AES-256-GCM, Argon2        │
└─────────────────────────────────────┘
```

## Key paths

| Path | Purpose |
|---|---|
| `app/` | Android application (Kotlin/Compose) |
| `notescribe-core/` | Rust core library |
| `build-logic/` | Gradle convention plugins |
| `gradle/libs.versions.toml` | Dependency versions |

## Build commands

```bash
./gradlew :app:assembleDebug              # Build Android APK
./gradlew :app:assembleRelease            # Build release APK
./gradlew test                            # Run local unit tests
./gradlew connectedCheck                  # Run instrumented tests
cargo build                                # Build Rust core only
cargo test                                 # Run Rust tests
```

## 🔴 STRICT RULES — ALL CODE

### General (zero tolerance)

- **NO comments in production code.** No KDoc, no `//`, no `/* */`, no section dividers. Code must be self-documenting. KDoc only on public API functions and MUST stay updated.
- **NO commented-out code.** Delete it forever. Git history exists.
- **NO `TODO` comments.** Create a GitHub issue or implement now.
- **NO unused imports, variables, functions, parameters, or dependencies.**
- **NO wildcard imports (`*`).** Every symbol imported explicitly.
- **NO hardcoded secrets, tokens, or keys.**
- **NO magic numbers.** All constants named with `const`.
- **MAX 2 consecutive blank lines.** Exactly one blank line at file end.
- **Function bodies MAX 30 lines.** Extract aggressively.
- **File length MAX 200 lines.** Longer files must be split.
- **MAX 2 levels of nesting.** Extract nested blocks into functions.
- **NO `data class` with >5 properties without grouping.**
- **Use `stringResource` for ALL user-visible strings.** No hardcoded text in composables.
- **`strings.xml` organized by screen** — GLOBAL section first, then per-screen with `<!-- ===== NAME ===== -->` dividers, keys prefixed by screen.
- **NO hardcoded `dp`/`sp` for text — use `MaterialTheme.typography`.**
- **NO hardcoded colors — use `MaterialTheme.colorScheme`.**
- **NO hardcoded URLs, endpoints, timeouts, delays, feature flags, or config values** — use `BuildConfig` or named `const`.

### Rust strict rules

- **NO `unwrap()` or `expect()` in production code.** Only in `#[cfg(test)]`.
- **NO `unsafe` without `// SAFETY:` justification.**
- **ALL public functions must be `#[uniffi::export]`** or `pub(crate)`.
- **ALL public types must derive `uniffi::Record`/`uniffi::Enum`** and `Debug`.
- **ALL SQL is parameterized** — never string-concatenated.
- **NO `pub` fields on non-FFI types.**
- **NO `as` casts for narrowing** — use `From`/`TryFrom`.
- **Zeroize all secrets** with `zeroize::Zeroizing`.
- **Encryption format is sacred:** `[salt(16)][nonce(12)][ciphertext]`.
- **NO `todo!()` or `unimplemented!()` in production** — return proper `Err`.
- **Binary size MUST NOT exceed 5MB compressed.**
- **Release profile: `lto = "fat"`, `codegen-units = 1`, `opt-level = "z"`.**
- **Run `cargo clippy -- -D warnings` before every commit.**

### Kotlin strict rules

- **`sealed interface` for Events** — NEVER `sealed class`.
- **State is `data class`** — no functions, no logic.
- **ViewModel exposes ONLY `val state: StateFlow<State>` + `fun onEvent(event: Event)`.**
- **ViewModel calls UniFFI on `Dispatchers.IO`** — never main thread.
- **NO business logic in composables.** Purely declarative.
- **NO hardcoded `dp`/`sp` for text** — use `MaterialTheme.typography`.
- **NO hardcoded colors** — use `MaterialTheme.colorScheme`.
- **Package must match project path** — `com.uliteamr.notescribe.*`, never `com.example.*`.
- **NO KDoc on composables or production code.**
- **NO `@Suppress` without justification.**

## Rust core conventions

- **DB**: SQLite via `rusqlite`, WAL mode, `synchronous=NORMAL`
- **Encryption**: AES-256-GCM (random 12-byte nonce) + Argon2 key derivation (random 16-byte salt) — output: `[salt(16)][nonce(12)][ciphertext]`
- **Models**: `Note` (full) and `NoteListItem` (lightweight, no content), both use `uniffi::Record` and serde
- **Error**: `NoteScribeError` enum with `Database`, `Encryption`, `WrongPassword`, `Io` variants
- **Exports**: All public Rust types/functions are exported via UniFFI. Add `#[uniffi::export]` to new functions.

## Kotlin UI conventions

- **MVI pattern**: Each screen has `{Name}State` (data class), `{Name}Event` (sealed interface), `{Name}ViewModel`
- **Adaptive breakpoints**: COMPACT (<600dp), MEDIUM (600-840dp), EXPANDED (>840dp) via `WindowSizeProvider`
- **Theme**: Material 3 light/dark with dynamic color (Android 12+)
- **Icons**: Custom `ImageVector` definitions in `presentation/icons/`
- **Package structure**: `core/` (bindings), `presentation/screens/{name}/`, `presentation/components/`, `presentation/theme/`, `presentation/utils/`

## Adding a new feature end-to-end

1. Define/add types in `notescribe-core/src/models.rs` with `#[derive(uniffi::Record, Serialize, Deserialize)]`
2. Add DB operations in `notescribe-core/src/db.rs` with `#[uniffi::export]`
3. Run `cargo build` to regenerate UniFFI Kotlin bindings
4. Call the new bindings from ViewModel
5. Create/modify UI components in the relevant screen directory
6. Update `HomeState`/`HomeEvent` if needed
7. Write tests (Rust `#[cfg(test)]` for core, JUnit 4/Compose UI Test for Kotlin)

## Testing

- `app/src/test/` — local JUnit 4 unit tests
- `app/src/androidTest/` — instrumented Espresso + Compose UI Test
- `notescribe-core/src/` — Rust unit tests (`#[cfg(test)]`)
- Every public function must have at least one test covering the success path and one covering the error path.

## Skills & Sub-Agents

This project uses opencode skills for specialized knowledge:

| Skill | Path | Use for |
|-------|------|---------|
| Architecture | `.opencode/skills/architecture/` | Project structure, data flow, navigation |
| Rust Core | `.opencode/skills/rust-core-conventions/` | Rust, SQLite, encryption, error handling |
| Kotlin UI | `.opencode/skills/kotlin-ui-conventions/` | Compose, MVI, adaptive layout |
| Testing | `.opencode/skills/testing/` | All test types and patterns |
| Strict Review | `.opencode/skills/strict-review/` | Security, performance, code quality audits |

## Verification Scripts

Run before every PR to catch violations automatically:

```bash
.opencode/skills/scripts/check-all.sh              # Quick quality check
.opencode/skills/scripts/check-rust-quality.sh     # Rust-specific
.opencode/skills/scripts/check-kotlin-quality.sh   # Kotlin-specific
.opencode/skills/scripts/check-architecture.sh     # Architecture check
.opencode/skills/scripts/run-strict-review.sh      # Full review suite
.opencode/skills/scripts/run-all-tests.sh          # Run all tests
```
