---
name: kotlin-ui-conventions
description: >
  Use when creating or modifying Kotlin UI code: composables, ViewModels,
  MVI state/events, theming, adaptive layouts, icons. Not for Rust or
  build system changes.
---

# 🔴 Kotlin UI Conventions

## Top Rules

| # | Rule | Details in |
|---|------|-----------|
| 1 | **NO wildcard imports** — every symbol explicit | `strict-review/references/code-quality-review.md` |
| 2 | **NO `sealed class` for Events** — must be `sealed interface` | `references/mvi-pattern.md` |
| 3 | **State is `data class`** — no functions, no logic | `references/mvi-pattern.md` |
| 4 | **ViewModel: ONLY `val state` + `fun onEvent`** publicly | `references/mvi-pattern.md` |
| 5 | **UniFFI on `Dispatchers.IO`** — never main thread | `references/mvi-pattern.md` |
| 6 | **NO hardcoded `dp`/`sp`/colors** — use theme only | `references/composable-guidelines.md` |
| 7 | **`strings.xml` organized by screen** — GLOBAL first, then per-screen with `<!-- ===== NAME ===== -->` dividers | `strict-review/references/code-quality-review.md` |
| 8 | **NO hardcoded URLs/endpoints/timeouts/flags** — `BuildConfig` or named `const` | `strict-review/references/code-quality-review.md` |
| 9 | **NO business logic in composables** — purely declarative | `references/composable-guidelines.md` |
| 10 | **`_state.update { it.copy(...) }`** — never `_state.value =` | `references/mvi-pattern.md` |

## Reference Documents

- `references/mvi-pattern.md` — Complete State/Event/ViewModel contract, 3-state UI, full code examples
- `references/composable-guidelines.md` — Modifier rules, previews, performance, accessibility
- `references/adaptive-layout.md` — COMPACT/MEDIUM/EXPANDED patterns, WindowSizeProvider, AdaptiveScaffold

## MVI Quick Reference

```
HomeState.kt          data class — loading, error, data fields
HomeEvent.kt          sealed interface — data objects/classes per action
HomeViewModel.kt      _state + state + onEvent + private handlers
HomeScreen.kt         @Composable — receives state + calls onEvent
```

## Theme

- `MaterialExpressiveTheme` with dynamic color (Android 12+)
- `AppTypography` + `Color.kt` light/dark schemes
- Always wrap in `NoteScribeTheme { }`

## Adaptive Layout

| Breakpoint | Width | Layout |
|-----------|-------|--------|
| COMPACT | <600dp | Mobile: top bar + bottom nav |
| MEDIUM | 600-839dp | Tablet: nav rail + content |
| EXPANDED | ≥840dp | Desktop: nav drawer + content |
