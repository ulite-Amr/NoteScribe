---
name: architecture
description: >
  Use when the user asks about project structure, data flow across the
  Rust-Kotlin boundary, how to add a new feature end-to-end, or where
  specific files/modules live in NoteScribe.
---

# 🔴 NoteScribe Architecture

## 10 Core Rules

1. **Kotlin NEVER accesses SQLite or filesystem** — all persistence through Rust
2. **UniFFI bindings** in `core/notescribe_core.kt` are **auto-generated** — never hand-edit
3. **Each screen is self-contained** — State, Event, ViewModel, Screen in one directory
4. **ViewModel exposes ONLY** `val state` + `fun onEvent` — never `MutableStateFlow`
5. **`RootScreen` owns navigation only** — feature screens own their own state
6. **NO Rust types in UI** — ViewModel transforms all FFI types before composables see them
7. **NO circular dependencies** — screens never import each other's internals
8. **Every new feature follows the 6-step workflow** (see `references/feature-workflow.md`)
9. **Data flows one direction** — UI → Event → ViewModel → UniFFI → Rust → back
10. **UniFFI is the ONLY bridge** — no Kotlin SQLite/Room/encryption libraries

## Data Flow
```
UI → onEvent → ViewModel → Dispatchers.IO → UniFFI → Rust → Result → _state.update → UI
```

## Reference Documents

- `references/data-flow.md` — READ/WRITE/ERROR flow patterns with full code
- `references/navigation.md` — RootScreen pattern, back stack, no Jetpack Navigation
- `references/feature-workflow.md` — Exact 6-step process for any new feature

## Directory Structure

```
app/src/main/java/com/uliteamr/notescribe/
├── MainActivity.kt
├── core/              # UniFFI-generated — DO NOT EDIT
└── presentation/
    ├── components/    # Reusable composables only
    ├── icons/         # ImageVector, one per file
    ├── screens/       # home/, root/
    ├── theme/         # Color.kt, Type.kt, Theme.kt
    └── utils/         # AdaptiveScaffold, WindowSizeProvider, TimeUtils

notescribe-core/src/
├── lib.rs    ├── error.rs    ├── models.rs
├── crypto.rs ├── db.rs       └── backup.rs
```

## Technology Stack

| Layer | Technology |
|-------|-----------|
| UI | Kotlin 2.x + Jetpack Compose + Material 3 (Expressive alpha) |
| Architecture | MVI + UniFFI bridge |
| Persistence | Rust + rusqlite (WAL, synchronous=NORMAL) |
| Encryption | AES-256-GCM + Argon2 — format `[salt(16)][nonce(12)][ciphertext]` |
| Threading | Coroutines + `Dispatchers.IO` for all Rust calls |
| DI | None — `viewModel()` factory |
