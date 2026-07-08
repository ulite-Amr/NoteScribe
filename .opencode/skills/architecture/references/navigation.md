---
name: architecture-navigation
description: >
  Navigation architecture using root screen pattern. Use when modifying
  navigation, adding new screens, or changing the back stack.
---

# 🔴 Navigation Architecture

## Architecture

- Single Activity (`MainActivity.kt`).
- NO Jetpack Navigation Component -- manual composable-based switching.
- `RootScreen` owns: top bar, navigation state, screen switching.
- Each feature screen is a composable called from RootScreen.

## RootScreen Pattern

```kotlin
@Composable
fun RootLayout(modifier: Modifier = Modifier) {
    val rootViewModel: RootViewModel = viewModel()
    val state by rootViewModel.state.collectAsStateWithLifecycle()

    AdaptiveScaffold(
        topBar = {
            TopBar(
                headerSlot = { Text(state.topBarTitle) },
                optionsMenuSlot = { /* menu items */ }
            )
        }
    ) { childModifier ->
        when (state.currentRoute) {
            "home" -> HomeScreen(modifier = childModifier)
            else -> HomeScreen(modifier = childModifier)
        }
    }
}
```

## Screen Registration

Every new screen must be registered in two places:

1. `RootState.currentRoute` -- add the route string constant.
2. `RootLayout` `when` block -- add the composable branch.

Route strings are plain lowercase kebab-case (e.g., `"settings"`, `"note-detail"`).

## Back Navigation

- Back press is handled by `RootViewModel` listening to `BackHandler`.
- `RootEvent.OnNavigateBack` pops the route stack.
- The system back button is intercepted via `BackHandler` composable.
- On the root route, system back exits the app (default behavior).

## 🔴 STRICT RULES

- NO Jetpack Navigation -- the root pattern is simpler and avoids Fragment complexities.
- `RootViewModel` manages ONLY navigation state -- NOT feature state.
- Each feature screen has its OWN ViewModel -- never share state through RootViewModel.
- Back navigation is handled via `RootEvent.OnNavigateBack`.
- Deep links go through `MainActivity` intent handling -> `RootEvent`.
- Screen-to-screen data passing: NEVER pass raw data. Pass IDs and let the screen load its own data.
- NO `Bundle`, `SavedStateHandle`, or `Intent` extras for complex objects -- IDs only.
