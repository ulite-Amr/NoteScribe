use crate::crypto;
use crate::db::Database;
use crate::error::NoteScribeError;
use crate::models::Note;

#[uniffi::export]
pub fn export_backup(
    db: &Database,
    password: String,
    file_path: String,
) -> Result<(), NoteScribeError> {
    let notes = db.get_all_notes_full()?;
    let json = serde_json::to_string(&notes)
        .map_err(|e| NoteScribeError::Encryption { msg: e.to_string() })?;
    let encrypted = crypto::encrypt(json.as_bytes(), &password)?;
    std::fs::write(&file_path, encrypted)
        .map_err(|e| NoteScribeError::Io { msg: e.to_string() })?;
    Ok(())
}

#[uniffi::export]
pub fn import_backup(
    db: &Database,
    password: String,
    file_path: String,
) -> Result<(), NoteScribeError> {
    let data = std::fs::read(&file_path)
        .map_err(|e| NoteScribeError::Io { msg: e.to_string() })?;
    let plaintext = crypto::decrypt(&data, &password)?;
    let notes: Vec<Note> = serde_json::from_slice(&plaintext)
        .map_err(|e| NoteScribeError::Encryption { msg: e.to_string() })?;
    db.replace_all_notes(notes)?;
    Ok(())
}
