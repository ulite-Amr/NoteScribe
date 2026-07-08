---
name: architecture-data-flow
description: >
  Detailed data flow patterns across the Rust<->UniFFI<->Kotlin boundary.
  Use when implementing a new feature that reads/writes data.
---

# 🔴 Data Flow Patterns

## READ Flow (e.g., load notes)

```
HomeScreen renders
  -> HomeViewModel.state (StateFlow)
    -> LaunchedEffect triggers HomeViewModel.onEvent(HomeEvent.LoadNotes)
      -> viewModelScope.launch(Dispatchers.IO) {
          val notes = notescribe_core.getNotes(database)
          _state.update { it.copy(notes = notes, loading = false) }
        }
```

## WRITE Flow (e.g., create note)

```
User taps FAB
  -> HomeScreen calls viewModel.onEvent(HomeEvent.OnAddNoteClick)
    -> HomeViewModel:
        fun onEvent(event: HomeEvent) {
          when (event) {
            HomeEvent.OnAddNoteClick -> createNote()
          }
        }
        private fun createNote() {
          viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(loading = true) }
            try {
              notescribe_core.createNote(database, title, content)
              _state.update {
                it.copy(loading = false, success = "Note created")
              }
            } catch (e: NoteScribeException) {
              _state.update {
                it.copy(loading = false, error = e.message)
              }
            }
          }
        }
```

## ERROR Flow (UniFFI Exception -> UI)

```
Rust returns Err(NoteScribeError::Database { msg: "Disk full" })
  -> JNA converts to com.uliteamr.notescribe.core.NoteScribeException
    -> Kotlin catch block in ViewModel
      -> _state.update { it.copy(error = e.message) }
        -> Compose shows Snackbar/ErrorText
```

## 🔴 CRITICAL RULES

- NEVER call `notescribe_core.*` on the main thread -- use `Dispatchers.IO`.
- NEVER catch exceptions silently -- always update state with error.
- NEVER expose Rust types (e.g., `Note`) directly to Composables -- always transform in ViewModel.
- ALWAYS use `try/catch` around UniFFI calls -- Rust errors become Kotlin exceptions.
- ALWAYS set `loading = true` before async work, `loading = false` after.
- NEVER swallow errors -- `error` field in state must be set.
