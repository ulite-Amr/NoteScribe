---
name: rust-core-conventions
description: >
  Use when modifying Rust code in notescribe-core/, adding new models,
  changing the DB schema, working with encryption/decryption, or adding
  UniFFI-exported functions. Not for general Kotlin or build changes.
---

# 🔴 Rust Core Conventions

## Quick Rules

| # | Rule | Details in |
|---|------|-----------|
| 1 | **NO `unwrap()` / `expect()` in production** — use `?`, `map_err`, match | `references/error-handling.md` |
| 2 | **NO `unsafe` without `// SAFETY:`** | `references/error-handling.md` |
| 3 | **ALL `pub fn` is `#[uniffi::export]` or `pub(crate)`** | `references/uniffi-export.md` |
| 4 | **FFI types: `derive(uniffi::Record/Enum)` + `Debug`** | `references/uniffi-export.md` |
| 5 | **ALL SQL parameterized (`?1`, `?2`)** — never string concat | `references/database.md` |
| 6 | **NO `as` narrowing — use `From`/`TryFrom`** | `references/error-handling.md` |
| 7 | **NO `todo!()` / `unimplemented!()` in production** | `references/error-handling.md` |
| 8 | **Zeroize secrets with `Zeroizing`** | `references/encryption.md` |
| 9 | **Encryption format `[salt(16)][nonce(12)][ciphertext]` — IMMUTABLE** | `references/encryption.md` |
| 10 | **Module isolation: `db.rs` does NOT call `crypto.rs`** | `references/database.md` + `references/encryption.md` |
| 11 | **Rust is SINGLE SOURCE OF TRUTH for all data logic** | `architecture/references/data-flow.md` |
| 12 | **Every public fn needs success + error test** | `testing/references/rust-testing.md` |

## Reference Documents

- `references/database.md` — WAL mode, query patterns, migrations, N+1 prevention, prepared statements
- `references/encryption.md` — AES-256-GCM details, Argon2 params, Zeroizing lifecycle, format immutability
- `references/error-handling.md` — NoteScribeError enum, `From` impls, human-readable messages
- `references/uniffi-export.md` — Export patterns, type mapping table, 4-step workflow

## Module Map

```
notescribe-core/src/
├── lib.rs        — re-exports, init, #[uniffi::export] facade
├── models.rs     — Note, NoteListItem, NoteScribeError
├── db.rs         — Database struct, all SQL (Mutex<Connection>)
├── crypto.rs     — encrypt, decrypt, derive_key (pure functions)
├── backup.rs     — export_backup, import_backup
└── error.rs      — NoteScribeError enum + From impls
```

## Cargo.toml

```toml
[package]
edition = "2024"
resolver = "2"

[profile.release]
lto = "fat"
codegen-units = 1
opt-level = "z"
strip = true
panic = "abort"
```

## Build Discipline

- `cargo clippy -- -D warnings` before every commit
- Binary MUST NOT exceed 5MB compressed
- `cargo build` regenerates UniFFI bindings — verify Android compile
