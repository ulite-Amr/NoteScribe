# Database Conventions

> SQLite via `rusqlite 0.31` with bundled SQLite, WAL mode, `Mutex<Connection>`.

## Library & Pragma Setup

```toml
# Cargo.toml
[dependencies]
rusqlite = { version = "0.31", features = ["bundled"] }
```

On connection open, execute these pragmas **in order**:

```rust
use rusqlite::Connection;
use std::sync::Mutex;

pub struct Database {
    conn: Mutex<Connection>,
}

impl Database {
    pub fn new(path: String) -> Result<Self, NoteScribeError> {
        let conn = Connection::open(&path).map_err(|e| NoteScribeError::Database {
            msg: format!("Failed to open database: {e}"),
        })?;

        conn.execute_batch(
            "PRAGMA journal_mode = WAL;
             PRAGMA synchronous = NORMAL;
             PRAGMA foreign_keys = ON;
             PRAGMA busy_timeout = 5000;"
        ).map_err(|e| NoteScribeError::Database {
            msg: format!("Failed to set pragmas: {e}"),
        })?;

        Ok(Self { conn: Mutex::new(conn) })
    }
}
```

## Blocking Rules

🔴 **ALL queries parameterized with `?1`, `?2` syntax.** Never use `format!` or string concatenation to build SQL.

```rust
// 🔴 WRONG
let sql = format!("UPDATE notes SET title = '{}' WHERE id = {}", title, id);

// ✅ CORRECT
let sql = "UPDATE notes SET title = ?1 WHERE id = ?2";
conn.execute(sql, params![title, id])?;
```

🔴 **NEVER `SELECT *`.** Always name specific columns. This prevents breakage when the schema changes and reduces memory overhead.

```rust
// 🔴 WRONG
let sql = "SELECT * FROM notes";

// ✅ CORRECT
let sql = "SELECT id, title, created_at, updated_at FROM notes";
```

🔴 **Use prepared statements with `prepare_cached` for repeated queries.** The query cache reuses compiled statements, avoiding repeated SQL compilation overhead.

```rust
let mut stmt = conn.prepare_cached(
    "SELECT id, title, created_at, updated_at FROM notes WHERE title LIKE ?1"
)?;
let notes = stmt.query_map(params![format!("%{}%", search)], |row| {
    Ok(NoteListItem {
        id: row.get(0)?,
        title: row.get(1)?,
        created_at: row.get(2)?,
        updated_at: row.get(3)?,
    })
})?.collect::<Result<Vec<_>, _>>()?;
```

🔴 **Avoid N+1 queries — batch with `IN (...)`.** If you need to fetch notes by a list of IDs, do it in one query, not one per ID.

```rust
// 🔴 WRONG — N queries
let mut notes = Vec::new();
for id in &ids {
    let note = fetch_note_by_id(&conn, *id)?;
    notes.push(note);
}

// ✅ CORRECT — 1 query
let placeholders: Vec<String> = ids.iter().enumerate()
    .map(|(i, _)| format!("?{}", i + 1))
    .collect();
let sql = format!(
    "SELECT id, title, created_at, updated_at FROM notes WHERE id IN ({})",
    placeholders.join(", ")
);
let mut stmt = conn.prepare_cached(&sql)?;
```

🔴 **Migration strategy: version table with forward-only migrations.** No down migrations. Never alter or delete past migration files.

```rust
fn migrate(conn: &Connection) -> Result<(), NoteScribeError> {
    conn.execute_batch(
        "CREATE TABLE IF NOT EXISTS _migrations (
            version INTEGER PRIMARY KEY,
            applied_at TEXT NOT NULL DEFAULT (datetime('now'))
        );"
    ).map_err(|e| NoteScribeError::Database {
        msg: format!("Failed to create migrations table: {e}"),
    })?;

    let current: i64 = conn
        .query_row("SELECT COALESCE(MAX(version), 0) FROM _migrations", [], |row| row.get(0))
        .map_err(|e| NoteScribeError::Database {
            msg: format!("Failed to read migration version: {e}"),
        })?;

    let migrations: Vec<(i64, &str)> = vec![
        (1, "CREATE TABLE IF NOT EXISTS notes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            content TEXT NOT NULL DEFAULT '',
            created_at TEXT NOT NULL DEFAULT (datetime('now')),
            updated_at TEXT NOT NULL DEFAULT (datetime('now'))
        )"),
        (2, "CREATE INDEX IF NOT EXISTS idx_notes_title ON notes(title)"),
        (3, "ALTER TABLE notes ADD COLUMN is_archived INTEGER NOT NULL DEFAULT 0"),
    ];

    for (version, sql) in migrations {
        if version > current {
            conn.execute_batch(sql).map_err(|e| NoteScribeError::Database {
                msg: format!("Migration {version} failed: {e}"),
            })?;
            conn.execute(
                "INSERT INTO _migrations (version) VALUES (?1)",
                params![version],
            ).map_err(|e| NoteScribeError::Database {
                msg: format!("Failed to record migration {version}: {e}"),
            })?;
        }
    }

    Ok(())
}
```

🔴 **Connection pooling is NOT needed.** A single `Mutex<Connection>` is correct for this app size. All DB access is serialized through the mutex, which is sufficient for a local mobile SQLite database.

🔴 **All fallible methods return `Result<T, NoteScribeError>`.** Panic-free design.

🔴 **On create: return the created item's ID.** On update: return the updated row count.

```rust
pub fn create_note(&self, title: String, content: String) -> Result<NoteListItem, NoteScribeError> {
    let conn = self.conn.lock().unwrap_or_else(|e| e.into_inner());
    conn.execute(
        "INSERT INTO notes (title, content) VALUES (?1, ?2)",
        params![title, content],
    ).map_err(|e| NoteScribeError::Database {
        msg: format!("Failed to create note: {e}"),
    })?;
    let id = conn.last_insert_rowid();
    self.get_note_list_item(id)
}

pub fn update_note(&self, id: i64, title: String, content: String) -> Result<i64, NoteScribeError> {
    let conn = self.conn.lock().unwrap_or_else(|e| e.into_inner());
    let count = conn.execute(
        "UPDATE notes SET title = ?1, content = ?2, updated_at = datetime('now') WHERE id = ?3",
        params![title, content, id],
    ).map_err(|e| NoteScribeError::Database {
        msg: format!("Failed to update note {id}: {e}"),
    })?;
    Ok(count as i64)
}
```

🔴 **Use `TRANSACTION` for multi-step writes.** Ensures atomicity.

```rust
pub fn archive_notes(&self, ids: Vec<i64>) -> Result<i64, NoteScribeError> {
    let conn = self.conn.lock().unwrap_or_else(|e| e.into_inner());
    let tx = conn.transaction().map_err(|e| NoteScribeError::Database {
        msg: format!("Failed to start transaction: {e}"),
    })?;

    let mut count = 0i64;
    for id in &ids {
        let rows = tx.execute(
            "UPDATE notes SET is_archived = 1 WHERE id = ?1",
            params![id],
        ).map_err(|e| NoteScribeError::Database {
            msg: format!("Failed to archive note: {e}"),
        })?;
        count += rows as i64;
    }

    tx.commit().map_err(|e| NoteScribeError::Database {
        msg: format!("Failed to commit archive transaction: {e}"),
    })?;

    Ok(count)
}
```

🔴 **NEVER close the connection manually.** `Connection::drop` handles cleanup when the `Database` struct is dropped.
