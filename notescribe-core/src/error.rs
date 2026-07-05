use std::fmt;

#[derive(Debug, uniffi::Error)]
pub enum NoteScribeError {
    Database { msg: String },
    Encryption { msg: String },
    WrongPassword,
    Io { msg: String },
}

impl fmt::Display for NoteScribeError {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            NoteScribeError::Database { msg } => write!(f, "Database error: {msg}"),
            NoteScribeError::Encryption { msg } => write!(f, "Encryption error: {msg}"),
            NoteScribeError::WrongPassword => write!(f, "Wrong password"),
            NoteScribeError::Io { msg } => write!(f, "IO error: {msg}"),
        }
    }
}

impl std::error::Error for NoteScribeError {}
