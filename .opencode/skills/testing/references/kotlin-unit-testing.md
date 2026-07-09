---
name: kotlin-unit-testing
description: >
  Use when writing or reviewing Kotlin unit tests in app/src/test/. Covers
  JUnit 4, ViewModel testing with kotlinx-coroutines-test, StateFlow
  assertions, UniFFI mocking, and testing all MVI state transitions.
---

# Kotlin Unit Testing

## Framework and setup

- **Framework:** JUnit 4 with `@Test` annotations.
- **Coroutines:** `kotlinx-coroutines-test` with `runTest` and `TestDispatcher`.
- **Mocking:** Hand-written fake implementations of UniFFI wrapper interfaces. If needed, use MockK — never Mockito.

```kotlin
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
```

## ViewModel testing pattern

Every test MUST use `runTest`, test initial state, test every state transition (loading, success, error, empty), and test every event handler with success and error paths.

```kotlin
class HomeViewModelTest {
    private lateinit var repository: FakeNoteRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        repository = FakeNoteRepository()
        viewModel = HomeViewModel(repository)
    }

    @Test
    fun `initial state is loading with empty notes and no error`() = runTest {
        val state = viewModel.state.value
        assertTrue(state.loading)
        assertTrue(state.notes.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `load notes transitions from loading to success`() = runTest {
        repository.notes = listOf(
            NoteListItem(id = "1", title = "Test", isPinned = false, createdAt = 1000L, updatedAt = 1000L)
        )
        val states = mutableListOf<HomeState>()
        val job = backgroundScope.launch {
            viewModel.state.collect { states.add(it) }
        }
        viewModel.onEvent(HomeEvent.LoadNotes)
        advanceUntilIdle()
        assertTrue(states[0].loading)
        assertFalse(states[1].loading)
        assertEquals("Test", states[1].notes[0].title)
        job.cancel()
    }

    @Test
    fun `load notes when repository fails sets error state`() = runTest {
        repository.shouldThrow = true
        viewModel.onEvent(HomeEvent.LoadNotes)
        advanceUntilIdle()
        val state = viewModel.state.value
        assertFalse(state.loading)
        assertNotNull(state.error)
        assertTrue(state.notes.isEmpty())
    }

    @Test
    fun `delete note removes note from list`() = runTest {
        repository.notes = listOf(
            NoteListItem(id = "1", title = "A", isPinned = false, createdAt = 1L, updatedAt = 1L),
            NoteListItem(id = "2", title = "B", isPinned = false, createdAt = 2L, updatedAt = 2L),
        )
        viewModel.onEvent(HomeEvent.LoadNotes)
        advanceUntilIdle()
        viewModel.onEvent(HomeEvent.DeleteNote("1"))
        advanceUntilIdle()
        assertEquals("B", viewModel.state.value.notes[0].title)
    }

    @Test
    fun `delete note with nonexistent id does not change list`() = runTest {
        repository.notes = listOf(
            NoteListItem(id = "1", title = "A", isPinned = false, createdAt = 1L, updatedAt = 1L)
        )
        viewModel.onEvent(HomeEvent.LoadNotes)
        advanceUntilIdle()
        viewModel.onEvent(HomeEvent.DeleteNote("nonexistent"))
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.notes.size)
    }

    @Test
    fun `load notes with empty result has empty list and no error`() = runTest {
        repository.notes = emptyList()
        viewModel.onEvent(HomeEvent.LoadNotes)
        advanceUntilIdle()
        val state = viewModel.state.value
        assertFalse(state.loading)
        assertTrue(state.notes.isEmpty())
        assertNull(state.error)
    }
}
```

## Mocking the UniFFI boundary

```kotlin
interface NoteRepository {
    suspend fun listNotes(): List<NoteListItem>
    suspend fun createNote(title: String, content: String): Note
    suspend fun deleteNote(id: String)
}

class FakeNoteRepository : NoteRepository {
    var notes: List<NoteListItem> = emptyList()
    var shouldThrow: Boolean = false

    override suspend fun listNotes(): List<NoteListItem> {
        if (shouldThrow) throw NoteScribeException.Database("Simulated error")
        return notes
    }

    override suspend fun createNote(title: String, content: String): Note {
        if (shouldThrow) throw NoteScribeException.Database("Simulated error")
        return Note(id = UUID.randomUUID().toString(), title = title, content = content,
            isPinned = false, isFavorite = false, tasksProgress = 0.0,
            createdAt = System.currentTimeMillis(), updatedAt = System.currentTimeMillis())
    }

    override suspend fun deleteNote(id: String) {
        if (shouldThrow) throw NoteScribeException.Database("Simulated error")
        notes = notes.filter { it.id != id }
    }
}
```

## Rules

- Every `onEvent` call must be followed by `advanceUntilIdle()`.
- Do NOT rely on `delay()`. Use `advanceUntilIdle()` or `advanceTimeBy()`.
- Test failure scenarios by setting the fake to throw.
- Test that `loading` is `true` during async work before `advanceUntilIdle()`.
- Never test Compose UI from unit tests. Use androidTest for UI verification.
- Never import Android framework classes (`Context`, `Bundle`, `Parcel`) in unit tests.
