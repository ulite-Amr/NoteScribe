---
name: ui-testing
description: >
  Use when writing or reviewing Compose UI tests in app/src/androidTest/.
  Covers rendering assertions, user interaction, state-driven changes,
  adaptive breakpoints, dark mode, and idling resources.
---

# UI Testing

## Setup

- **Framework:** Compose UI Test via `createComposeRule()`.
- **Location:** `app/src/androidTest/java/com/uliteamr/notescribe/`
- **Theme:** Every test MUST wrap content in `NoteScribeTheme { }`.

```kotlin
class HomeScreenTest {
    @Rule @JvmField val composeTestRule = createComposeRule()

    @Test
    fun shows_loading_indicator() {
        composeTestRule.setContent { NoteScribeTheme { HomeScreen() } }
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun displays_note_list_when_loaded() = runTest {
        val vm = HomeViewModel(FakeNoteRepository().apply {
            notes = listOf(NoteListItem(id = "1", title = "Meeting Notes",
                isPinned = false, createdAt = 1L, updatedAt = 1L))
        }).apply { onEvent(HomeEvent.LoadNotes) }
        composeTestRule.setContent { NoteScribeTheme { HomeScreen(viewModel = vm) } }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Meeting Notes").assertIsDisplayed()
    }

    @Test
    fun tapping_create_note_navigates() = runTest {
        var navigated = false
        composeTestRule.setContent {
            NoteScribeTheme { HomeScreen(onCreateNote = { navigated = true }) }
        }
        composeTestRule.onNodeWithContentDescription("Create note").performClick()
        composeTestRule.waitForIdle()
        assertTrue(navigated)
    }

    @Test
    fun tapping_delete_removes_note() = runTest {
        val vm = HomeViewModel(FakeNoteRepository().apply {
            notes = listOf(NoteListItem(id = "1", title = "Delete Me",
                isPinned = false, createdAt = 1L, updatedAt = 1L))
        }).apply { onEvent(HomeEvent.LoadNotes) }
        composeTestRule.setContent { NoteScribeTheme { HomeScreen(viewModel = vm) } }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Delete note").performClick()
        composeTestRule.onNodeWithText("Confirm").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Delete Me").assertDoesNotExist()
    }

    @Test
    fun shows_error_when_load_fails() = runTest {
        val vm = HomeViewModel(FakeNoteRepository().apply { shouldThrow = true })
            .apply { onEvent(HomeEvent.LoadNotes) }
        composeTestRule.setContent { NoteScribeTheme { HomeScreen(viewModel = vm) } }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Could not load notes").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun retry_after_error_reloads() = runTest {
        val repo = FakeNoteRepository().apply { shouldThrow = true }
        val vm = HomeViewModel(repo).apply { onEvent(HomeEvent.LoadNotes) }
        composeTestRule.setContent { NoteScribeTheme { HomeScreen(viewModel = vm) } }
        composeTestRule.waitForIdle()
        repo.shouldThrow = false
        repo.notes = listOf(NoteListItem(id = "1", title = "After Retry",
            isPinned = false, createdAt = 1L, updatedAt = 1L))
        composeTestRule.onNodeWithText("Retry").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("After Retry").assertIsDisplayed()
    }

    @Test
    fun shows_empty_state_when_no_notes() = runTest {
        val vm = HomeViewModel(FakeNoteRepository()).apply { onEvent(HomeEvent.LoadNotes) }
        composeTestRule.setContent { NoteScribeTheme { HomeScreen(viewModel = vm) } }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("No notes yet").assertIsDisplayed()
    }

    @Test
    fun renders_in_dark_theme() {
        composeTestRule.setContent {
            NoteScribeTheme(darkTheme = true) { HomeScreen() }
        }
        composeTestRule.onNodeWithTag("home_screen_root").assertExists()
    }
}
```

## Adaptive breakpoint testing

```kotlin
@Test
fun compact_layout_shows_bottom_navigation() {
    composeTestRule.setContent {
        CompositionLocalProvider(LocalWindowWidth provides WindowWidth.COMPACT) {
            NoteScribeTheme { HomeScreen() }
        }
    }
    composeTestRule.onNodeWithTag("bottom_navigation").assertIsDisplayed()
}

@Test
fun expanded_layout_shows_navigation_rail() {
    composeTestRule.setContent {
        CompositionLocalProvider(LocalWindowWidth provides WindowWidth.EXPANDED) {
            NoteScribeTheme { HomeScreen() }
        }
    }
    composeTestRule.onNodeWithTag("navigation_rail").assertIsDisplayed()
}
```

## Matcher usage

| Matcher | Use for | Example |
|---|---|---|
| `onNodeWithText(...)` | Text labels, titles, content | Note titles, button labels |
| `onNodeWithContentDescription(...)` | Icon buttons, accessibility | Create, delete, search icons |
| `onNodeWithTag(...)` | Structural containers, spinners | `loading_indicator`, `note_list` |
| `assertIsDisplayed()` | Element visibility | Element must be on screen |
| `assertDoesNotExist()` | Element absence | Deleted note, hidden element |
| `performClick()` | User taps | Button, list item, icon |

## Idling and rules

Call `composeTestRule.waitForIdle()` after triggering async work. Never use `Thread.sleep()`.

- Every test MUST call `setContent { NoteScribeTheme { ... } }`.
- Every async event MUST be followed by `waitForIdle()`.
- Test rendering, interaction, and state change.
- Test loading, success, error, and empty states.
- Test light and dark themes. Test all three breakpoints.
- Do NOT test ViewModel internals from UI tests.
