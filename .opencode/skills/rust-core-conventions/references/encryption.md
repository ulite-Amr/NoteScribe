# Encryption Conventions

> AES-256-GCM, Argon2id key derivation, Zeroizing of secrets.

## Cryptographic Primitives

| Component | Algorithm / Parameter | Value |
|---|---|---|
| Key derivation | Argon2id | m=19456, t=2, p=1 (OWASP recommended) |
| Cipher | AES-256-GCM | Random 12-byte nonce per encryption |
| Salt | Random | 16 bytes |
| Output format | `[salt(16)][nonce(12)][ciphertext]` | IMMUTABLE |

## Dependencies

```toml
[dependencies]
aes-gcm = "0.10"
argon2 = "0.5"
rand = "0.8"
zeroize = { version = "1", features = ["derive"] }
```

## Blocking Rules

🔴 **Output format `[salt(16)][nonce(12)][ciphertext]` is IMMUTABLE.** Never change salt or nonce lengths. Doing so will break decryption of all existing data. This format is sacred.

```rust
const SALT_LEN: usize = 16;
const NONCE_LEN: usize = 12;
const KEY_LEN: usize = 32;
```

🔴 **ALL passwords and keys wrapped in `Zeroizing`.** This ensures secrets are zeroed on drop, preventing them from lingering in memory.

```rust
use zeroize::Zeroizing;

fn derive_key(password: &str, salt: &[u8]) -> Result<Zeroizing<Vec<u8>>, NoteScribeError> {
    let mut key = Zeroizing::new(vec![0u8; KEY_LEN]);
    let argon2 = argon2::Argon2::new(
        argon2::Algorithm::Argon2id,
        argon2::Version::V0x13,
        argon2::Params::new(19456, 2, 1, None).map_err(|e| NoteScribeError::Encryption {
            msg: format!("Argon2 params invalid: {e}"),
        })?,
    );

    argon2
        .hash_password_into(password.as_bytes(), salt, &mut key)
        .map_err(|e| NoteScribeError::Encryption {
            msg: format!("Key derivation failed: {e}"),
        })?;

    Ok(key)
}
```

🔴 **Password is NEVER stored.** Only the derived key material exists transiently in `Zeroizing` wrappers. No password hash, no encrypted password — the password enters as a function argument and is zeroed after use.

🔴 **`decrypt()` returns `WrongPassword` if authentication fails.** AES-GCM authenticates ciphertext; a failed authentication tag check means either the wrong password was used or data is corrupted. Do NOT distinguish between these cases to avoid oracle attacks.

```rust
pub fn decrypt(data: &[u8], password: &str) -> Result<Vec<u8>, NoteScribeError> {
    if data.len() < SALT_LEN + NONCE_LEN {
        return Err(NoteScribeError::Encryption {
            msg: "Invalid backup file".into(),
        });
    }

    let (salt, rest) = data.split_at(SALT_LEN);
    let (nonce_bytes, ciphertext) = rest.split_at(NONCE_LEN);

    let key = derive_key(password, salt)?;
    let cipher = Aes256Gcm::new_from_slice(&key).map_err(|e| NoteScribeError::Encryption {
        msg: format!("Failed to create cipher: {e}"),
    })?;

    let nonce = Nonce::from_slice(nonce_bytes);
    let plaintext = cipher
        .decrypt(nonce, ciphertext)
        .map_err(|_| NoteScribeError::WrongPassword)?;

    Ok(plaintext)
}
```

🔴 **`encrypt()` and `decrypt()` are pure functions.** No state, no side effects, no self. They are deterministic given the same inputs (except for random salt/nonce). They do not touch the database, filesystem, or network.

```rust
pub fn encrypt(plaintext: &[u8], password: &str) -> Result<Vec<u8>, NoteScribeError> {
    let mut salt = [0u8; SALT_LEN];
    let mut nonce_bytes = [0u8; NONCE_LEN];
    rand::thread_rng().fill_bytes(&mut salt);
    rand::thread_rng().fill_bytes(&mut nonce_bytes);

    let key = derive_key(password, &salt)?;
    let cipher = Aes256Gcm::new_from_slice(&key).map_err(|e| NoteScribeError::Encryption {
        msg: format!("Failed to create cipher: {e}"),
    })?;

    let nonce = Nonce::from_slice(&nonce_bytes);
    let ciphertext = cipher
        .encrypt(nonce, plaintext)
        .map_err(|e| NoteScribeError::Encryption {
            msg: format!("Encryption failed: {e}"),
        })?;

    let mut result = Vec::with_capacity(SALT_LEN + NONCE_LEN + ciphertext.len());
    result.extend_from_slice(&salt);
    result.extend_from_slice(&nonce_bytes);
    result.extend_from_slice(&ciphertext);

    Ok(result)
}
```

🔴 **Plaintext is NEVER logged or exposed.** Debug print of encrypted data is acceptable. Debug print of plaintext or password is a blocker.

```rust
// 🔴 WRONG — never log plaintext
println!("Decrypted content: {plaintext}");

// ✅ CORRECT — log length only if necessary
println!("Decrypted {} bytes", plaintext.len());
```

🔴 **Timing side-channels: AES-GCM provides authenticated encryption.** AES-GCM uses constant-time operations for encryption and authentication tag verification. No additional constant-time comparison is needed. Do NOT attempt to compare MACs manually — the GCM implementation handles this.

## Full Module Template

```rust
use aes_gcm::aead::Aead;
use aes_gcm::{Aes256Gcm, KeyInit, Nonce};
use argon2::Argon2;
use rand::RngCore;
use zeroize::Zeroizing;

use crate::error::NoteScribeError;

const SALT_LEN: usize = 16;
const NONCE_LEN: usize = 12;
const KEY_LEN: usize = 32;

fn derive_key(password: &str, salt: &[u8]) -> Result<Zeroizing<Vec<u8>>, NoteScribeError> {
    let mut key = Zeroizing::new(vec![0u8; KEY_LEN]);
    let argon2 = argon2::Argon2::new(
        argon2::Algorithm::Argon2id,
        argon2::Version::V0x13,
        argon2::Params::new(19456, 2, 1, None).map_err(|e| NoteScribeError::Encryption {
            msg: format!("Argon2 params invalid: {e}"),
        })?,
    );
    argon2
        .hash_password_into(password.as_bytes(), salt, &mut key)
        .map_err(|e| NoteScribeError::Encryption {
            msg: format!("Key derivation failed: {e}"),
        })?;
    Ok(key)
}

pub fn encrypt(plaintext: &[u8], password: &str) -> Result<Vec<u8>, NoteScribeError> {
    let mut salt = [0u8; SALT_LEN];
    let mut nonce_bytes = [0u8; NONCE_LEN];
    rand::thread_rng().fill_bytes(&mut salt);
    rand::thread_rng().fill_bytes(&mut nonce_bytes);

    let key = derive_key(password, &salt)?;
    let cipher = Aes256Gcm::new_from_slice(&key)
        .map_err(|e| NoteScribeError::Encryption { msg: e.to_string() })?;
    let nonce = Nonce::from_slice(&nonce_bytes);

    let ciphertext = cipher
        .encrypt(nonce, plaintext)
        .map_err(|e| NoteScribeError::Encryption { msg: e.to_string() })?;

    let mut result = Vec::with_capacity(SALT_LEN + NONCE_LEN + ciphertext.len());
    result.extend_from_slice(&salt);
    result.extend_from_slice(&nonce_bytes);
    result.extend_from_slice(&ciphertext);

    Ok(result)
}

pub fn decrypt(data: &[u8], password: &str) -> Result<Vec<u8>, NoteScribeError> {
    if data.len() < SALT_LEN + NONCE_LEN {
        return Err(NoteScribeError::Encryption {
            msg: "Invalid backup file".to_string(),
        });
    }

    let (salt, rest) = data.split_at(SALT_LEN);
    let (nonce_bytes, ciphertext) = rest.split_at(NONCE_LEN);

    let key = derive_key(password, salt)?;
    let cipher = Aes256Gcm::new_from_slice(&key)
        .map_err(|e| NoteScribeError::Encryption { msg: e.to_string() })?;
    let nonce = Nonce::from_slice(nonce_bytes);

    let plaintext = cipher
        .decrypt(nonce, ciphertext)
        .map_err(|_| NoteScribeError::WrongPassword)?;

    Ok(plaintext)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_encrypt_decrypt_roundtrip() {
        let plaintext = b"Hello, NoteScribe!";
        let password = "correct-horse-battery-staple";
        let encrypted = encrypt(plaintext, password).unwrap();
        let decrypted = decrypt(&encrypted, password).unwrap();
        assert_eq!(decrypted, plaintext);
    }

    #[test]
    fn test_decrypt_wrong_password_fails() {
        let plaintext = b"Secret note content";
        let encrypted = encrypt(plaintext, "correct-password").unwrap();
        let result = decrypt(&encrypted, "wrong-password");
        assert!(matches!(result, Err(NoteScribeError::WrongPassword)));
    }

    #[test]
    fn test_decrypt_truncated_data_fails() {
        let result = decrypt(&[0u8; 10], "password");
        assert!(matches!(result, Err(NoteScribeError::Encryption { .. })));
    }
}
```
