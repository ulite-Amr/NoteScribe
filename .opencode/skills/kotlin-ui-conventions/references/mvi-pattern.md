---
name: mvi-pattern
description: >
  Use when implementing or modifying any screen's state/event/viewmodel
  layer. Enforces the unidirectional MVI contract.
---

# MVI Pattern — strict contract

## Architecture

```
User → Event → ViewModel → State → Composable → UI
                  ↑                            │
                  └──────── feedback ──────────┘
```

- **View** renders `State`, sends `Event` through ViewModel.
- **ViewModel** holds `State`, processes `Event`, produces new `State`.
- **State** is an immutable snapshot of the UI at a point in time.
- **Event** represents a user action or system command.

## State — data class only

```kotlin
data class HomeState(
    val notes: List<NoteListItem> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
)
```

**Rules:**
- All fields with sensible defaults for initial state.
- `List<Model>` not `List<Model>?` — empty list means empty.
- `String?` for nullable errors — `null` means no error.
- MAX 5 properties. Group related data:
  ```kotlin
  data class NoteEditorState(
      val note: NoteData? = null,
      val saving: Boolean = false,
      val validation: ValidationState = ValidationState(),
  )
  data class ValidationState(val titleError: String? = null, val contentError: String? = null)
  ```
- NO `sealed class`, `open class`, `interface` for State.

## Event — sealed interface only

```kotlin
sealed interface HomeEvent {
    data object LoadNotes : HomeEvent
    data object OnAddNoteClick : HomeEvent
    data class DeleteNote(val id: String) : HomeEvent
    data class Search(val query: String) : HomeEvent
}
```

**Rules:**
- `data object` for parameterless events. Never bare `object`.
- `data class` for events with parameters.
- Name from user perspective: `OnAddNoteClick`, `OnSearchQueryChanged`.
- One event per action. Do not combine actions.
- NO logic in events — pure data carriers.

## ViewModel — exact contract

```kotlin
class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.LoadNotes -> loadNotes()
            HomeEvent.OnAddNoteClick -> onAddNoteClick()
            is HomeEvent.DeleteNote -> deleteNote(event.id)
            is HomeEvent.Search -> search(event.query)
        }
    }

    private fun loadNotes() {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val notes = repository.getNotes()
                _state.update { it.copy(notes = notes, loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, loading = false) }
            }
        }
    }

    private fun onAddNoteClick() { /* one-shot navigation event */ }

    private fun deleteNote(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteNote(id); loadNotes()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    private fun search(query: String) {
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val results = repository.searchNotes(query)
                _state.update { it.copy(notes = results, loading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, loading = false) }
            }
        }
    }
}
```

**Rules:**
- `private val _state = MutableStateFlow(InitialState())`
- `val state: StateFlow<State> = _state.asStateFlow()` — public read-only.
- `fun onEvent(event: Event)` — single entry point.
- Every event handler is a `private fun`.
- `_state.update { it.copy(...) }` — NEVER `_state.value =`.
- `loading = true` BEFORE async work. `loading = false` AFTER completion (both success and catch).
- Catch all exceptions, map to `error` field.
- All async on `Dispatchers.IO`. Never `Dispatchers.Main`.

## Three-state UI in composable

```kotlin
@Composable
private fun HomeContent(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when {
            state.loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            state.error != null -> {
                Column(Modifier.align(Alignment.Center).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { onEvent(HomeEvent.LoadNotes) }) { Text(stringResource(R.string.retry)) }
                }
            }
            state.notes.isEmpty() -> EmptyState(onAddClick = { onEvent(HomeEvent.OnAddNoteClick) })
            else -> NoteList(notes = state.notes, onEvent = onEvent, modifier = modifier)
        }
    }
}
```

**Order:** `loading` → `error` → empty (success) → data (success).
Errors must be user-visible via Snackbar or inline error text.
Never swallow errors. Never show Toast.

## One-shot navigation events

```kotlin
class HomeViewModel : ViewModel() {
    private val _events = Channel<HomeNavigationEvent>(Channel.BUFFERED)
    val events: Flow<HomeNavigationEvent> = _events.receiveAsFlow()

    private fun onAddNoteClick() {
        viewModelScope.launch { _events.send(HomeNavigationEvent.NavigateToEditor) }
    }
}

sealed interface HomeNavigationEvent { data object NavigateToEditor : HomeNavigationEvent }

// In composable:
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        when (event) { HomeNavigationEvent.NavigateToEditor -> navController.navigate("editor") }
    }
}
```

## 🟠 NEVER

- Expose `MutableStateFlow` from ViewModel.
- Put business logic in composables.
- Put logic/functions in State `data class`.
- Use `sealed class` instead of `sealed interface` for Events.
- Use `_state.value =` instead of `_state.update { it.copy(...) }`.
- Forget `loading = false` in catch block.
- Call Rust bindings on `Dispatchers.Main`.
- Swallow exceptions.
- Use `Toast` for errors — use `Snackbar`.
- Put navigation logic inside ViewModel — emit event, navigate in composable.
