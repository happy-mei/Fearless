// src/wasm_bindings.rs
#![cfg(feature = "wasm")]

use wasm_bindgen::prelude::*;
use unicode_segmentation::UnicodeSegmentation;
use unicode_normalization::UnicodeNormalization;
use regex::Regex;
use seahash;

// ========================
// String utilities
// ========================

#[wasm_bindgen]
pub fn hash_string(s: &str) -> u64 {
    // Hashes the string using Seahash
    seahash::hash(s.as_bytes())
}

#[wasm_bindgen]
pub fn normalise_string(s: &str) -> String {
    // Normalises the string using NFC (Normalization Form C)
    s.nfc().collect::<String>()
}

#[wasm_bindgen]
pub fn index_string(s: &str) -> js_sys::Int32Array {
    // Returns the indices of grapheme clusters in the string
    let indices: Vec<i32> = s
        .grapheme_indices(true)
        .map(|(idx, _)| idx as i32)
        .collect();
    js_sys::Int32Array::from(&indices[..])
}

// ========================
// Regex utilities
// ========================

#[wasm_bindgen]
pub fn compile_regex(pattern: &str) -> Result<JsValue, String> {
    // Compiles a regex pattern and returns a JsValue
    // Returns an error if the pattern is invalid
    Regex::new(pattern)
        .map(|_re| JsValue::from_str(pattern))
        .map_err(|e| e.to_string())
}

#[wasm_bindgen]
pub fn does_regex_match(pattern: &str, text: &str) -> bool {
    // Checks if the text matches the regex pattern
    match Regex::new(pattern) {
        Ok(re) => re.is_match(text),
        Err(_) => false,
    }
}
