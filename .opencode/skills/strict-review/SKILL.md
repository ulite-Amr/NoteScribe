---
name: strict-review
description: >
  ULTRA-STRICT CODE REVIEW. Use for EVERY code review, PR review, or quality
  check in NoteScribe. Enforces security, clean code, naming, imports,
  architecture purity, zero tolerance for shortcuts. ALWAYS load this skill
  before reviewing ANY code — Rust, Kotlin, Compose, Gradle, or build files.
---

# 🔴 ULTRA-STRICT REVIEW — ZERO TOLERANCE RULES

These rules are **absolute and mandatory**. Every violation must be flagged as **BLOCKING**. No exceptions, no shortcuts, no "we'll fix it later."

## Top 10 Non-Negotiables

| # | Rule | Scope |
|---|------|-------|
| 1 | **NO `unwrap()` / `expect()` / `todo!()` in production Rust** — use `?`, `map_err`, or match | Rust |
| 2 | **NO wildcard imports (`import ... *`)** — every symbol explicit | Kotlin |
| 3 | **NO `sealed class` for Events** — must be `sealed interface` with `data object` | Kotlin |
| 4 | **ALL SQL is parameterized (`?1`, `?2`)** — never string-concatenated | Rust |
| 5 | **ViewModel exposes ONLY `val state` + `fun onEvent`** — no other public methods | Kotlin |
| 6 | **ViewModel calls UniFFI on `Dispatchers.IO`** — never main thread | Kotlin |
| 7 | **Encryption format `[salt(16)][nonce(12)][ciphertext]` is IMMUTABLE** | Rust |
| 8 | **NO hardcoded `dp`/`sp` for text — use `MaterialTheme.typography`** | Kotlin |
| 9 | **NO business logic in composables** — purely declarative | Kotlin |
| 10 | **Package must be `com.uliteamr.notescribe.*`** — never `com.example.*` | Kotlin |

## Reference Documents — Load for Detailed Review

| For this type of review | Load this reference |
|------------------------|---------------------|
| **Full security audit** (OWASP Mobile Top 10) | `references/security-review.md` |
| **Performance audit** (APK size, rendering, memory, startup) | `references/performance-review.md` |
| **Code quality audit** (naming, structure, imports, architecture) | `references/code-quality-review.md` |

## Rust Zero Tolerance

| Rule |
|------|
| NO `unsafe` without `// SAFETY:` justification |
| ALL `pub fn` must be `#[uniffi::export]` or `pub(crate)` |
| ALL public types crossing FFI must derive `uniffi::Record`/`uniffi::Enum` + `Debug` |
| NO `as` narrowing casts — use `From`/`TryFrom` |
| Zeroize ALL secrets with `zeroize::Zeroizing` |
| Mutex `lock().unwrap()` only when poison = process-kill scenario |
| Input validation at FFI boundary |
| NO storing raw passwords — derive key via Argon2, discard original |
| File paths must be validated — no path traversal |
| NO commented-out code |
| ALL error messages must be human-readable and meaningful |
| Module separation strict — `db.rs` doesn't call `crypto.rs` directly |

## Kotlin / Compose Zero Tolerance

| Rule |
|------|
| NO unused imports, variables, parameters, or functions |
| State is `data class` only — no functions, no logic |
| `_state` is private, `state` exposed via `.asStateFlow()` |
| State updates: `_state.update { it.copy(...) }` — never `_state.value =` |
| NO `@Suppress` without justification |
| Every `@Composable` receives `modifier: Modifier = Modifier` as first optional param |
| NO hardcoded colors — always `MaterialTheme.colorScheme.*` |
| Use `stringResource` for ALL user-visible strings — NO hardcoded text in composables |
| `strings.xml` organized by screen: GLOBAL section first, then per-screen with `<!-- ===== NAME ===== -->` dividers |
| String keys prefixed by screen (`home_`, `note_detail_`, `settings_`, `error_`) — no duplicate values |
| Plurals use `<plurals>` tag, not `<string>` with `%d` |
| NO hardcoded URLs, endpoints, feature flags, timeouts, or config values — all in `BuildConfig` or named `const` |
| NO hardcoded test/production mix — test data only in `src/test/` and `src/androidTest/` |
| File max 200 lines. Function max 30 lines. Nesting max 2 levels. |
| NO `data class` with >5 properties without grouping |
| Preview functions: suffix variant in name, typos = BLOCKING |
| KDoc allowed on public API ONLY — must be updated when code changes. Outdated KDoc = BLOCKING |
| NO `//` inline comments or section dividers |
| NO `TODO` comments — create issue or implement now |

## Build / Gradle Zero Tolerance

| Rule |
|------|
| ALL dependency versions in `libs.versions.toml` — never hardcoded |
| NO unused or duplicate dependencies |
| Convention plugins required for shared config |
| ProGuard must keep UniFFI JNA bindings |
| NO hardcoded signing keys — load from env or CI |
| ProGuard/R8 with aggressive optimization required |
| NO reflection or annotation processing at runtime |

## Test Requirements

| Rule |
|------|
| Every public function needs success + error test |
| Test names must be descriptive — no `//` comments in tests |
| NO dependence on external state |
| Edge cases required: empty, wrong password, missing IDs, max-length, concurrency |

## Legacy Code — Keep Clean

All 21 original violations have been fixed. Do NOT reintroduce:
- `NoteItem.kt`: package, wildcards, `Long`→`String`, comments, typos
- `HomeEvents.kt` / `RootEvents.kt`: `sealed class` → `sealed interface`
- All KDocs removed from private functions
- All wildcard imports expanded
- `HomeViewModel.kt` / `RootViewModel.kt`: proper MVI pattern
