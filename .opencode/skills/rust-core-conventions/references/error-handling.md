# Error Handling Conventions

> `NoteScribeError` enum with `thiserror` — the single error type for all Rust core operations.

## Error Enum Definition

```rust
use thiserror::Error;

#[derive(uniffi::Enum, Debug, Error)]
pub enum NoteScribeError {
    #[error("Database error: {msg}")]
    Database { msg: String },

    #[error("Encryption error: {msg}")]
    Encryption { msg: String },

    #[error("Wrong password")]
    WrongPassword,

    #[error("I/O error: {msg}")]
    Io { msg: String },
}
```

## Blocking Rules

🔴 **`NoteScribeError` is the ONLY error type exposed across the FFI boundary.** Internal modules may use `rusqlite::Error` or `std::io::Error` internally, but they MUST be mapped to `NoteScribeError` at the module boundary. No other error type crosses into Kotlin.

🔴 **`WrongPassword` variant carries NO message.** This prevents information leakage about why authentication failed (e.g., distinguishing "wrong password" from "data corrupted"). An attacker must not be able to differentiate these cases.

```rust
// 🔴 WRONG — leaks information
WrongPassword { reason: String }

// ✅ CORRECT — indistinguishable failure
WrongPassword
```

🔴 **ALL error messages must be HUMAN-READABLE.** Write for the developer reading logs, not a user-facing UI. Include context (what operation failed, what identifier) but NEVER include secrets, file paths, or memory addresses.

```rust
// 🔴 WRONG — includes internal path
NoteScribeError::Database {
    msg: format!("SQL error at /home/user/.local/share/notescribe/db.sqlite: {e}"),
}

// ✅ CORRECT — context without internal paths
NoteScribeError::Database {
    msg: format!("Failed to create note with title '{title}': {e}"),
}
```

🔴 **Map lower-level errors at module boundaries, not at every call site.** Define `From` impls for the most common conversions.

```rust
impl From<rusqlite::Error> for NoteScribeError {
    fn from(e: rusqlite::Error) -> Self {
        NoteScribeError::Database {
            msg: format!("{e}"),
        }
    }
}

impl From<std::io::Error> for NoteScribeError {
    fn from(e: std::io::Error) -> Self {
        NoteScribeError::Io {
            msg: format!("{e}"),
        }
    }
}
```

🔴 **NEVER expose internal details in user-facing errors.** No stack traces, no file paths, no memory addresses, no SQL query text that includes user data (parameterized queries are fine).

## Usage Pattern

Functions in `db.rs` use `?` with `From` impls to convert automatically:

```rust
use rusqlite::params;

pub fn get_note_list_item(&self, id: i64) -> Result<NoteListItem, NoteScribeError> {
    let conn = self.conn.lock().unwrap_or_else(|e| e.into_inner());
    let sql = "SELECT id, title, created_at, updated_at FROM notes WHERE id = ?1";
    conn.query_row(sql, params![id], |row| {
        Ok(NoteListItem {
            id: row.get(0)?,
            title: row.get(1)?,
            created_at: row.get(2)?,
            updated_at: row.get(3)?,
        })
    })
    .map_err(|e| match e {
        rusqlite::Error::QueryReturnedNoRows => NoteScribeError::Database {
            msg: format!("Note with id {id} not found"),
        },
        other => NoteScribeError::Database {
            msg: format!("Failed to fetch note {id}: {other}"),
        },
    })
}
```

Where more granular control is needed, use `map_err`:

```rust
pub fn delete_note(&self, id: i64) -> Result<(), NoteScribeError> {
    let conn = self.conn.lock().unwrap_or_else(|e| e.into_inner());
    let count = conn
        .execute("DELETE FROM notes WHERE id = ?1", params![id])
        .map_err(|e| NoteScribeError::Database {
            msg: format!("Failed to delete note {id}: {e}"),
        })?;

    if count == 0 {
        return Err(NoteScribeError::Database {
            msg: format!("Note {id} not found"),
        });
    }

    Ok(())
}
```

## Kotlin-Side Mapping

UniFFI maps `Result<T, NoteScribeError>` to Kotlin `T` throws `NoteScribeException`. The Kotlin code catches it:

```kotlin
// Kotlin — in ViewModel on Dispatchers.IO
try {
    val note = database.createNote("Title", "Content")
    _state.update { it.copy(notes = it.notes + note) }
} catch (e: NoteScribeException) {
    _state.update { it.copy(error = e.message ?: "Unknown error") }
}
```

## Testing Error Paths

Every function that returns `Result` must test at least one error path:

```rust
#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_delete_nonexistent_note_returns_error() {
        let db = Database::new(":memory:".into()).unwrap();
        let result = db.delete_note(99999);
        assert!(matches!(result, Err(NoteScribeError::Database { .. })));
    }

    #[test]
    fn test_decrypt_wrong_password_returns_wrong_password() {
        let encrypted = encrypt(b"hello", "pass1".into()).unwrap();
        let result = decrypt(&encrypted, "pass2".into());
        assert!(matches!(result, Err(NoteScribeError::WrongPassword)));
    }
}
```

## Error Variant Summary

| Variant | Carries | When used |
|---|---|---|
| `Database { msg }` | `String` | SQL errors, constraint violations, not found |
| `Encryption { msg }` | `String` | Argon2 init failure, cipher creation failure, truncated data |
| `WrongPassword` | Nothing | AES-GCM authentication tag mismatch |
| `Io { msg }` | `String` | File read/write failures, directory creation failures |
