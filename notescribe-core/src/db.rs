use rusqlite::{params, Connection};
use std::sync::Mutex;

use crate::error::NoteScribeError;
use crate::models::{Note, NoteListItem};

#[derive(Debug, uniffi::Object)]
pub struct Database {
    conn: Mutex<Connection>,
}

#[uniffi::export]
impl Database {
    #[uniffi::constructor]
    pub fn open(path: String) -> Result<Self, NoteScribeError> {
        let conn =
            Connection::open(&path).map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;

        conn.execute_batch(
            "PRAGMA journal_mode=WAL;
             PRAGMA synchronous=NORMAL;
             PRAGMA foreign_keys=ON;",
        )
        .map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;

        Ok(Database {
            conn: Mutex::new(conn),
        })
    }

    pub fn create_tables(&self) -> Result<(), NoteScribeError> {
        let conn = self.conn.lock().unwrap();
        conn.execute_batch(
            "CREATE TABLE IF NOT EXISTS notes (
                id            TEXT PRIMARY KEY,
                title         TEXT NOT NULL DEFAULT '',
                content       TEXT NOT NULL DEFAULT '',
                is_pinned     INTEGER NOT NULL DEFAULT 0,
                is_favorite   INTEGER NOT NULL DEFAULT 0,
                tasks_progress REAL NOT NULL DEFAULT 0.0,
                created_at    INTEGER NOT NULL,
                updated_at    INTEGER NOT NULL
            );

            CREATE INDEX IF NOT EXISTS idx_notes_updated_at ON notes(updated_at DESC);
            CREATE INDEX IF NOT EXISTS idx_notes_pinned ON notes(is_pinned) WHERE is_pinned = 1;",
        )
        .map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;
        Ok(())
    }

    pub fn create_note(
        &self,
        title: String,
        content: String,
        is_pinned: bool,
        is_favorite: bool,
    ) -> Result<String, NoteScribeError> {
        let id = uuid::Uuid::new_v4().to_string();
        let now = chrono::Utc::now().timestamp();
        let conn = self.conn.lock().unwrap();
        conn.execute(
            "INSERT INTO notes (id, title, content, is_pinned, is_favorite, tasks_progress, created_at, updated_at)
             VALUES (?1, ?2, ?3, ?4, ?5, 0.0, ?6, ?7)",
            params![id, title, content, is_pinned as i32, is_favorite as i32, now, now],
        )
        .map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;
        Ok(id)
    }

    pub fn get_all_notes(&self) -> Result<Vec<NoteListItem>, NoteScribeError> {
        let conn = self.conn.lock().unwrap();
        let mut stmt = conn
            .prepare(
                "SELECT id, title, is_pinned, is_favorite, tasks_progress, created_at, updated_at
                 FROM notes
                 ORDER BY is_pinned DESC, updated_at DESC",
            )
            .map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;

        let rows = stmt
            .query_map([], |row| {
                Ok(NoteListItem {
                    id: row.get(0)?,
                    title: row.get(1)?,
                    is_pinned: row.get::<_, i32>(2)? != 0,
                    is_favorite: row.get::<_, i32>(3)? != 0,
                    tasks_progress: row.get(4)?,
                    created_at: row.get(5)?,
                    updated_at: row.get(6)?,
                })
            })
            .map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;

        let mut notes = Vec::new();
        for row in rows {
            notes.push(row.map_err(|e| NoteScribeError::Database { msg: e.to_string() })?);
        }
        Ok(notes)
    }

    pub fn get_note_by_id(&self, id: String) -> Result<Note, NoteScribeError> {
        let conn = self.conn.lock().unwrap();
        conn.query_row(
            "SELECT id, title, content, is_pinned, is_favorite, tasks_progress, created_at, updated_at
             FROM notes WHERE id = ?1",
            params![id],
            |row| {
                Ok(Note {
                    id: row.get(0)?,
                    title: row.get(1)?,
                    content: row.get(2)?,
                    is_pinned: row.get::<_, i32>(3)? != 0,
                    is_favorite: row.get::<_, i32>(4)? != 0,
                    tasks_progress: row.get(5)?,
                    created_at: row.get(6)?,
                    updated_at: row.get(7)?,
                })
            },
        )
        .map_err(|e| match e {
            rusqlite::Error::QueryReturnedNoRows => {
                NoteScribeError::Database {
                    msg: "Note not found".to_string(),
                }
            }
            _ => NoteScribeError::Database { msg: e.to_string() },
        })
    }

    pub fn update_note(
        &self,
        id: String,
        title: String,
        content: String,
        is_pinned: bool,
        is_favorite: bool,
        tasks_progress: f64,
    ) -> Result<(), NoteScribeError> {
        let now = chrono::Utc::now().timestamp();
        let conn = self.conn.lock().unwrap();
        let rows = conn
            .execute(
                "UPDATE notes
                 SET title = ?1, content = ?2, is_pinned = ?3, is_favorite = ?4,
                     tasks_progress = ?5, updated_at = ?6
                 WHERE id = ?7",
                params![title, content, is_pinned as i32, is_favorite as i32, tasks_progress, now, id],
            )
            .map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;

        if rows == 0 {
            return Err(NoteScribeError::Database {
                msg: "Note not found".to_string(),
            });
        }
        Ok(())
    }

    pub fn delete_note(&self, id: String) -> Result<(), NoteScribeError> {
        let conn = self.conn.lock().unwrap();
        let rows = conn
            .execute("DELETE FROM notes WHERE id = ?1", params![id])
            .map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;

        if rows == 0 {
            return Err(NoteScribeError::Database {
                msg: "Note not found".to_string(),
            });
        }
        Ok(())
    }

    pub fn toggle_pin(&self, id: String) -> Result<(), NoteScribeError> {
        let now = chrono::Utc::now().timestamp();
        let conn = self.conn.lock().unwrap();
        let rows = conn
            .execute(
                "UPDATE notes
                 SET is_pinned = CASE WHEN is_pinned = 0 THEN 1 ELSE 0 END,
                     updated_at = ?1
                 WHERE id = ?2",
                params![now, id],
            )
            .map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;

        if rows == 0 {
            return Err(NoteScribeError::Database {
                msg: "Note not found".to_string(),
            });
        }
        Ok(())
    }

    pub fn toggle_favorite(&self, id: String) -> Result<(), NoteScribeError> {
        let now = chrono::Utc::now().timestamp();
        let conn = self.conn.lock().unwrap();
        let rows = conn
            .execute(
                "UPDATE notes
                 SET is_favorite = CASE WHEN is_favorite = 0 THEN 1 ELSE 0 END,
                     updated_at = ?1
                 WHERE id = ?2",
                params![now, id],
            )
            .map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;

        if rows == 0 {
            return Err(NoteScribeError::Database {
                msg: "Note not found".to_string(),
            });
        }
        Ok(())
    }

    pub fn replace_all_notes(&self, notes: Vec<Note>) -> Result<(), NoteScribeError> {
        let conn = self.conn.lock().unwrap();
        conn.execute("DELETE FROM notes", [])
            .map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;

        for note in &notes {
            conn.execute(
                "INSERT INTO notes (id, title, content, is_pinned, is_favorite, tasks_progress, created_at, updated_at)
                 VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)",
                params![
                    note.id,
                    note.title,
                    note.content,
                    note.is_pinned as i32,
                    note.is_favorite as i32,
                    note.tasks_progress,
                    note.created_at,
                    note.updated_at
                ],
            )
            .map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;
        }
        Ok(())
    }

    pub fn get_all_notes_full(&self) -> Result<Vec<Note>, NoteScribeError> {
        let conn = self.conn.lock().unwrap();
        let mut stmt = conn
            .prepare(
                "SELECT id, title, content, is_pinned, is_favorite, tasks_progress, created_at, updated_at
                 FROM notes
                 ORDER BY is_pinned DESC, updated_at DESC",
            )
            .map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;

        let rows = stmt
            .query_map([], |row| {
                Ok(Note {
                    id: row.get(0)?,
                    title: row.get(1)?,
                    content: row.get(2)?,
                    is_pinned: row.get::<_, i32>(3)? != 0,
                    is_favorite: row.get::<_, i32>(4)? != 0,
                    tasks_progress: row.get(5)?,
                    created_at: row.get(6)?,
                    updated_at: row.get(7)?,
                })
            })
            .map_err(|e| NoteScribeError::Database { msg: e.to_string() })?;

        let mut notes = Vec::new();
        for row in rows {
            notes.push(row.map_err(|e| NoteScribeError::Database { msg: e.to_string() })?);
        }
        Ok(notes)
    }
}
