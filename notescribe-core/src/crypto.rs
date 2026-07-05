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
    let argon2 = Argon2::default();
    argon2
        .hash_password_into(password.as_bytes(), salt, &mut key)
        .map_err(|e| NoteScribeError::Encryption { msg: e.to_string() })?;
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
