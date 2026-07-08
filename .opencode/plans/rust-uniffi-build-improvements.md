# Plan: Make Rust→Android build a true one-button workflow

## Goal
Ensure a single Gradle build (Run / Build APK) reliably cross-compiles Rust, regenerates
UniFFI Kotlin bindings from up-to-date code, and packages the app — on any machine, with no
manual `cargo` commands and no `ANDROID_HOME` env wiring.

## Current state (working but fragile)
- `app/build.gradle.kts`: `preBuild` depends on `cargoBuildAll`, `copyAllSo`, `copyGeneratedBindings`.
- `generateUniFFIBindings` reads `notescribe-core/target/release/libnotescribe_core.so` (host build)
  but does **NOT** depend on building that host lib.
- NDK path derived from `ANDROID_HOME` env, defaulting to `/home/amr/Android/Sdk`.

### Gaps
1. Editing Rust then building recompiles Android `.so` but may regenerate bindings from a stale
   host lib → UniFFI checksum mismatch → runtime crash.
2. Fresh checkout (no `target/release`) → `generateUniFFIBindings` fails immediately.
3. Requires `ANDROID_HOME=/home/amr/Android/Sdk` prefix on every command.

## Changes — all in `app/build.gradle.kts`

### 1. NDK path from `local.properties` (robust, AGP-standard)
Replace:
```kotlin
val ndkVersion = "27.2.12479018"
val sdkDir = System.getenv("ANDROID_HOME") ?: "/home/amr/Android/Sdk"
val ndkPath = "$sdkDir/ndk/$ndkVersion"
```
With:
```kotlin
val ndkVersion = "27.2.12479018"
val localPropsFile = rootProject.file("local.properties")
val sdkDir = if (localPropsFile.exists()) {
    localPropsFile.readLines()
        .firstOrNull { it.startsWith("sdk.dir=") }
        ?.removePrefix("sdk.dir=")
        ?.trim()
} ?: System.getenv("ANDROID_HOME") ?: "/home/amr/Android/Sdk"
val ndkPath = "$sdkDir/ndk/$ndkVersion"
```

### 2. Add host build task (so bindings always match current Rust source)
Directly after the `ndkPath` block, add:
```kotlin
tasks.register<Exec>("cargoBuildHost") {
    workingDir(rustProjectDir)
    commandLine("cargo", "build", "--release")
}
```

### 3. Wire bindings generation to depend on the host build
In `generateUniFFIBindings`, add `dependsOn("cargoBuildHost")` as the first line inside the task:
```kotlin
tasks.register<Exec>("generateUniFFIBindings") {
    dependsOn("cargoBuildHost")
    workingDir(rustProjectDir)
    val outputDir = layout.buildDirectory.get().asFile.resolve("generated/uniffi")
    outputDir.mkdirs()
    commandLine(
        "uniffi-bindgen", "generate",
        "--library", File(rustProjectDir, "target/release/libnotescribe_core.so").absolutePath,
        "--language", "kotlin",
        "--out-dir", outputDir.absolutePath
    )
}
```

## What stays the same
- `preBuild` chain (cargoBuildAll → copyAllSo → copyGeneratedBindings) already triggers everything.
- `notescribe_core.kt` remains committed in `src/main/java/.../core/` and is overwritten each build.
- JNA dependency and ProGuard rules already in place.

## Verification (after plan approved & executed)
```bash
cd /home/amr/AndroidStudioProjects/NoteScribe
./gradlew :app:clean
./gradlew :app:assembleUniversalDebug        # no ANDROID_HOME prefix needed
```
Expected: BUILD SUCCESSFUL; APK contains `lib/{abi}/libnotescribe_core.so` for all 4 ABIs;
`notescribe_core.kt` regenerated from current Rust source (verify by touching a Rust file and
rebuilding — should succeed with no checksum error).
