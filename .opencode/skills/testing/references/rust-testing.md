---
name: rust-testing
description: >
  Use when writing or reviewing Rust tests in notescribe-core/. Covers inline
  unit tests, integration tests, property-based testing with proptest, DB
  tests with in-memory SQLite, crypto tests, backup tests, and thread safety.
---

# Rust Testing

## Inline unit tests

Every source file MUST contain `#[cfg(test)] mod tests { use super::*; }`.

```rust
#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn test_create_note_sets_id_and_timestamps() {
        let db = Database::new_memory().unwrap();
        let note = db.create_note("title", "content").unwrap();
        assert!(!note.id.is_empty());
        assert!(note.created_at > 0);
    }
    #[test]
    fn test_create_note_empty_title_returns_error() {
        let db = Database::new_memory().unwrap();
        assert!(matches!(db.create_note("", "content"),
            Err(NoteScribeError::Database { .. })));
    }
    #[test]
    fn test_get_nonexistent_note_returns_error() {
        let db = Database::new_memory().unwrap();
        assert!(db.get_note("nonexistent-id").is_err());
    }
}
```

## Integration tests

```rust
use notescribe_core::{Database, backup};
#[test]
fn test_export_then_import_restores_all_notes() {
    let dir = std::env::temp_dir().join("notescribe_test_backup");
    let _ = std::fs::create_dir_all(&dir);
    let db = Database::new_memory().unwrap();
    let original = db.create_note("Backup", "Test").unwrap();
    let path = dir.join("backup.db");
    backup::export_to_file(&db, path.to_str().unwrap()).unwrap();
    let db2 = Database::new_memory().unwrap();
    backup::import_from_file(&db2, path.to_str().unwrap()).unwrap();
    let restored = db2.get_note(&original.id).unwrap();
    assert_eq!(restored.title, original.title);
    assert_eq!(restored.content, original.content);
    let _ = std::fs::remove_dir_all(&dir);
}
```

## DB testing

Every test gets its own `Database::new_memory()`. Test CRUD with zero, one, many rows.

```rust
fn setup_db() -> Database { Database::new_memory().expect("must succeed") }
#[test]
fn test_list_notes_returns_empty() { assert!(setup_db().list_notes().unwrap().is_empty()); }
#[test]
fn test_list_notes_after_insert() {
    let db = setup_db();
    db.create_note("Title", "Content").unwrap();
    assert_eq!(db.list_notes().unwrap().len(), 1);
}
```

## Crypto testing

```rust
const SALT_LEN: usize = 16;
const NONCE_LEN: usize = 12;
#[test]
fn test_encrypt_decrypt_roundtrip() {
    let pw = "correct-horse-battery-staple";
    let e = encrypt("Hello!", pw).unwrap();
    assert_eq!("Hello!", decrypt(&e, pw).unwrap());
}
#[test]
fn test_decrypt_wrong_password_returns_error() {
    let e = encrypt("secret", "correct").unwrap();
    assert!(matches!(decrypt(&e, "wrong"), Err(NoteScribeError::WrongPassword)));
}
#[test]
fn test_encrypt_output_format_length() {
    let pt = "hello";
    assert_eq!(encrypt(pt, "pass").unwrap().len(), SALT_LEN + NONCE_LEN + pt.len());
}
#[test]
fn test_encrypt_empty_plaintext() { assert_eq!(encrypt("", "pass").unwrap().len(), SALT_LEN + NONCE_LEN); }
#[test]
fn test_decrypt_tampered_ciphertext_fails() {
    let mut e = encrypt("tamper test", "pass").unwrap();
    e[e.len() - 1] ^= 0x01;
    assert!(decrypt(&e, "pass").is_err());
}
```

## Property-based testing

```rust
use proptest::prelude::*;
proptest! {
    #[test]
    fn test_encrypt_decrypt_any_string(pt in ".*", pw in ".{0,64}") {
        prop_assert_eq!(pt, decrypt(&encrypt(&pt, &pw).unwrap(), &pw).unwrap());
    }
}
```

## Thread safety

```rust
#[test]
fn test_concurrent_reads_and_writes() {
    let db = std::sync::Arc::new(Database::new_memory().unwrap());
    let mut handles = vec![];
    for i in 0..10 {
        let d = db.clone();
        handles.push(std::thread::spawn(move || {
            let note = d.create_note(&format!("Thread-{}", i), "data").unwrap();
            assert_eq!(note.title, d.get_note(&note.id).unwrap().title);
        }));
    }
    for h in handles { h.join().expect("thread panicked"); }
    assert_eq!(db.list_notes().unwrap().len(), 10);
}
```

## Rules

- Use `std::env::temp_dir()` or `tempfile`. Never write to production paths.
- Clean up temp dirs on failure with a `Drop` guard or `remove_dir_all`.
- Run `cargo test -- --nocapture` for stdout visibility.
- Prefer `assert!(result.is_err())` over `#[should_panic]`.
