---
name: composable-guidelines
description: >
  Use when writing or reviewing @Composable functions. Covers parameters,
  theming, previews, performance, and accessibility.
---

# Composable guidelines — strict rules

## Parameter conventions

- **`modifier: Modifier = Modifier` is the first optional parameter.**
  Required params first, then `modifier`, then other optional params.

  ```kotlin
  @Composable
  fun NoteCard(
      data: NoteCardData,
      onClick: () -> Unit,
      modifier: Modifier = Modifier,
      elevated: Boolean = false,
  )
  ```

- Never omit modifier on public composables. Callers must control layout/padding.
- Never require `Modifier` — always default to `Modifier`.

## Theming — zero hardcoded values

- **ALL text uses `MaterialTheme.typography.*`.** No `fontSize`, `lineHeight`,
  `letterSpacing`, or `fontWeight` for standard text. Use `fontWeight` only for
  semantic emphasis (bold labels).

  ```kotlin
  // CORRECT
  Text(text = data.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)

  // BLOCKING FAIL
  Text(text = data.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B13))
  ```

- **ALL colors use `MaterialTheme.colorScheme.*`.** No `Color(0xFF...)`,
  no `Color.Red`, no `Color.Unspecified` as default for themed colors.

  ```kotlin
  // CORRECT
  Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface))

  // BLOCKING FAIL
  Card(colors = CardDefaults.cardColors(containerColor = Color.White))
  ```

- Use `theme.shapes.*` for corner radii. Only use explicit `RoundedCornerShape`
  when design intentionally deviates from theme.

## Preview functions

- **Every public composable MUST have at least one Preview.**
- **Light AND dark Previews REQUIRED for every component.**
- **Name format: `{Name}{Variant}Preview`.** Examples: `NoteCardPreview`, `NoteCardDarkPreview`.

  ```kotlin
  @Preview(showBackground = true)
  @Composable
  private fun NoteCardPreview() {
      NoteScribeTheme { NoteCard(data = sampleNoteData(), onClick = {}) }
  }

  @Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
  @Composable
  private fun NoteCardDarkPreview() {
      NoteScribeTheme { NoteCard(data = sampleNoteData(), onClick = {}) }
  }
  ```

- **Always wrap in `NoteScribeTheme { }`.** Never use default Material theme.
- **Dark preview MUST use `Configuration.UI_MODE_NIGHT_YES`.**
- **Extract sample data** to avoid duplication:
  ```kotlin
  private fun sampleNoteData() = NoteCardData(id = "1", title = "Sample", content = "Brief content", category = "Dev", date = "JAN 26")
  ```

## Composable naming

- **PascalCase nouns** describing what is rendered: `NoteCard`, `TopBar`, `NoteList`.
- Avoid verbs: use `NoteCard` not `DisplayNoteCard`.
- Avoid `UI` suffix: `HomeScreen` not `HomeScreenUI`.
- Private helpers keep descriptive but short: `NoteCardHeader`, `CategoryLabel`.
- Screen composable matches file name: `HomeScreen.kt` → `HomeScreen`.

## Performance

- **Stable lambdas — do not recreate on every recomposition:**
  ```kotlin
  val onDelete = remember { { id: String -> onEvent(HomeEvent.DeleteNote(id)) } }
  ```
- **Use `remember` for expensive computations:**
  ```kotlin
  val formattedDate = remember(date) { formatDate(date) }
  ```
- **Use `derivedStateOf` for derived state:**
  ```kotlin
  val hasContent = remember { derivedStateOf { state.notes.isNotEmpty() } }
  ```
- **Use `key` in `LazyColumn`/`LazyGrid`:** `items(notes, key = { it.id })`.
- **Lift state up.** Keep leaf composables stateless where possible.

## Accessibility

- **Every `Icon` must have meaningful `contentDescription`.**
  Decorative icons: `contentDescription = null` with justification.
- **Use `stringResource` for content descriptions:**
  ```kotlin
  Icon(imageVector = Add, contentDescription = stringResource(R.string.cd_add_note))
  ```
- **Describe the action, not the element:**
  `contentDescription = "Add note"` not `"Plus icon"`.

## Strings — no hardcoded text

- **ALL user-visible strings use `stringResource(R.string.xxx)`.**
- **No string concatenation.** Use `String.format` with `stringResource`:
  ```kotlin
  Text(stringResource(R.string.note_count, count))
  // BLOCKING: Text("$count notes")
  ```

## Structural limits

- **File max 200 lines.** Extract private composables and helpers.
- **Function body max 30 lines.** Extract large UI blocks.
- **Nesting max 2 levels.** Extract nested `if`/`when` into functions:
  ```kotlin
  @Composable
  fun HomeContent(state: HomeState, ...) {
      Box {
          when {
              state.loading -> LoadingIndicator()
              state.error != null -> ErrorSection(state.error, ...)
              else -> NoteList(state.notes, ...)
          }
      }
  }
  ```

## State collection

- **Use `collectAsStateWithLifecycle()`** not `collectAsState()`:
  ```kotlin
  val state by viewModel.state.collectAsStateWithLifecycle()
  ```
- Collect at screen level in the public composable. Pass data down.

## Layout best practices

- Use `Modifier.fillMaxWidth()` / `Modifier.fillMaxSize()` explicitly.
- Use `Arrangement.spacedBy()` and `PaddingValues` for spacing. Avoid `Spacer`.
- Prefer `Column`/`Row`/`Box` over `ConstraintLayout` unless complex relationships require it.

## Checklist

Every `@Composable` must pass:

- [ ] `modifier: Modifier = Modifier` first optional parameter
- [ ] No hardcoded `fontSize`/`sp` — uses `MaterialTheme.typography.*`
- [ ] No hardcoded `Color(...)` — uses `MaterialTheme.colorScheme.*`
- [ ] All `Icon` elements have `contentDescription`
- [ ] All strings use `stringResource(R.string.xxx)`
- [ ] Light + dark Preview exist in the file
- [ ] Previews wrapped in `NoteScribeTheme { }`
- [ ] No business logic — purely declarative
- [ ] File <= 200 lines, function <= 30 lines, nesting <= 2 levels
- [ ] Keys on `LazyColumn`/`LazyGrid` items
