# Performance Review Checklist

> Comprehensive performance audit for Android + Rust hybrid apps.
> Each check rated **PASS / FAIL / NA**.
> FAIL on any check requires remediation before merge.

---

## APK Size

### A1: ProGuard / R8 enabled
- [ ] PASS / FAIL / NA — `minifyEnabled true` in release build type with `useFullR8`
- [ ] PASS / FAIL / NA — No over-broad `-keep` rules; mapping file retained for deobfuscation
- [ ] PASS / FAIL / NA — `consumer-rules.pro` for libraries are minimal

### A2: Resources optimized
- [ ] PASS / FAIL / NA — Unused resources removed (lint UnusedResources); WebP preferred over PNG
- [ ] PASS / FAIL / NA — No duplicate resources across modules
- [ ] PASS / FAIL / NA — `resConfigs` set to locale subset for release

### A3: Vector drawables preferred
- [ ] PASS / FAIL / NA — All icons are vector drawables (`.xml`), not raster images
- [ ] PASS / FAIL / NA — Vector path complexity reviewed (no excessively complex paths bloating binary)

### A4: No unused dependencies
- [ ] PASS / FAIL / NA — All declared dependencies are used in code (no orphaned libs)
- [ ] PASS / FAIL / NA — Test dependencies scoped with `testImplementation` / `androidTestImplementation`
- [ ] PASS / FAIL / NA — No `api` leakage from internal modules (should be `implementation`)
- [ ] PASS / FAIL / NA — Rust `Cargo.toml` pruned of unused crates

### A5: Native library size
- [ ] PASS / FAIL / NA — `libnotescribe_core.so` size measured per ABI (arm64-v8a, armeabi-v7a, x86_64)
- [ ] PASS / FAIL / NA — Only necessary ABIs packaged; `jniLibs` excludes debug symbols
- [ ] PASS / FAIL / NA — APK Analyzer used to audit per-ABI contribution

---

## Rendering

### R1: Recomposition count checked
- [ ] PASS / FAIL / NA — Layout Inspector recomposition counter reviewed for key screens
- [ ] PASS / FAIL / NA — `remember` and `derivedStateOf` used to filter unnecessary recompositions
- [ ] PASS / FAIL / NA — Lambdas extracted to `val` (not recreated inline) in composable calls

### R2: Stable keys in LazyColumn
- [ ] PASS / FAIL / NA — `key` parameter provided to every `LazyColumn` / `LazyRow`
- [ ] PASS / FAIL / NA — Keys are stable identifiers (not index, not random) for correct diffing
- [ ] PASS / FAIL / NA — Content types specified for slot reuse (`contentType` parameter)

### R3: No unnecessary state hoisting
- [ ] PASS / FAIL / NA — State hosted at minimum common scope (not at top-level unnecessarily)
- [ ] PASS / FAIL / NA — `rememberSaveable` used for UI state surviving config changes
- [ ] PASS / FAIL / NA — Composables accept lambdas, not stateful ViewModels directly

### R4: Layout performance
- [ ] PASS / FAIL / NA — No deeply nested `Column` / `Row` chains (use `ConstraintLayout` or `FlowRow`)
- [ ] PASS / FAIL / NA — Intrinsic measurements avoided (`IntrinsicSize.Min` / `IntrinsicSize.Max`)
- [ ] PASS / FAIL / NA — No `SubcomposeLayout` without documented justification

### R5: Image loading
- [ ] PASS / FAIL / NA — Coil or Glide used (not manual `BitmapFactory` on UI thread)
- [ ] PASS / FAIL / NA — Image requests specify target size; `DiskCache` and `MemoryCache` configured
- [ ] PASS / FAIL / NA — Placeholder and error drawables specified for async loads

---

## Memory

### M1: No leaks in ViewModel
- [ ] PASS / FAIL / NA — No references to `Context`, `View`, `Activity`, or `Fragment` in ViewModel
- [ ] PASS / FAIL / NA — No state mutations after `onCleared()` (cancelled job guard)
- [ ] PASS / FAIL / NA — `viewModelScope` used for coroutines (not `GlobalScope`)

### M2: No large bitmaps in memory
- [ ] PASS / FAIL / NA — Bitmaps loaded at display size (not full resolution); `inSampleSize` applied
- [ ] PASS / FAIL / NA — No raw bitmap references held in `State` objects

### M3: Lazy loading for lists
- [ ] PASS / FAIL / NA — Pagination implemented for lists exceeding 100 items (Paging 3 or manual offset)
- [ ] PASS / FAIL / NA — Prefetch distance configured; no loading entire dataset into memory before display
- [ ] PASS / FAIL / NA — `NoteListItem` is lightweight (no full content in list items)

### M4: Rust memory management
- [ ] PASS / FAIL / NA — Large Rust allocations freed promptly (RAII and Drop verified)
- [ ] PASS / FAIL / NA — No `Arc` cycles causing memory leaks; FFI strings/buffers ownership clear

### M5: LeakCanary / memory tooling
- [ ] PASS / FAIL / NA — LeakCanary installed in debug builds and reviewed before releases
- [ ] PASS / FAIL / NA — No `static` references to Activity or Context

---

## Startup

### S1: Deferred initialization
- [ ] PASS / FAIL / NA — Heavy init (DB, encryption setup) deferred to background thread
- [ ] PASS / FAIL / NA — Libraries initialized lazily on first access, not in `Application.onCreate`

### S2: No heavy work on main thread
- [ ] PASS / FAIL / NA — No DB queries, file I/O, or network on main thread (verified with StrictMode)
- [ ] PASS / FAIL / NA — UniFFI calls dispatched on `Dispatchers.IO`; no `runBlocking` in UI code

### S3: Startup profiling
- [ ] PASS / FAIL / NA — Baseline profile generated for startup path
- [ ] PASS / FAIL / NA — Cold start time measured and below threshold (e.g., <2s on mid-range device)
- [ ] PASS / FAIL / NA — `reportFullyDrawn()` called at correct point

### S4: Content provider trimming
- [ ] PASS / FAIL / NA — Custom `ContentProvider` init reviewed; `android:initOrder` set appropriately
- [ ] PASS / FAIL / NA — No third-party SDKs with heavy init that are unused

---

## Database

### D1: Query optimization
- [ ] PASS / FAIL / NA — All queries reviewed with `EXPLAIN QUERY PLAN`
- [ ] PASS / FAIL / NA — No full table scans on large tables; writes batched in transactions
- [ ] PASS / FAIL / NA — No `SELECT *` in production queries — explicit column selection

### D2: Indexes on searched columns
- [ ] PASS / FAIL / NA — `title`, `created_at`, `updated_at`, `is_archived` indexed
- [ ] PASS / FAIL / NA — Composite indexes for common query patterns (e.g., `(is_archived, created_at)`)
- [ ] PASS / FAIL / NA — No redundant indexes; index sizes monitored

### D3: No N+1 queries
- [ ] PASS / FAIL / NA — JOINs used instead of queries in loops
- [ ] PASS / FAIL / NA — Batch loading for related data (e.g., loading all tags for notes in one query)

### D4: SELECT specific columns
- [ ] PASS / FAIL / NA — `NoteListItem` query selects only `id`, `title`, `created_at`, `updated_at`
- [ ] PASS / FAIL / NA — No `SELECT *` anywhere; explicit column lists in all queries

### D5: Connection management
- [ ] PASS / FAIL / NA — WAL mode with `synchronous=NORMAL` verified; no connection leaks

---

## Rust Performance

### P1: Release profile with LTO
- [ ] PASS / FAIL / NA — `lto = true` (or `"fat"`) and `codegen-units = 1` in release profile
- [ ] PASS / FAIL / NA — `opt-level = "z"` or `"s"` considered; `panic = "abort"` set

### P2: Binary size checked
- [ ] PASS / FAIL / NA — `cargo bloat` or `cargo size` run on release build
- [ ] PASS / FAIL / NA — Largest functions reviewed for unnecessary monomorphization

### P3: No unnecessary allocations
- [ ] PASS / FAIL / NA — Hot paths avoid `String::clone` / `Vec::clone` (use references)
- [ ] PASS / FAIL / NA — `Cow<str>` considered for borrow-or-own patterns
- [ ] PASS / FAIL / NA — No `format!` in hot paths; serde uses pre-allocated `Vec<u8>`

### P4: Hot path profiling
- [ ] PASS / FAIL / NA — Encryption/decryption path profiled; DB operations under 50ms for typical payloads
- [ ] PASS / FAIL / NA — Argon2 key derivation timed (<500ms on target device)
- [ ] PASS / FAIL / NA — `#[inline]` annotations reviewed (not overused)

---

## Coroutines

### C1: No GlobalScope
- [ ] PASS / FAIL / NA — All coroutines launched in `viewModelScope`, `lifecycleScope`, or custom scopes with `SupervisorJob()`

### C2: Proper dispatcher usage
- [ ] PASS / FAIL / NA — `Dispatchers.IO` for DB/file ops; `Dispatchers.Default` for CPU work; `Dispatchers.Main` for UI only

### C3: No leaking coroutines
- [ ] PASS / FAIL / NA — ViewModel coroutines cancelled in `onCleared()`; lifecycle-aware collection used
- [ ] PASS / FAIL / NA — No `Job.join()` without timeout on main thread

### C4: Structured concurrency
- [ ] PASS / FAIL / NA — No orphaned `async` without `await()`; exception handling uses `CoroutineExceptionHandler`

---

## Network (Future-Proofing)

### N1: Caching strategy
- [ ] PASS / FAIL / NA — HTTP cache configured (OkHttp cache, `Cache-Control`); offline cache strategy designed
- [ ] PASS / FAIL / NA — Cache size bounded with eviction policy defined

### N2: Request batching
- [ ] PASS / FAIL / NA — Multiple small requests batched where possible; writes debounced (auto-save delay)
- [ ] PASS / FAIL / NA — No repeated identical requests in quick succession

### N3: Compression
- [ ] PASS / FAIL / NA — HTTP `Accept-Encoding: gzip` enabled; request body compressed for large uploads

### N4: Timeout handling
- [ ] PASS / FAIL / NA — Connect (10s), read (30s), and write (30s) timeouts configured
- [ ] PASS / FAIL / NA — Timeout errors handled gracefully (retry with backoff or user-visible message)

---

## Summary

| Section | Checks | PASS | FAIL | NA | Score |
|---|---|---|---|---|---|
| APK Size | 5 | - | - | - | - |
| Rendering | 5 | - | - | - | - |
| Memory | 5 | - | - | - | - |
| Startup | 4 | - | - | - | - |
| Database | 5 | - | - | - | - |
| Rust Perf | 4 | - | - | - | - |
| Coroutines | 4 | - | - | - | - |
| Network | 4 | - | - | - | - |
| **TOTAL** | **36** | **-** | **-** | **-** | **-** |

**Any FAIL requires a written remediation plan and re-review. Performance regression >10% from baseline blocks merge.**
