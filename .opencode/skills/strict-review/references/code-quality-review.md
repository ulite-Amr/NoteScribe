# Code Quality Review Checklist

> **BLOCKING** checks must pass to ship. Each check rated **PASS / FAIL / NA**.
> FAIL on any BLOCKING check blocks merge.

---

## Imports

### [BLOCKING] I1: No wildcard imports
- [ ] PASS / FAIL — No `import com.example.*` in any `.kt` file
- [ ] PASS / FAIL — No `use crate::*` in any Rust file

Good: `import com.uliteamr.notescribe.core.Note`
Bad: `import com.uliteamr.notescribe.*`

### [BLOCKING] I2: No unused imports
- [ ] PASS / FAIL — No grayed-out (unused) imports in Kotlin files
- [ ] PASS / FAIL — No `unused import` warnings in Rust files
- [ ] PASS / FAIL — CI enforces with `spotless`, `ktlint`, or `cargo check`

### I3: Import ordering
- [ ] PASS / FAIL / NA — Kotlin: android.*, androidx.*, com.*, org.*, java.*, kotlin.* grouped
- [ ] PASS / FAIL / NA — Rust: std, external crates, crate::self — alphabetically ordered
- [ ] PASS / FAIL / NA — No blank lines between imports of the same group

---

## Naming

### [BLOCKING] N1: PascalCase for classes
- [ ] PASS / FAIL — All class, `sealed interface`, `enum`, and `object` names use PascalCase
- [ ] PASS / FAIL — All Rust `struct` / `enum` / `trait` names use PascalCase

### [BLOCKING] N2: camelCase for functions and variables (Kotlin)
- [ ] PASS / FAIL — All Kotlin functions and variables use camelCase
- [ ] PASS / FAIL — Composable functions are PascalCase (convention)
- [ ] PASS / FAIL — Rust functions/variables use snake_case

Good: `val noteTitle: String`, `fun formatDate(timestamp: Long)`
Bad: `val note_title: String`, `fun FormatDate(timestamp: Long)`

### [BLOCKING] N3: No abbreviations
- [ ] PASS / FAIL — No single-letter names except loop indices (`i`, `j`) and type params (`T`)
- [ ] PASS / FAIL — No non-standard abbreviations: `btn`, `txt`, `ctx`, `vm`, `repo`

Good: `submitButton`, `databaseHelper`  Bad: `btn`, `dbHelper`

### [BLOCKING] N4: Meaningful names
- [ ] PASS / FAIL — Function names are verbs (`saveNote`, `deleteNote`)
- [ ] PASS / FAIL — Booleans are predicates (`isLoading`, `hasError`, `canDelete`)
- [ ] PASS / FAIL — No generic names like `data`, `info`, `stuff`, `tmp`, `temp`
- [ ] PASS / FAIL — No name shadowing (parameter hiding field or outer variable)

---

## Structure

### [BLOCKING] S1: File <= 200 lines
- [ ] PASS / FAIL — Every `.kt` and `.rs` file checked (generated bindings exempted)
- [ ] PASS / FAIL — Files exceeding 200 lines have a plan for splitting

### [BLOCKING] S2: Function <= 30 lines
- [ ] PASS / FAIL — Every function body <= 30 lines (excluding braces and blank lines)
- [ ] PASS / FAIL — Complex logic extracted into named helper functions

Good:
```kotlin
fun processContent(content: String): String {
    val trimmed = content.trim()
    return sanitizeHtml(normalizeWhitespace(trimmed))
}
```

### [BLOCKING] S3: Nesting <= 2 levels
- [ ] PASS / FAIL — No nesting deeper than 2 levels; early returns used to flatten

Good:
```kotlin
for (item in items) {
    if (!item.isValid()) continue
    handle(item)
}
```
Bad: `if` inside `for` inside `if` inside `when` (level 4)

### [BLOCKING] S4: No comment headers or section dividers
- [ ] PASS / FAIL — No `// ---` dividers, no `// IMPORTS` labels, no file-level headers
- [ ] PASS / FAIL — Code structure is self-documenting

### [BLOCKING] S5: No data class with >5 properties without grouping
- [ ] PASS / FAIL — `data class` with >5 properties groups related fields into sub-data classes

Good:
```kotlin
data class Note(
    val id: Long,
    val title: String,
    val content: String,
    val metadata: NoteMetadata,
    val timestamps: NoteTimestamps,
)
```
Bad: flat `data class` with 8+ scalar properties

---

## MVI

### [BLOCKING] M1: State is data class
- [ ] PASS / FAIL — All `{Name}State` types are `data class` with no functions, `val` only

Good: `data class HomeState(val notes: List<NoteListItem> = emptyList(), val isLoading: Boolean = false)`

### [BLOCKING] M2: Event is sealed interface
- [ ] PASS / FAIL — All `{Name}Event` types are `sealed interface` (NOT `sealed class`)
- [ ] PASS / FAIL — Event objects are `data object` / `data class`; named as commands

Good: `sealed interface HomeEvent { data object LoadNotes : HomeEvent }`
Bad: `sealed class HomeEvent { class LoadNotes : HomeEvent() }`

### [BLOCKING] M3: ViewModel exposes only state + onEvent
- [ ] PASS / FAIL — ViewModel has exactly 2 public members: `val state: StateFlow<State>` + `fun onEvent(event: Event)`
- [ ] PASS / FAIL — No public `MutableStateFlow`; state updates via `_state.update { }`

### [BLOCKING] M4: ViewModel calls UniFFI on Dispatchers.IO
- [ ] PASS / FAIL — All Rust core calls wrapped in `withContext(Dispatchers.IO)`
- [ ] PASS / FAIL — No `runBlocking` in ViewModel; no UniFFI on main thread

---

## Architecture

### [BLOCKING] A1: No circular dependencies
- [ ] PASS / FAIL — No module A -> module B -> module A chains
- [ ] PASS / FAIL — Presentation does not depend on another presentation module
- [ ] PASS / FAIL — `notescribe-core` (Rust) does not depend on Android/Kotlin

### [BLOCKING] A2: No data layer leakage to UI
- [ ] PASS / FAIL — Composables do not reference `Repository`, `Database`, or `Dao`
- [ ] PASS / FAIL — ViewModel is the single source of UI state; no `Context` in ViewModel

### [BLOCKING] A3: Proper module isolation
- [ ] PASS / FAIL — `core`: UniFFI bindings + data classes only
- [ ] PASS / FAIL — `presentation`: UI + ViewModels + state only
- [ ] PASS / FAIL — `app`: Application + DI + navigation only

### A4: Package structure compliance
- [ ] PASS / FAIL / NA — Package `com.uliteamr.notescribe.*` matches directory path
- [ ] PASS / FAIL / NA — Screen files in `presentation/screens/{name}/`, shared in `components/`, theme in `theme/`

---

## Error Handling

### [BLOCKING] E1: No swallowed exceptions
- [ ] PASS / FAIL — Every `try/catch` has meaningful handling (no empty `catch {}`)

Good: `catch (e: DatabaseException) { _state.update { copy(error = "Save failed") } }`
Bad: `catch (e: Exception) { }`

### [BLOCKING] E2: Meaningful error messages
- [ ] PASS / FAIL — Error messages are user-friendly (not raw stack traces or `e.message`)

Good: `"Could not save note. Check storage."`
Bad: `"SQLiteException: code 13 SQL_ERROR disk image malformed"`

### [BLOCKING] E3: Proper Result usage
- [ ] PASS / FAIL — Rust functions return `Result<T, NoteScribeError>` (no panics)
- [ ] PASS / FAIL — Kotlin handles success/error from results or thrown exceptions

### [BLOCKING] E4: No unwrap/expect in Rust production code
- [ ] PASS / FAIL — No `unwrap()` or `expect()` outside `#[cfg(test)]`
- [ ] PASS / FAIL — Use `?` operator or `match` for propagation

Good: `let conn = db.connect()?;`
Bad: `let conn = db.connect().unwrap();`

---

## Comments

### [BLOCKING] C1: No inline comments in production code
- [ ] PASS / FAIL — No `//` or `/* */` comments in Kotlin or Rust production code
- [ ] PASS / FAIL — Code is self-documenting via clear names and small functions
- [ ] PASS / FAIL — Only exception: `// SAFETY:` before Rust `unsafe` blocks

Bad: `// Check if note is archived\nfun isArchived(note: Note) = note.isArchived`

### C2: KDoc only on public API (if present)
- [ ] PASS / FAIL / NA — If KDoc exists, it is on public API only and kept in sync

### [BLOCKING] C3: No TODO comments
- [ ] PASS / FAIL — No `// TODO`, `// FIXME`, `// HACK`, `// XXX`, `// BUG`, `// OPTIMIZE`

### [BLOCKING] C4: No commented-out code
- [ ] PASS / FAIL — No commented-out code blocks anywhere. Git history is the record.

---

## Strings & Resources — Production Hardening

These checks ensure every user-visible value is localizable, maintainable, and never hardcoded.

### [BLOCKING] R1: All user-visible strings in `strings.xml`
- [ ] PASS / FAIL — Every `Text()`, `contentDescription`, `Button`, `AlertDialog` text uses `stringResource(R.string.xxx)`
- [ ] PASS / FAIL — Zero hardcoded English strings in composables
- [ ] PASS / FAIL — Error messages in `stringResource`, not concatenated in code

Good: `Text(stringResource(R.string.home_empty))`
Bad: `Text("No notes yet")`

### [BLOCKING] R2: `strings.xml` organized by screen with section comments
- [ ] PASS / FAIL — File starts with GLOBAL section, then per-screen sections
- [ ] PASS / FAIL — Each section has `<!-- ===== SCREEN NAME ===== -->` comment divider
- [ ] PASS / FAIL — Keys prefixed by screen: `home_`, `note_detail_`, `settings_`, `error_`

Structure:
```xml
<!-- ===== GLOBAL ===== -->
<string name="settings">Settings</string>

<!-- ===== HOME ===== -->
<string name="home_title">My Notes</string>
```

### [BLOCKING] R3: No duplicate string values
- [ ] PASS / FAIL — Same text never defined twice for different screens
- [ ] PASS / FAIL — Shared strings (Save, Cancel, Delete) in GLOBAL section only

### [BLOCKING] R4: No unused `string` resources
- [ ] PASS / FAIL — Every `<string>` in `strings.xml` is referenced in code or other XML
- [ ] PASS / FAIL — Strings only used in previews are marked or kept in preview scope

### [BLOCKING] R5: Plurals use `<plurals>` tag
- [ ] PASS / FAIL — `"%d note(s)"` implemented as `<plurals name="notes_count">`
- [ ] PASS / FAIL — Single/plural forms properly defined

Good: `<plurals name="notes_count"><item quantity="one">%d note</item><item quantity="other">%d notes</item></plurals>`
Bad: `<string name="notes_count">%d notes</string>` (ignores singular)

### R6: No hardcoded padding/margins/spacing outside theme
- [ ] PASS / FAIL / NA — Spacing values defined in theme/Spacing or `LocalSpacing` instead of scattered `Modifier.padding(X.dp)`
- [ ] PASS / FAIL / NA — Key measurements centralized (e.g., `CardCornerRadius`, `ListSpacing`)

### [BLOCKING] R7: No hardcoded URLs, endpoints, feature flags, or config values
- [ ] PASS / FAIL — URLs in `BuildConfig` or config file, not in code
- [ ] PASS / FAIL — Feature flags in `Config` object or runtime source, not `val isEnabled = true`
- [ ] PASS / FAIL — Timeouts, intervals, delays in named constants
- [ ] PASS / FAIL — No hardcoded test data in production code (only in `src/test/`)

Good: `private val DEBOUNCE_MS = 300L`
Bad: `delay(300)` without named constant

---

## Security (Code Quality Subset)

### [BLOCKING] Q1: No hardcoded secrets
- [ ] PASS / FAIL — No API keys, passwords, tokens in source, `BuildConfig`, `strings.xml`, or `gradle.properties`

### [BLOCKING] Q2: Parameterized SQL
- [ ] PASS / FAIL — All SQL uses parameterized statements (`?1`, `$1`), never string concatenation

Good: `"INSERT INTO notes (title) VALUES (?1)", params![title]`
Bad: `format!("INSERT INTO notes (title) VALUES ('{}')", title)`

### [BLOCKING] Q3: Only public API is #[uniffi::export]
- [ ] PASS / FAIL — Internal helpers are `pub(crate)` or private, never `#[uniffi::export]`
- [ ] PASS / FAIL — Only `NoteScribeError` crosses the FFI boundary

### [BLOCKING] Q4: SAFETY comments for unsafe
- [ ] PASS / FAIL — Every `unsafe` block has `// SAFETY:` explaining why invariants hold
- [ ] PASS / FAIL — No `unsafe` in `#[uniffi::export]` functions

Good: `// SAFETY: ptr is valid, non-null, and properly aligned because ...`
Bad: bare `unsafe { ... }` with no justification

---

## Summary

| Section | Checks | PASS | FAIL | NA | Score |
|---|---|---|---|---|---|
| Imports | 3 | - | - | - | - |
| Naming | 4 | - | - | - | - |
| Structure | 5 | - | - | - | - |
| MVI | 4 | - | - | - | - |
| Architecture | 4 | - | - | - | - |
| Error Handling | 4 | - | - | - | - |
| Comments | 4 | - | - | - | - |
| Security (subset) | 4 | - | - | - | - |
| Strings & Resources | 7 | - | - | - | - |
| **TOTAL** | **39** | **-** | **-** | **-** | **-** |

**All `[BLOCKING]` checks must be PASS to merge. Review not complete until all BLOCKING checks pass.**
