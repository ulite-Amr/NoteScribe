---
name: architecture-feature-workflow
description: >
  The exact 6-step workflow for adding any new feature end-to-end.
  Use BEFORE starting any new feature implementation.
---

# 🔴 Feature Workflow -- EXACT 6 STEPS

## Step 1: Define Rust Model

**File:** `notescribe-core/src/models.rs`

- Add struct with `#[derive(uniffi::Record, Serialize, Deserialize)]`.
- Add any new error variants to `error.rs`.
- 🔴 All fields must match Kotlin expectations: `String` not `Long` for IDs, `i64` for timestamps.
- 🔴 All public types must derive `uniffi::Record`/`uniffi::Enum` and `Debug`.

```rust
#[derive(uniffi::Record, Serialize, Deserialize, Debug)]
pub struct Note {
    pub id: String,
    pub title: String,
    pub content: String,
    pub created_at: i64,
    pub updated_at: i64,
}
```

## Step 2: Add DB Operations

**File:** `notescribe-core/src/db.rs`

- Add method to `Database` impl with `#[uniffi::export]`.
- Use parameterized SQL only (`?1`, `?2`, `?3`, ...).
- Return `Result<T, NoteScribeError>`.
- 🔴 NO `unwrap`, NO `expect`, NO `todo!`, NO `unimplemented!()`.
- 🔴 ALL SQL is parameterized -- never string-concatenated.
- 🔴 NO `pub` fields on non-FFI types.

```rust
#[uniffi::export]
pub fn create_note(&self, title: String, content: String) -> Result<Note, NoteScribeError> {
    let conn = self.conn.lock().map_err(/* ... */)?;
    conn.execute(
        "INSERT INTO notes (id, title, content, created_at, updated_at) VALUES (?1, ?2, ?3, ?4, ?5)",
        params![...],
    )?;
    // ...
}
```

## Step 3: Build & Regenerate Bindings

**Command:** `./gradlew :app:assembleDebug` (or `cargo build` in `notescribe-core/` for Rust-only)

- The Gradle pipeline handles cross-compilation and binding regeneration:
  - `cargoBuildHost` builds the Rust library for the host platform.
  - `generateUniFFIBindings` generates Kotlin bindings from the compiled `.so`.
  - `copyGeneratedBindings` places the generated `.kt` into the Android source tree.
- `cargo build` alone only compiles Rust — it does NOT regenerate the Kotlin bindings.
- 🔴 NEVER manually edit the generated file (`notescribe_core.kt`).
- 🔴 If build fails, fix Rust first before touching Kotlin.
- Run `cargo test` to verify Rust tests pass.

## Step 4: Add to Backup (if needed)

**File:** `notescribe-core/src/backup.rs`

- Add export/import for new data.
- Maintain backward compatibility with existing backups.
- 🔴 Never break the backup format -- append new fields, never remove.

## Step 5: Create/Update ViewModel

**File:** `app/.../screens/{name}/{Name}ViewModel.kt`

- Call the new UniFFI binding inside `viewModelScope.launch(Dispatchers.IO)`.
- Update state via `_state.update { it.copy(...) }`.
- 🔴 NEVER on main thread, NEVER expose MutableStateFlow.
- 🔴 NO business logic in composables -- purely declarative.

```kotlin
class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadNotes -> loadNotes()
        }
    }

    private fun loadNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(loading = true) }
            try {
                val notes = notescribe_core.getNotes(database)
                _state.update { it.copy(notes = notes, loading = false) }
            } catch (e: NoteScribeException) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }
}
```

## Step 6: Create/Update UI

**File:** `app/.../screens/{name}/{Name}Screen.kt`

- Composable receives `modifier: Modifier = Modifier` as first optional parameter.
- Uses `MaterialTheme.typography` for text, `MaterialTheme.colorScheme` for colors.
- 🔴 NO business logic, NO hardcoded `dp`/`sp` values, NO hardcoded colors.
- 🔴 NO KDoc or comments in production code.

```kotlin
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // ...
}
```

## 🔴 ENFORCEMENT

- Skipping Step 1 or Step 2 (Rust) before touching Kotlin is FORBIDDEN.
- Step 3 must pass (green build) before Step 5.
- Any feature touching the database MUST include backup support (Step 4).
- Code review will reject PRs missing any of these 6 steps.
