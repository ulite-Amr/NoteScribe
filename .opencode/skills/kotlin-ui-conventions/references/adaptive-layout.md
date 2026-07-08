---
name: adaptive-layout
description: >
  Use when implementing adaptive/windowing behavior. Covers breakpoints,
  WindowSizeProvider, AdaptiveScaffold, and per-form-factor layout.
---

# Adaptive layout — strict rules

## Breakpoint definitions

Based on Material Design 3 window size classes:

| Class | Width range | Typical device |
|---|---|---|
| `COMPACT` | 0 – 599dp | Phone portrait |
| `MEDIUM` | 600 – 839dp | Tablet portrait, phone landscape |
| `EXPANDED` | 840dp+ | Tablet landscape, desktop |

**Defined in `WindowSizeProvider.kt`:**

```kotlin
enum class WindowSizeGroup { COMPACT, MEDIUM, EXPANDED }

object AdaptiveThresholds {
    val Medium = 600.dp
    val Expanded = 840.dp
}
```

## WindowSizeProvider — single source of truth

Wrap root composable to inject window size into the tree:

```kotlin
WindowSizeProvider { RootLayout() }
```

`WindowSizeProvider` uses `BoxWithConstraints` to measure available width and
provides result via `LocalWindowSizeGroup`.

Access anywhere:

```kotlin
val windowSize = LocalWindowSizeGroup.current
```

**Rules:**
- Apply `WindowSizeProvider` once at tree root, not per screen.
- NEVER check `maxWidth` directly. Use `LocalWindowSizeGroup.current`.
- NEVER hardcode breakpoints. Use `WindowSizeGroup` enum.
- File: `presentation/utils/WindowSizeProvider.kt`.

## AdaptiveLayoutContainer — subtree switching

Swap entire composable subtrees based on window size:

```kotlin
@Composable
fun AdaptiveLayoutContainer(
    modifier: Modifier = Modifier,
    compact: @Composable () -> Unit,
    medium: (@Composable () -> Unit)? = null,
    expanded: (@Composable () -> Unit)? = null,
)
```

**Fallback chain:**
- `EXPANDED` -> `expanded` ?: `medium` ?: `compact`
- `MEDIUM` -> `medium` ?: `compact`
- `COMPACT` -> `compact`

Only provide layouts that meaningfully differ:

```kotlin
AdaptiveLayoutContainer(
    compact = { MobileLayout() },
    medium = { TabletLayout() },
    expanded = { DesktopLayout() },
)
```

## AdaptiveScaffold — structural layout

Provides the correct structural container per window size:

```kotlin
@Composable
fun AdaptiveScaffold(
    topBar: (@Composable () -> Unit)? = null,
    bottomBar: (@Composable () -> Unit)? = null,
    navigationRail: (@Composable () -> Unit)? = null,
    navigationDrawer: (@Composable () -> Unit)? = null,
    content: @Composable (Modifier) -> Unit,
)
```

**Per-form-factor behavior:**

| Class | Top bar | Bottom nav | Nav rail | Nav drawer |
|---|---|---|---|---|
| `COMPACT` | Full width | Visible | Hidden | Hidden |
| `MEDIUM` | Full width | Hidden | Visible | Hidden |
| `EXPANDED` | Full width | Hidden | Hidden | Visible (240dp) |

**Usage:**

```kotlin
WindowSizeProvider {
    AdaptiveScaffold(
        topBar = {
            TopBar(headerSlot = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge) })
        },
        bottomBar = { WindowSizeAwareBottomNav() },
        navigationRail = { NavigationRail { RailItem(icon = Icons.Default.Home, label = "Home") } },
        navigationDrawer = { ModalDrawerSheet { DrawerItem(icon = Icons.Default.Home, label = "Home") } },
    ) { contentModifier ->
        HomeScreen(modifier = contentModifier.fillMaxSize())
    }
}
```

## Adaptive content grid

Switch lists vs grids based on size group:

```kotlin
val windowSize = LocalWindowSizeGroup.current
when (windowSize) {
    WindowSizeGroup.COMPACT -> NoteListVertical(notes = state.notes, onEvent = onEvent)
    WindowSizeGroup.MEDIUM, WindowSizeGroup.EXPANDED -> NoteListGrid(notes = state.notes, onEvent = onEvent)
}
```

## Testing adaptive layouts

Every screen needs Previews for all three breakpoints. Dark variant required
where layout differs.

```kotlin
@Preview(name = "Compact", widthDp = 360)
@Composable
private fun HomeScreenCompactPreview() { NoteScribeTheme { HomeScreen() } }

@Preview(name = "Medium", widthDp = 700)
@Composable
private fun HomeScreenMediumPreview() { NoteScribeTheme { HomeScreen() } }

@Preview(name = "Expanded", widthDp = 1000)
@Composable
private fun HomeScreenExpandedPreview() { NoteScribeTheme { HomeScreen() } }
```

Instrumented tests should verify layout switching via `onNodeWithTag`.

## Complete example

```kotlin
@Composable
fun RootLayout(modifier: Modifier = Modifier) {
    WindowSizeProvider {
        AdaptiveScaffold(
            topBar = {
                TopBar(headerSlot = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge) })
            },
            bottomBar = { WindowSizeAwareBottomNav() },
            navigationRail = { NavigationRail { /* items */ } },
            navigationDrawer = { ModalDrawerSheet { Text("Drawer", modifier = Modifier.padding(16.dp)) } },
        ) { contentModifier ->
            HomeScreen(modifier = contentModifier.fillMaxSize())
        }
    }
}

@Composable
private fun WindowSizeAwareBottomNav() {
    if (LocalWindowSizeGroup.current == WindowSizeGroup.COMPACT) {
        BottomNavigation { /* items */ }
    }
}
```

## NEVER

- Check `maxWidth` or `BoxWithConstraints` directly in screen composables.
- Hardcode `600.dp` or `840.dp` in screen code.
- Forget the fallback chain — always provide `compact` at minimum.
- Render navigation rail in compact mode or bottom nav in expanded mode.
- Mix layout strategies — use `AdaptiveScaffold` at top, content switching below.

## File reference

| File | Purpose |
|---|---|
| `presentation/utils/WindowSizeProvider.kt` | `WindowSizeProvider`, `WindowSizeGroup`, `AdaptiveThresholds` |
| `presentation/utils/AdaptiveContainer.kt` | `AdaptiveLayoutContainer` — swaps subtrees by size group |
| `presentation/utils/AdaptiveScaffold.kt` | `AdaptiveScaffold` — structural scaffold per size group |
