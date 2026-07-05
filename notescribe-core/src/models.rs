use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize, uniffi::Record)]
pub struct Note {
    pub id: String,
    pub title: String,
    pub content: String,
    pub is_pinned: bool,
    pub is_favorite: bool,
    pub tasks_progress: f64,
    pub created_at: i64,
    pub updated_at: i64,
}

#[derive(Debug, Clone, uniffi::Record)]
pub struct NoteListItem {
    pub id: String,
    pub title: String,
    pub is_pinned: bool,
    pub is_favorite: bool,
    pub tasks_progress: f64,
    pub created_at: i64,
    pub updated_at: i64,
}
