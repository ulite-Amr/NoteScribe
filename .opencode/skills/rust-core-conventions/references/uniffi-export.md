# UniFFI Export Conventions

> How Rust types and functions are exported to Kotlin via UniFFI.

## Configuration

```toml
# notescribe-core/uniffi.toml
[bindings]
cdylib_name = "notescribe_core"

[bindings.kotlin]
package = "com.uliteamr.notescribe.core"
```

## Blocking Rules

🔴 **`#[uniffi::export]` on `impl Database { ... }` exports ALL methods.** Use this pattern to export an entire implementation block. For standalone functions, use `#[uniffi::export]` on individual `pub fn`.

```rust
// ✅ CORRECT — exports all Database methods as a group
#[uniffi::export]
impl Database {
    pub fn create_note(&self, title: String, content: String) -> Result<NoteListItem, NoteScribeError> {
        // ...
    }

    pub fn get_all_notes(&self) -> Result<Vec<NoteListItem>, NoteScribeError> {
        // ...
    }
}

// ✅ CORRECT — standalone export function
#[uniffi::export]
pub fn encrypt(plaintext: Vec<u8>, password: String) -> Result<Vec<u8>, NoteScribeError> {
    // ...
}
```

🔴 **Type mappings are fixed.** Know the Rust-to-Kotlin mapping:

| Rust | Kotlin |
|---|---|
| `String` | `String` |
| `i64` | `Long` |
| `i32` | `Int` |
| `f64` | `Double` |
| `bool` | `Boolean` |
| `Vec<T>` | `List<T>` |
| `Option<T>` | `T?` |
| `Result<T, E>` | `T` throws `NoteScribeException` |
| `()` | `Unit` |

🔴 **Records: `#[derive(uniffi::Record)]` on data structs.** Records become Kotlin `data class` objects. Fields are accessed directly as properties.

```rust
#[derive(uniffi::Record, Debug, Serialize, Deserialize)]
pub struct NoteListItem {
    pub id: i64,
    pub title: String,
    pub created_at: String,
    pub updated_at: String,
}
```

In Kotlin:
```kotlin
val item: NoteListItem = database.getAllNotes().first()
println(item.title) // direct property access
```

🔴 **Enums: `#[derive(uniffi::Enum)]` on error/state enums.** Enums become Kotlin `sealed class` (or regular `enum class` for fieldless variants).

```rust
#[derive(uniffi::Enum, Debug)]
pub enum NoteScribeError {
    Database { msg: String },
    Encryption { msg: String },
    WrongPassword,
    Io { msg: String },
}
```

In Kotlin:
```kotlin
try {
    database.createNote("", "content")
} catch (e: NoteScribeException) {
    when (e) {
        is NoteScribeException.DatabaseException -> println(e.msg)
        is NoteScribeException.WrongPasswordException -> println("Wrong password")
        else -> println(e.message)
    }
}
```

🔴 **`Result<T, E>` maps to Kotlin exceptions (throws).** The Kotlin caller must catch `NoteScribeException`. UniFFI auto-generates the exception hierarchy from `NoteScribeError`.

🔴 **`#[derive(Debug)]` on ALL types — no exceptions.** Every type that crosses the FFI boundary must implement `Debug`. This is required for logging and error reporting even if the type is not directly printed.

🔴 **NEVER change a type's exported name.** UniFFI uses the Rust identifier as-is to generate Kotlin class/function names. Renaming a Rust type breaks all Kotlin callers.

```rust
// If you rename this...
pub struct NoteListItem { ... }
// ...to this...
pub struct NoteSummary { ... }
// ...every Kotlin file referencing `NoteListItem` breaks.
```

🔴 **If you rename a field in a Record, all Kotlin callers break — update them in the same PR.**

```rust
// Before
pub struct Note {
    pub note_title: String,  // ← Kotlin uses .noteTitle
}

// After — 🔴 BREAKING CHANGE
pub struct Note {
    pub title: String,       // ← Kotlin now uses .title
}
```

## Full Workflow

### Step 1: Add or update a type in `models.rs`

```rust
#[derive(uniffi::Record, Debug, Serialize, Deserialize)]
pub struct Note {
    pub id: i64,
    pub title: String,
    pub content: String,
    pub created_at: String,
    pub updated_at: String,
}
```

### Step 2: Add or update an export in `db.rs`

```rust
#[uniffi::export]
impl Database {
    pub fn search_notes(&self, query: String) -> Result<Vec<NoteListItem>, NoteScribeError> {
        let conn = self.conn.lock().unwrap_or_else(|e| e.into_inner());
        let sql = "SELECT id, title, created_at, updated_at FROM notes WHERE title LIKE ?1";
        let mut stmt = conn.prepare_cached(sql)?;
        let notes = stmt
            .query_map(params![format!("%{}%", query)], |row| {
                Ok(NoteListItem {
                    id: row.get(0)?,
                    title: row.get(1)?,
                    created_at: row.get(2)?,
                    updated_at: row.get(3)?,
                })
            })?
            .collect::<Result<Vec<_>, _>>()?;
        Ok(notes)
    }
}
```

### Step 3: Run `cargo build` to regenerate bindings

```bash
cargo build
```

This produces:
- `target/release/libnotescribe_core.so` (or `.dylib` / `.dll`)
- `notescribe-core/bindings/kotlin/com/uliteamr/notescribe/core/` — auto-generated `.kt` files

### Step 4: Use the new export in Kotlin

```kotlin
// In NoteScribeViewModel.kt
fun onEvent(event: HomeEvent.Search) {
    viewModelScope.launch(Dispatchers.IO) {
        try {
            val results = database.searchNotes(event.query)
            _state.update { it.copy(searchResults = results) }
        } catch (e: NoteScribeException) {
            _state.update { it.copy(error = e.message ?: "Search failed") }
        }
    }
}
```

## Generated Kotlin Structure

UniFFI generates these Kotlin files (simplified):

```
notescribe-core/bindings/kotlin/com/uliteamr/notescribe/core/
  NoteScribeCore.kt          — main binding, contains Database class
  NoteScribeCoreModels.kt    — Note, NoteListItem records
  NoteScribeCoreErrors.kt    — NoteScribeException hierarchy
```

These files are checked into the Android `app/` module and referenced as regular Kotlin source. DO NOT hand-edit them — they are auto-generated and will be overwritten on the next `cargo build`.
