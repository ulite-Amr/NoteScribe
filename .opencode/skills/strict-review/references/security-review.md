# Security Review Checklist

> Based on OWASP Mobile Top 10. Every check is **YES/NO/NA**.
> **BLOCKING** checks marked with `[BLOCKING]` must pass YES to ship.

---

## Data Storage

### [BLOCKING] S1: No hardcoded secrets
- [ ] YES / NO / NA — Any API keys, tokens, passwords, or private keys in source code (`.kt`, `.rs`, `.toml`, `.gradle`)
- [ ] YES / NO / NA — Any secrets in `BuildConfig` fields or `gradle.properties`
- [ ] YES / NO / NA — Any secrets in version control history
- [ ] YES / NO / NA — Secrets loaded from secure key store (e.g. Android KeyStore / env vars / CI secrets)

### [BLOCKING] S2: No sensitive data in SharedPreferences
- [ ] YES / NO / NA — `SharedPreferences` used for non-sensitive data only (UI prefs, feature flags)
- [ ] YES / NO / NA — EncryptedSharedPreferences used if any PII or tokens are persisted
- [ ] YES / NO / NA — No session tokens, auth tokens, or keys stored in plaintext

### [BLOCKING] S3: No logging of PII
- [ ] YES / NO / NA — No `Log.d`/`Log.i`/`Log.e` containing email, name, password, auth tokens, credit card numbers
- [ ] YES / NO / NA — No `println` or `System.out` in production code
- [ ] YES / NO / NA — No `Timber` or custom logger calls with PII in release builds
- [ ] YES / NO / NA — Rust `println!` / `eprintln!` / `log!` checked for PII exposure

### [BLOCKING] S4: Encryption format verified
- [ ] YES / NO / NA — AES-256-GCM used (not ECB, not CBC, not DES, not RC4)
- [ ] YES / NO / NA — Random 12-byte nonce generated per encryption (never reused)
- [ ] YES / NO / NA — Random 16-byte salt generated per key derivation
- [ ] YES / NO / NA — Output format matches: `[salt(16)][nonce(12)][ciphertext]`
- [ ] YES / NO / NA — Authenticated encryption (GCM tag) verified on decryption
- [ ] YES / NO / NA — No homegrown crypto — only reviewed libraries (ring, aes-gcm, argon2)

### [BLOCKING] S5: Zeroizing verified
- [ ] YES / NO / NA — `zeroize::Zeroizing` used for all secrets in Rust (`Password`, `MasterKey`, `DerivedKey`)
- [ ] YES / NO / NA — Memory explicitly zeroed after use (not relying on Drop alone if `Zeroize` available)
- [ ] YES / NO / NA — No secrets copied to unsecured buffers (e.g. `Vec<u8>` without `Zeroizing`)
- [ ] YES / NO / NA — Kotlin `ByteArray` secrets overwritten with `fill(0)` after use

### S6: SQLite database protection
- [ ] YES / NO / NA — Database file stored in app-internal storage (not external/SD card)
- [ ] YES / NO / NA — SQLCipher or similar encryption enabled if storing sensitive data
- [ ] YES / NO / NA — WAL mode with `synchronous=NORMAL` (as per project spec)
- [ ] YES / NO / NA — No world-readable database permissions

---

## Communication

### [BLOCKING] C1: All network calls use HTTPS
- [ ] YES / NO / NA — Every URL uses `https://` scheme (no `http://`)
- [ ] YES / NO / NA — No `android:usesCleartextTraffic="true"` in `AndroidManifest.xml`
- [ ] YES / NO / NA — No `NetworkSecurityPolicy` cleartext bypass programmatically
- [ ] YES / NO / NA — No cleartext `http://` in Rust HTTP client code

### [BLOCKING] C2: TLS configuration
- [ ] YES / NO / NA — TLS 1.2+ enforced (no TLS 1.0/1.1, no SSLv3)
- [ ] YES / NO / NA — Strong cipher suites only (no NULL, no EXPORT, no RC4, no DES, no 3DES)
- [ ] YES / NO / NA — Certificate validation enabled (not trusting all certs)
- [ ] YES / NO / NA — Hostname verification enabled

### C3: Certificate pinning (if applicable)
- [ ] YES / NO / NA — Certificate or public key pinning implemented for production endpoints
- [ ] YES / NO / NA — Backup pins configured in case of key rotation
- [ ] YES / NO / NA — Pinning failure handled gracefully (no crash, no data exposure)
- [ ] YES / NO / NA — OkHttp `CertificatePinner` or equivalent used

---

## Authentication

### [BLOCKING] A1: Password never stored
- [ ] YES / NO / NA — Password is hashed immediately with Argon2, never stored in plaintext
- [ ] YES / NO / NA — No password logging, debugging, or transmission in plaintext
- [ ] YES / NO / NA — Password cleared from memory after key derivation (`Zeroizing`)

### [BLOCKING] A2: Argon2 parameters verified
- [ ] YES / NO / NA — `argon2` crate or equivalent used (not a custom implementation)
- [ ] YES / NO / NA — Memory cost >= 64 MiB
- [ ] YES / NO / NA — Iterations >= 3
- [ ] YES / NO / NA — Parallelism >= 1 (appropriate for mobile device)
- [ ] YES / NO / NA — Output length >= 32 bytes (256 bits)
- [ ] YES / NO / NA — Salt is cryptographically random, unique per derivation

### A3: Biometric auth (if implemented)
- [ ] YES / NO / NA — `BiometricPrompt` with `BIOMETRIC_STRONG` or `BIOMETRIC_WEAK` as appropriate
- [ ] YES / NO / NA — `setAllowedAuthenticators(BIOMETRIC_STRONG)` for high-value operations
- [ ] YES / NO / NA — CryptoObject used with `KeyGenParameterSpec` (not just callback-based auth)
- [ ] YES / NO / NA — Fallback to app password/PIN if biometrics unavailable (with timeout)
- [ ] YES / NO / NA — Biometric auth `setConfirmationRequired(false)` justified

---

## Code Quality (Security)

### [BLOCKING] Q1: No debug code in release
- [ ] YES / NO / NA — No `if (BuildConfig.DEBUG)` gates that enable dangerous functionality
- [ ] YES / NO / NA — No debug overlays, debug menus, or debug endpoints in release builds
- [ ] YES / NO / NA — No `android:debuggable="true"` in release `AndroidManifest.xml`
- [ ] YES / NO / NA — No `StrictMode` enabled in release builds
- [ ] YES / NO / NA — No `testOnly="true"` in release APK manifest

### [BLOCKING] Q2: No backdoors
- [ ] YES / NO / NA — No hidden intents, activities, or broadcast receivers that bypass auth
- [ ] YES / NO / NA — No app shortcut / deep link that skips authentication screen
- [ ] YES / NO / NA — No hardcoded "master password" or override code in any language
- [ ] YES / NO / NA — No runtime-debug interfaces exposed (e.g., `WebView.addJavascriptInterface`)

### [BLOCKING] Q3: No leaked API keys
- [ ] YES / NO / NA — No API keys in `strings.xml`, `gradle.properties`, or `BuildConfig`
- [ ] YES / NO / NA — No API keys in `AndroidManifest.xml` `<meta-data>` tags
- [ ] YES / NO / NA — No API keys in `local.properties` (which is `.gitignore`d but safe)
- [ ] YES / NO / NA — API keys fetched at runtime from secure backend if needed

### [BLOCKING] Q4: ProGuard / R8 enforced
- [ ] YES / NO / NA — `minifyEnabled true` and `proguardFiles` configured in release build type
- [ ] YES / NO / NA — Obfuscation enabled (`useFullR8` or ProGuard)
- [ ] YES / NO / NA — Debug symbols stripped from release APK
- [ ] YES / NO / NA — `consumer-rules.pro` checked for over-sharing of internal types

---

## FFI Security (Rust -> Kotlin)

### [BLOCKING] F1: Input validation at FFI boundary
- [ ] YES / NO / NA — All `#[uniffi::export]` function parameters validated for length, range, and nullity
- [ ] YES / NO / NA — String lengths bounded (no unbounded allocation from FFI)
- [ ] YES / NO / NA — Integer parameters checked for overflow/underflow before use
- [ ] YES / NO / NA — No `unwrap()` or `expect()` on FFI input — always handle `Err` gracefully

### [BLOCKING] F2: Buffer overflow protection
- [ ] YES / NO / NA — No unsafe array indexing with user-controlled indices
- [ ] YES / NO / NA — All buffer sizes checked against actual data length before copy
- [ ] YES / NO / NA — No `std::mem::transmute` across FFI boundary
- [ ] YES / NO / NA — No pointer arithmetic exposed through UniFFI

### F3: JNI / UniFFI safe patterns
- [ ] YES / NO / NA — UniFFI used for all FFI (no raw JNI hand-written)
- [ ] YES / NO / NA — `#[uniffi::export]` functions avoid `unsafe` unless justified with `// SAFETY:`
- [ ] YES / NO / NA — No Kotlin `Unsafe` API usage in binding wrappers
- [ ] YES / NO / NA — Error types mapped properly (`NoteScribeError` -> Kotlin exception)

### F4: Memory safety
- [ ] YES / NO / NA — No Rust `unsafe` code without `// SAFETY:` comment and review
- [ ] YES / NO / NA — All Rust references across FFI have correct lifetimes
- [ ] YES / NO / NA — No `Box::into_raw` / `Box::from_raw` patterns without ownership tracking
- [ ] YES / NO / NA — Kotlin GC cannot observe Rust-allocated memory

---

## Dependency Security

### [BLOCKING] D1: Dependencies scanned for CVEs
- [ ] YES / NO / NA — `cargo-audit` run on workspace (no advisory warnings)
- [ ] YES / NO / NA — OWASP Dependency Check or equivalent run on Android dependencies
- [ ] YES / NO / NA — No known-critical or known-high CVE in any dependency
- [ ] YES / NO / NA — `cargo deny` configured with `advisory` and `license` checks

### D2: No deprecated libraries
- [ ] YES / NO / NA — No `deprecated` annotations used in dependencies (e.g. `AsyncTask`, `Handler`)
- [ ] YES / NO / NA — No use of `Apache HTTP Client` (removed in API 23)
- [ ] YES / NO / NA — No use of `android.arch.*` or other migrated AndroidX packages
- [ ] YES / NO / NA — Dependencies at latest stable unless pinned with justification

### D3: License compliance
- [ ] YES / NO / NA — All dependency licenses compatible with project license
- [ ] YES / NO / NA — `cargo deny` or license checker passes
- [ ] YES / NO / NA — Third-party notices file generated for release build

---

## Runtime Security

### R1: No reflection
- [ ] YES / NO / NA — No `Class.forName()` or `java.lang.reflect.*` in production code
- [ ] YES / NO / NA — No reflection-based serialization (Gson/Kotlinx Serialization fine)
- [ ] YES / NO / NA — No `method.invoke()` or `field.setAccessible(true)` in production

### [BLOCKING] R2: No dynamic code loading
- [ ] YES / NO / NA — No `DexClassLoader`, `PathClassLoader`, or `InMemoryDexClassLoader`
- [ ] YES / NO / NA — No `System.load()` with path from untrusted source
- [ ] YES / NO / NA — No downloaded/remotely-loaded code execution
- [ ] YES / NO / NA — No `WebView` JS interface that can load arbitrary code

### R3: Root detection / integrity
- [ ] YES / NO / NA — Root detection implemented if security requirements demand it
- [ ] YES / NO / NA — Emulator detection considered for sensitive operations
- [ ] YES / NO / NA — App integrity verified (APK signature check, not just rooted device)
- [ ] YES / NO / NA — No simple boolean toggle to bypass detection

### R4: Secure random
- [ ] YES / NO / NA — `java.security.SecureRandom` in Kotlin (not `java.util.Random`)
- [ ] YES / NO / NA — `Os.urandom` or `getrandom` in Rust (not `std::collections::rand`)
- [ ] YES / NO / NA — No seeding of `SecureRandom` with predictable value

---

## Summary

| Section | Checks | PASS | FAIL | NA | Score |
|---|---|---|---|---|---|
| Data Storage | 7 | - | - | - | - |
| Communication | 4 | - | - | - | - |
| Authentication | 3 | - | - | - | - |
| Code Quality | 4 | - | - | - | - |
| FFI Security | 4 | - | - | - | - |
| Dependencies | 3 | - | - | - | - |
| Runtime | 4 | - | - | - | - |
| **TOTAL** | **29** | **-** | **-** | **-** | **-** |

**All `[BLOCKING]` checks must be YES to ship.** Any NO on a BLOCKING check requires an explicit security exception signed by the security lead.
