use crate::types::{max_json_size, ErrorCode};
use simd_json::prelude::*;
use std::slice;
use std::str;

/// Extract string value from JSON by path (zero-copy)
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

    // Parse JSON
    let mut bytes = json_str.as_bytes().to_vec();
    let value = match simd_json::to_owned_value(&mut bytes) {
        Ok(v) => v,
        Err(_) => return ErrorCode::SyntaxError.as_i32(),
    };

    // Extract field by path
    let extracted = match extract_value_by_path(&value, path_str) {
        Some(v) => v,
        None => return ErrorCode::PathNotFound.as_i32(),
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

    // Parse JSON
    let mut bytes = json_str.as_bytes().to_vec();
    let value = match simd_json::to_owned_value(&mut bytes) {
        Ok(v) => v,
        Err(_) => return ErrorCode::SyntaxError.as_i32(),
    };

    // Extract field by path
    let extracted = match extract_value_by_path(&value, path_str) {
        Some(v) => v,
        None => return ErrorCode::PathNotFound.as_i32(),
    };

    // Convert to number (support all numeric types: f64, i64, u64)
    let number = if let Some(n) = extracted.as_f64() {
        n
    } else if let Some(n) = extracted.as_i64() {
        n as f64
    } else if let Some(n) = extracted.as_u64() {
        n as f64
    } else {
        return ErrorCode::TypeMismatch.as_i32();
    };

    // Write result
    *output = number;

    ErrorCode::Ok.as_i32()
}

/// Extract boolean value from JSON by path
///
/// # Safety
/// Same safety requirements as json_get_string
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

    // Parse JSON
    let mut bytes = json_str.as_bytes().to_vec();
    let value = match simd_json::to_owned_value(&mut bytes) {
        Ok(v) => v,
        Err(_) => return ErrorCode::SyntaxError.as_i32(),
    };

    // Extract field by path
    let extracted = match extract_value_by_path(&value, path_str) {
        Some(v) => v,
        None => return ErrorCode::PathNotFound.as_i32(),
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

/// Extract value from JSON by simple path
///
/// Supports paths like:
/// - "user.name"
/// - "data.items.0.id" (array index)
/// - "$.user.email" (JSONPath style, $ is ignored)
fn extract_value_by_path<'a>(
    value: &'a simd_json::OwnedValue,
    path: &str,
) -> Option<&'a simd_json::OwnedValue> {
    // Remove leading $ if present (JSONPath style)
    let clean_path = path.strip_prefix("$.").unwrap_or(path);
    let clean_path = clean_path.strip_prefix('.').unwrap_or(clean_path);

    // Split path by dots
    let parts: Vec<&str> = clean_path.split('.').collect();

    let mut current = value;

    for part in parts {
        // Check if it's an array index
        if let Ok(index) = part.parse::<usize>() {
            // Access array element by index
            current = current.get_idx(index)?;
        } else {
            // Access object field by key
            current = current.get(part)?;
        }
    }

    Some(current)
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
    fn test_extract_simple_field() {
        let json = r#"{"name":"Alice","age":30}"#;
        let mut bytes = json.as_bytes().to_vec();
        let value = simd_json::to_owned_value(&mut bytes).unwrap();

        let result = extract_value_by_path(&value, "name");
        assert!(result.is_some());
        assert_eq!(result.unwrap().as_str().unwrap(), "Alice");
    }

    #[test]
    fn test_extract_nested_field() {
        let json = r#"{"user":{"name":"Bob","email":"bob@example.com"}}"#;
        let mut bytes = json.as_bytes().to_vec();
        let value = simd_json::to_owned_value(&mut bytes).unwrap();

        let result = extract_value_by_path(&value, "user.email");
        assert!(result.is_some());
        assert_eq!(result.unwrap().as_str().unwrap(), "bob@example.com");
    }

    #[test]
    fn test_extract_array_element() {
        let json = r#"{"items":[{"id":1},{"id":2},{"id":3}]}"#;
        let mut bytes = json.as_bytes().to_vec();
        let value = simd_json::to_owned_value(&mut bytes).unwrap();

        let result = extract_value_by_path(&value, "items.1.id");
        assert!(result.is_some());
        assert_eq!(result.unwrap().as_i64().unwrap(), 2);
    }

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
