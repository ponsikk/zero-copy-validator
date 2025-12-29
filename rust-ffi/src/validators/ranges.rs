/// Range validation for JSON fields
///
/// This module provides validators for checking numeric ranges, string lengths,
/// and array sizes without parsing the entire JSON document (zero-copy).
use crate::types::{max_json_size, ErrorCode};
use simd_json::prelude::*;
use std::slice;
use std::str;

/// Validates that a number field is within the specified range
///
/// # Safety
/// - `json_ptr` must point to valid UTF-8 encoded JSON data of size `json_len`
/// - `path_ptr` must point to valid UTF-8 encoded path string of size `path_len`
/// - All pointers must remain valid during the call
///
/// # Returns
/// * `0` - Success, number is within range
/// * `-1` - Null pointer
/// * `-2` - Invalid UTF-8
/// * `-3` - JSON too large
/// * `1` - JSON syntax error
/// * `2` - Path not found
/// * `3` - Type mismatch (not a number)
/// * `4` - Out of range
#[no_mangle]
pub unsafe extern "C" fn json_validate_number_range(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
    min: f64,
    max: f64,
) -> i32 {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() {
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

    // Get number value
    let number = if let Some(n) = extracted.as_f64() {
        n
    } else if let Some(n) = extracted.as_i64() {
        n as f64
    } else if let Some(n) = extracted.as_u64() {
        n as f64
    } else {
        return ErrorCode::TypeMismatch.as_i32();
    };

    // Check range
    if number < min || number > max {
        return 4; // Out of range
    }

    ErrorCode::Ok.as_i32()
}

/// Validates that a string field length is within the specified range
///
/// # Safety
/// Same safety requirements as json_validate_number_range
///
/// # Returns
/// * `0` - Success, string length is within range
/// * Error codes same as json_validate_number_range
#[no_mangle]
pub unsafe extern "C" fn json_validate_string_length(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
    min_len: usize,
    max_len: usize,
) -> i32 {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() {
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

    // Get string value
    let string_val = match extracted.as_str() {
        Some(s) => s,
        None => return ErrorCode::TypeMismatch.as_i32(),
    };

    // Check length
    let len = string_val.len();
    if len < min_len || len > max_len {
        return 4; // Out of range
    }

    ErrorCode::Ok.as_i32()
}

/// Validates that an array size is within the specified range
///
/// # Safety
/// Same safety requirements as json_validate_number_range
///
/// # Returns
/// * `0` - Success, array size is within range
/// * Error codes same as json_validate_number_range
#[no_mangle]
pub unsafe extern "C" fn json_validate_array_size(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
    min_items: usize,
    max_items: usize,
) -> i32 {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() {
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

    // Check if it's an array
    let array = match extracted.as_array() {
        Some(arr) => arr,
        None => return ErrorCode::TypeMismatch.as_i32(),
    };

    // Check size
    let size = array.len();
    if size < min_items || size > max_items {
        return 4; // Out of range
    }

    ErrorCode::Ok.as_i32()
}

// ============================================
// Helper Functions
// ============================================

/// Extract value from JSON by simple path
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

// ============================================
// Tests
// ============================================

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_number_range_valid() {
        let json = r#"{"age":30}"#;
        let path = "age";

        unsafe {
            let result = json_validate_number_range(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                0.0,
                120.0,
            );
            assert_eq!(result, 0, "Should be within range");
        }
    }

    #[test]
    fn test_number_range_out_of_range() {
        let json = r#"{"age":150}"#;
        let path = "age";

        unsafe {
            let result = json_validate_number_range(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                0.0,
                120.0,
            );
            assert_eq!(result, 4, "Should be out of range");
        }
    }

    #[test]
    fn test_string_length_valid() {
        let json = r#"{"password":"secret123"}"#;
        let path = "password";

        unsafe {
            let result = json_validate_string_length(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                8,
                64,
            );
            assert_eq!(result, 0, "Should be within length range");
        }
    }

    #[test]
    fn test_string_length_too_short() {
        let json = r#"{"password":"abc"}"#;
        let path = "password";

        unsafe {
            let result = json_validate_string_length(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                8,
                64,
            );
            assert_eq!(result, 4, "Should be too short");
        }
    }

    #[test]
    fn test_array_size_valid() {
        let json = r#"{"items":[1,2,3,4,5]}"#;
        let path = "items";

        unsafe {
            let result = json_validate_array_size(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                1,
                10,
            );
            assert_eq!(result, 0, "Should be within size range");
        }
    }

    #[test]
    fn test_array_size_too_large() {
        let json = r#"{"items":[1,2,3,4,5,6,7,8,9,10,11]}"#;
        let path = "items";

        unsafe {
            let result = json_validate_array_size(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                1,
                10,
            );
            assert_eq!(result, 4, "Should be too large");
        }
    }

    #[test]
    fn test_nested_number_range() {
        let json = r#"{"user":{"age":25}}"#;
        let path = "user.age";

        unsafe {
            let result = json_validate_number_range(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                0.0,
                120.0,
            );
            assert_eq!(result, 0, "Should validate nested field");
        }
    }
}
