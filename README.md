> [!WARNING]
> This project is in early development. APIs, database schema, and encryption internals are all subject to breaking changes without notice.

# NoteScribe

An Android notes application with a clear boundary between what the user sees and what does the actual work. The Jetpack Compose layer handles the interface; a Rust core — bridged via JNI — owns the storage, encryption, and business logic underneath it.