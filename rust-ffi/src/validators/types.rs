/// Type validation for JSON fields
///
/// This module provides validators for checking field types, existence, and null values
/// without parsing the entire JSON document (zero-copy).
use crate::types::{max_json_size, JsonType};
use simd_json::prelude::*;
use std::slice;
use std::str;

/// Validates that a JSON field has the expected type
///
/// # Safety
/// - `json_ptr` must point to valid UTF-8 encoded JSON data of size `json_len`
/// - `path_ptr` must point to valid UTF-8 encoded path string of size `path_len`
/// - All pointers must remain valid during the call
///
/// # Arguments
/// * `json_ptr` - Pointer to JSON data
/// * `json_len` - Length of JSON data in bytes
/// * `path_ptr` - Pointer to JSONPath query (e.g., "user.name")
/// * `path_len` - Length of path string
/// * `expected_type` - Expected type as i32 (from JsonType enum)
///
/// # Returns
/// * `true` - Field exists and has expected type
/// * `false` - Field doesn't exist, has different type, or validation error
#[no_mangle]
pub unsafe extern "C" fn json_validate_field_type(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
    expected_type: i32,
) -> bool {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() {
        return false;
    }

    let max_size = max_json_size();
    if json_len == 0 || json_len > max_size {
        return false;
    }

    // Convert to slices
    let json_slice = slice::from_raw_parts(json_ptr, json_len);
    let path_slice = slice::from_raw_parts(path_ptr, path_len);

    // Validate UTF-8
    let json_str = match str::from_utf8(json_slice) {
        Ok(s) => s,
        Err(_) => return false,
    };

    let path_str = match str::from_utf8(path_slice) {
        Ok(s) => s,
        Err(_) => return false,
    };

    // Parse JSON
    let mut bytes = json_str.as_bytes().to_vec();
    let value = match simd_json::to_owned_value(&mut bytes) {
        Ok(v) => v,
        Err(_) => return false,
    };

    // Extract field by path
    let extracted = match extract_value_by_path(&value, path_str) {
        Some(v) => v,
        None => return false,
    };

    // Check type
    let actual_type = get_value_type(extracted);
    actual_type as i32 == expected_type
}

/// Checks if a JSON field exists at the given path
///
/// # Safety
/// Same safety requirements as json_validate_field_type
///
/// # Returns
/// * `true` - Field exists (regardless of type or value)
/// * `false` - Field doesn't exist or validation error
#[no_mangle]
pub unsafe extern "C" fn json_field_exists(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
) -> bool {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() {
        return false;
    }

    let max_size = max_json_size();
    if json_len == 0 || json_len > max_size {
        return false;
    }

    // Convert to slices
    let json_slice = slice::from_raw_parts(json_ptr, json_len);
    let path_slice = slice::from_raw_parts(path_ptr, path_len);

    // Validate UTF-8
    let json_str = match str::from_utf8(json_slice) {
        Ok(s) => s,
        Err(_) => return false,
    };

    let path_str = match str::from_utf8(path_slice) {
        Ok(s) => s,
        Err(_) => return false,
    };

    // Parse JSON
    let mut bytes = json_str.as_bytes().to_vec();
    let value = match simd_json::to_owned_value(&mut bytes) {
        Ok(v) => v,
        Err(_) => return false,
    };

    // Check if field exists
    extract_value_by_path(&value, path_str).is_some()
}

/// Checks if a JSON field is null
///
/// # Safety
/// Same safety requirements as json_validate_field_type
///
/// # Returns
/// * `true` - Field exists AND is null
/// * `false` - Field doesn't exist, is not null, or validation error
#[no_mangle]
pub unsafe extern "C" fn json_field_is_null(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
) -> bool {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() {
        return false;
    }

    let max_size = max_json_size();
    if json_len == 0 || json_len > max_size {
        return false;
    }

    // Convert to slices
    let json_slice = slice::from_raw_parts(json_ptr, json_len);
    let path_slice = slice::from_raw_parts(path_ptr, path_len);

    // Validate UTF-8
    let json_str = match str::from_utf8(json_slice) {
        Ok(s) => s,
        Err(_) => return false,
    };

    let path_str = match str::from_utf8(path_slice) {
        Ok(s) => s,
        Err(_) => return false,
    };

    // Parse JSON
    let mut bytes = json_str.as_bytes().to_vec();
    let value = match simd_json::to_owned_value(&mut bytes) {
        Ok(v) => v,
        Err(_) => return false,
    };

    // Extract field and check if null
    match extract_value_by_path(&value, path_str) {
        Some(v) => v.is_null(),
        None => false,
    }
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

/// Determine the type of a JSON value
fn get_value_type(value: &simd_json::OwnedValue) -> JsonType {
    if value.is_null() {
        JsonType::Null
    } else if value.is_bool() {
        JsonType::Boolean
    } else if value.is_number() {
        JsonType::Number
    } else if value.is_str() {
        JsonType::String
    } else if value.is_array() {
        JsonType::Array
    } else if value.is_object() {
        JsonType::Object
    } else {
        JsonType::Null // fallback
    }
}

// ============================================
// Tests
// ============================================

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_validate_field_type_string() {
        let json = r#"{"name":"John","age":30}"#;
        let path = "name";

        unsafe {
            let result = json_validate_field_type(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                JsonType::String as i32,
            );
            assert!(result, "Should validate string type");
        }
    }

    #[test]
    fn test_validate_field_type_number() {
        let json = r#"{"name":"John","age":30}"#;
        let path = "age";

        unsafe {
            let result = json_validate_field_type(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                JsonType::Number as i32,
            );
            assert!(result, "Should validate number type");
        }
    }

    #[test]
    fn test_validate_field_type_mismatch() {
        let json = r#"{"name":"John","age":30}"#;
        let path = "name";

        unsafe {
            let result = json_validate_field_type(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                JsonType::Number as i32,
            );
            assert!(!result, "Should fail on type mismatch");
        }
    }

    #[test]
    fn test_field_exists() {
        let json = r#"{"name":"John","age":30}"#;
        let path = "name";

        unsafe {
            let result = json_field_exists(json.as_ptr(), json.len(), path.as_ptr(), path.len());
            assert!(result, "Field should exist");
        }
    }

    #[test]
    fn test_field_not_exists() {
        let json = r#"{"name":"John","age":30}"#;
        let path = "email";

        unsafe {
            let result = json_field_exists(json.as_ptr(), json.len(), path.as_ptr(), path.len());
            assert!(!result, "Field should not exist");
        }
    }

    #[test]
    fn test_field_is_null() {
        let json = r#"{"name":"John","middle":null,"age":30}"#;
        let path = "middle";

        unsafe {
            let result = json_field_is_null(json.as_ptr(), json.len(), path.as_ptr(), path.len());
            assert!(result, "Field should be null");
        }
    }

    #[test]
    fn test_field_is_not_null() {
        let json = r#"{"name":"John","age":30}"#;
        let path = "name";

        unsafe {
            let result = json_field_is_null(json.as_ptr(), json.len(), path.as_ptr(), path.len());
            assert!(!result, "Field should not be null");
        }
    }

    #[test]
    fn test_nested_field_type() {
        let json = r#"{"user":{"name":"Alice","id":123}}"#;
        let path = "user.name";

        unsafe {
            let result = json_validate_field_type(
                json.as_ptr(),
                json.len(),
                path.as_ptr(),
                path.len(),
                JsonType::String as i32,
            );
            assert!(result, "Should validate nested field type");
        }
    }
}
