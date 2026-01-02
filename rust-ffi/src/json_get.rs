use crate::types::{max_json_size, ErrorCode};
use crate::{BUFFER_POOL, ZERO_COPY_THRESHOLD};
use ffi_safety_macro::ffi_safe_with_error;
use serde_json::Value;
use std::slice;
use std::str;

/// Get the size of string value from JSON by path (step 1 of 2-step extraction)
///
/// This function calculates the size needed for the output buffer without
/// actually copying the string data. This enables dynamic buffer allocation
/// on the Java side.
///
/// # Safety
/// - `json_ptr` must point to valid UTF-8 encoded JSON data of size `json_len`
/// - `path_ptr` must point to valid UTF-8 encoded path string of size `path_len`
/// - All pointers must remain valid during the call
///
/// # Returns
/// * Positive number - Size of string in bytes (excluding null terminator)
/// * `-1` - Null pointer
/// * `-2` - Invalid UTF-8
/// * `-3` - JSON too large
/// * `2` - Path not found (returned as negative: -2)
/// * `3` - Type mismatch (returned as negative: -3)
#[ffi_safe_with_error(-1)]
#[no_mangle]
pub unsafe extern "C" fn json_get_string_size(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
) -> isize {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() {
        return ErrorCode::NullPointer.as_i32() as isize;
    }

    let max_size = max_json_size();
    if json_len == 0 || json_len > max_size {
        return ErrorCode::TooLarge.as_i32() as isize;
    }

    // Convert to slices
    let json_slice = slice::from_raw_parts(json_ptr, json_len);
    let path_slice = slice::from_raw_parts(path_ptr, path_len);

    // Validate UTF-8
    let json_str = match str::from_utf8(json_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8.as_i32() as isize,
    };

    let path_str = match str::from_utf8(path_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8.as_i32() as isize,
    };

    // HYBRID EXTRACTION: Zero-copy for small JSON, SIMD for large
    let extracted = match extract_field_hybrid(json_str, path_str) {
        Ok(v) => v,
        Err(ErrorCode::PathNotFound) => return -(ErrorCode::PathNotFound.as_i32() as isize),
        Err(e) => return e.as_i32() as isize,
    };

    // Convert to string
    let result_str = match extracted.as_str() {
        Some(s) => s,
        None => return -(ErrorCode::TypeMismatch.as_i32() as isize),
    };

    // Return string length (in bytes, UTF-8)
    result_str.len() as isize
}

/// Extract string value from JSON by path (step 2 of 2-step extraction)
///
/// This function should be called after `json_get_string_size()` to get the
/// actual string data. The output buffer must be sized according to the
/// size returned by `json_get_string_size()` + 1 (for null terminator).
///
/// # Safety
/// - `json_ptr` must point to valid UTF-8 encoded JSON data of size `json_len`
/// - `path_ptr` must point to valid UTF-8 encoded path string of size `path_len`
/// - `output_buf` must point to writable memory of size `output_buf_len`
/// - All pointers must remain valid during the call
///
/// # Arguments
/// * `json_ptr` - Pointer to JSON data
/// * `json_len` - Length of JSON data in bytes
/// * `path_ptr` - Pointer to JSONPath query (e.g., "user.name" or "$.user.email")
/// * `path_len` - Length of path string
/// * `output_buf` - Buffer to write extracted string
/// * `output_buf_len` - Size of output buffer
///
/// # Returns
/// * `0` - Success, output_buf contains the extracted string
/// * `-1` - Null pointer
/// * `-2` - Invalid UTF-8
/// * `-3` - JSON too large
/// * `2` - Path not found
/// * `3` - Type mismatch (expected string, got different type)
#[ffi_safe_with_error(-1)]
#[no_mangle]
pub unsafe extern "C" fn json_get_string(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
    output_buf: *mut u8,
    output_buf_len: usize,
) -> i32 {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() || output_buf.is_null() {
        return ErrorCode::NullPointer.as_i32();
    }

    let max_size = max_json_size();
    if json_len == 0 || json_len > max_size {
        return ErrorCode::TooLarge.as_i32();
    }

    // Convert to slices
    let json_slice = slice::from_raw_parts(json_ptr, json_len);
    let path_slice = slice::from_raw_parts(path_ptr, path_len);

    // Validate UTF-8
    let json_str = match str::from_utf8(json_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8.as_i32(),
    };

    let path_str = match str::from_utf8(path_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8.as_i32(),
    };

    // HYBRID EXTRACTION: Zero-copy for small JSON, SIMD for large
    let extracted = match extract_field_hybrid(json_str, path_str) {
        Ok(v) => v,
        Err(e) => return e.as_i32(),
    };

    // Convert to string
    let result_str = match extracted.as_str() {
        Some(s) => s,
        None => return ErrorCode::TypeMismatch.as_i32(),
    };

    // Write to output buffer
    write_string_to_buffer(output_buf, output_buf_len, result_str);

    ErrorCode::Ok.as_i32()
}

/// Extract numeric value from JSON by path
///
/// # Safety
/// Same safety requirements as json_get_string
///
/// # Returns
/// * Positive value - Success, returns the extracted number as i64
/// * Negative - Error code (same as json_get_string)
#[ffi_safe_with_error(-1)]
#[no_mangle]
pub unsafe extern "C" fn json_get_number(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
    output: *mut f64,
) -> i32 {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() || output.is_null() {
        return ErrorCode::NullPointer.as_i32();
    }

    let max_size = max_json_size();
    if json_len == 0 || json_len > max_size {
        return ErrorCode::TooLarge.as_i32();
    }

    // Convert to slices
    let json_slice = slice::from_raw_parts(json_ptr, json_len);
    let path_slice = slice::from_raw_parts(path_ptr, path_len);

    // Validate UTF-8
    let json_str = match str::from_utf8(json_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8.as_i32(),
    };

    let path_str = match str::from_utf8(path_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8.as_i32(),
    };

    // HYBRID EXTRACTION: Zero-copy for small JSON, SIMD for large
    let extracted = match extract_field_hybrid(json_str, path_str) {
        Ok(v) => v,
        Err(e) => return e.as_i32(),
    };

    // Convert to number (support all numeric types: f64, i64, u64)
    let number = match extracted
        .as_f64()
        .or_else(|| extracted.as_i64().map(|n| n as f64))
        .or_else(|| extracted.as_u64().map(|n| n as f64))
    {
        Some(n) => n,
        None => return ErrorCode::TypeMismatch.as_i32(),
    };

    // Write result
    *output = number;

    ErrorCode::Ok.as_i32()
}

/// Extract boolean value from JSON by path
///
/// # Safety
/// Same safety requirements as json_get_string
#[ffi_safe_with_error(-1)]
#[no_mangle]
pub unsafe extern "C" fn json_get_bool(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
    output: *mut bool,
) -> i32 {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() || output.is_null() {
        return ErrorCode::NullPointer.as_i32();
    }

    let max_size = max_json_size();
    if json_len == 0 || json_len > max_size {
        return ErrorCode::TooLarge.as_i32();
    }

    // Convert to slices
    let json_slice = slice::from_raw_parts(json_ptr, json_len);
    let path_slice = slice::from_raw_parts(path_ptr, path_len);

    // Validate UTF-8
    let json_str = match str::from_utf8(json_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8.as_i32(),
    };

    let path_str = match str::from_utf8(path_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8.as_i32(),
    };

    // HYBRID EXTRACTION: Zero-copy for small JSON, SIMD for large
    let extracted = match extract_field_hybrid(json_str, path_str) {
        Ok(v) => v,
        Err(e) => return e.as_i32(),
    };

    // Convert to boolean
    let bool_val = match extracted.as_bool() {
        Some(b) => b,
        None => return ErrorCode::TypeMismatch.as_i32(),
    };

    // Write result
    *output = bool_val;

    ErrorCode::Ok.as_i32()
}

// ============================================
// Helper Functions
// ============================================

/// HYBRID field extraction: Zero-copy for small JSON, SIMD for large
///
/// This function chooses the optimal parsing strategy based on JSON size:
/// - Small JSON (< 1KB): serde_json for TRUE zero-copy
/// - Large JSON (>= 1KB): simd-json for SIMD acceleration
///
/// Returns the extracted value as serde_json::Value for uniform handling.
///
/// **SHARED HELPER**: This function is used by both field extraction (`json_get_*`)
/// and validators (`json_validate_email`, `json_validate_url`, etc.) to avoid
/// duplicating the parsing logic and ensure consistent zero-copy behavior.
pub(crate) fn extract_field_hybrid(json_str: &str, path: &str) -> Result<Value, ErrorCode> {
    if json_str.len() < ZERO_COPY_THRESHOLD {
        // Small JSON: TRUE zero-copy with serde_json
        // No buffer allocation, direct parsing from &str!
        let value: Value = serde_json::from_str(json_str).map_err(|_| ErrorCode::SyntaxError)?;

        extract_value_serde(&value, path).ok_or(ErrorCode::PathNotFound)
    } else {
        // Large JSON: SIMD acceleration with simd-json
        BUFFER_POOL.with(|pool| {
            let mut buffer = pool.borrow_mut();
            buffer.clear();
            buffer.extend_from_slice(json_str.as_bytes());

            let simd_value =
                simd_json::to_owned_value(&mut buffer).map_err(|_| ErrorCode::SyntaxError)?;

            // Convert simd-json value to serde_json value for uniform handling
            let value = simd_value_to_serde(&simd_value);

            extract_value_serde(&value, path).ok_or(ErrorCode::PathNotFound)
        })
    }
}

/// Extract value from serde_json::Value by path (zero-copy navigation)
///
/// Supports paths like:
/// - "user.name"
/// - "data.items.0.id" (array index)
/// - "$.user.email" (JSONPath style, $ is ignored)
fn extract_value_serde(value: &Value, path: &str) -> Option<Value> {
    // Remove leading $ if present (JSONPath style)
    let clean_path = path.strip_prefix("$.").unwrap_or(path);
    let clean_path = clean_path.strip_prefix('.').unwrap_or(clean_path);

    // Split path by dots
    let parts: Vec<&str> = clean_path.split('.').collect();

    let mut current = value;

    for part in parts {
        // Check if it's an array index
        if let Ok(index) = part.parse::<usize>() {
            current = current.get(index)?;
        } else {
            current = current.get(part)?;
        }
    }

    Some(current.clone())
}

/// Convert simd_json::OwnedValue to serde_json::Value
fn simd_value_to_serde(value: &simd_json::OwnedValue) -> Value {
    use simd_json::prelude::*;

    if value.is_null() {
        Value::Null
    } else if let Some(b) = value.as_bool() {
        Value::Bool(b)
    } else if let Some(i) = value.as_i64() {
        Value::Number(i.into())
    } else if let Some(u) = value.as_u64() {
        Value::Number(u.into())
    } else if let Some(f) = value.as_f64() {
        Value::Number(serde_json::Number::from_f64(f).unwrap_or(0.into()))
    } else if let Some(s) = value.as_str() {
        Value::String(s.to_string())
    } else if let Some(arr) = value.as_array() {
        Value::Array(arr.iter().map(simd_value_to_serde).collect())
    } else if let Some(obj) = value.as_object() {
        Value::Object(
            obj.iter()
                .map(|(k, v)| (k.to_string(), simd_value_to_serde(v)))
                .collect(),
        )
    } else {
        Value::Null
    }
}

/// Write string to C buffer with null terminator
fn write_string_to_buffer(buf: *mut u8, buf_len: usize, s: &str) {
    unsafe {
        if buf.is_null() || buf_len == 0 {
            return;
        }

        let bytes = s.as_bytes();
        let copy_len = bytes.len().min(buf_len - 1);

        if copy_len > 0 {
            let slice = slice::from_raw_parts_mut(buf, buf_len);
            slice[..copy_len].copy_from_slice(&bytes[..copy_len]);
            slice[copy_len] = 0; // Null terminator
        } else {
            *buf = 0; // Empty string
        }
    }
}

// ============================================
// Tests
// ============================================

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_json_get_string() {
        let json = r#"{"greeting":"Hello, World!"}"#;
        let path = "greeting";
        let mut output_buf = [0u8; 256];

        unsafe {
            let result = json_get_string(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                output_buf.as_mut_ptr(),
                output_buf.len(),
            );

            assert_eq!(result, 0);

            // Extract null-terminated string
            let output_str = std::ffi::CStr::from_ptr(output_buf.as_ptr() as *const i8)
                .to_str()
                .unwrap();
            assert_eq!(output_str, "Hello, World!");
        }
    }

    #[test]
    fn test_json_get_number() {
        let json = r#"{"price":99.99}"#;
        let path = "price";
        let mut output: f64 = 0.0;

        unsafe {
            let result = json_get_number(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                &mut output as *mut f64,
            );

            assert_eq!(result, 0);
            assert!((output - 99.99).abs() < 0.001);
        }
    }

    #[test]
    fn test_json_get_bool() {
        let json = r#"{"active":true}"#;
        let path = "active";
        let mut output: bool = false;

        unsafe {
            let result = json_get_bool(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                &mut output as *mut bool,
            );

            assert_eq!(result, 0);
            assert_eq!(output, true);
        }
    }
}
