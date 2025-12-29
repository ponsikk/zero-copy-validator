mod json_get;
mod types;

use std::slice;
use std::str;
use types::{max_json_size, ErrorCode};

// Re-export json_get functions
pub use json_get::{json_get_bool, json_get_number, json_get_string};

/// Validates JSON string (zero-copy)
///
/// # Safety
/// - `ptr` must point to valid memory of size `len` bytes
/// - Memory must exist during the entire call
/// - Data must be valid UTF-8
///
/// # Returns
/// - `true` if JSON is valid
/// - `false` if JSON is invalid or error occurred
#[no_mangle]
pub unsafe extern "C" fn json_validate(ptr: *const u8, len: usize) -> bool {
    // Check for null pointer
    if ptr.is_null() {
        return false;
    }

    // Check for reasonable size (configurable via MAX_JSON_SIZE env)
    let max_size = max_json_size();
    if len == 0 || len > max_size {
        return false;
    }

    // Create slice from raw pointer
    let slice = unsafe { slice::from_raw_parts(ptr, len) };

    // Check UTF-8 validity
    let text = match str::from_utf8(slice) {
        Ok(s) => s,
        Err(_) => return false,
    };

    // Validate JSON using simd-json
    // Note: simd-json requires mutable data, so we need to copy
    let mut bytes = text.as_bytes().to_vec();

    // Use owned value to avoid lifetime issues
    simd_json::to_owned_value(&mut bytes).is_ok()
}

/// Validates JSON and returns detailed error code
///
/// # Safety
/// - `ptr` must point to valid memory of size `len` bytes
/// - `error_buf` must point to valid memory of size `error_buf_len` bytes, or be null
/// - Memory must remain valid during the entire call
/// - Data at `ptr` must be valid UTF-8
///
/// # Returns
/// - `0` - successful validation
/// - `-1` - null pointer
/// - `-2` - invalid UTF-8
/// - `-3` - JSON too large
/// - `1` - JSON syntax error
#[no_mangle]
pub unsafe extern "C" fn json_validate_detailed(
    ptr: *const u8,
    len: usize,
    error_buf: *mut u8,
    error_buf_len: usize,
) -> i32 {
    // Check for null pointer
    if ptr.is_null() {
        write_error(error_buf, error_buf_len, "Null pointer");
        return -1;
    }

    // Check for reasonable size (configurable via MAX_JSON_SIZE env)
    let max_size = max_json_size();
    if len == 0 || len > max_size {
        let msg = format!("JSON too large or empty (max: {} bytes)", max_size);
        write_error(error_buf, error_buf_len, &msg);
        return ErrorCode::TooLarge.as_i32();
    }

    // Create slice from raw pointer
    let slice = unsafe { slice::from_raw_parts(ptr, len) };

    // Check UTF-8 validity
    let text = match str::from_utf8(slice) {
        Ok(s) => s,
        Err(e) => {
            write_error(error_buf, error_buf_len, &format!("Invalid UTF-8: {}", e));
            return -2;
        }
    };

    // Validate JSON
    let mut bytes = text.as_bytes().to_vec();
    match simd_json::to_owned_value(&mut bytes) {
        Ok(_) => {
            write_error(error_buf, error_buf_len, "");
            0
        }
        Err(e) => {
            write_error(error_buf, error_buf_len, &format!("JSON error: {}", e));
            1
        }
    }
}

/// Helper function to write error message to buffer
///
/// # Safety
/// - `buf` must point to valid memory of size `buf_len` bytes, or be null
/// - Memory must remain valid during the entire call
unsafe fn write_error(buf: *mut u8, buf_len: usize, msg: &str) {
    if buf.is_null() || buf_len == 0 {
        return;
    }

    let bytes = msg.as_bytes();
    let copy_len = bytes.len().min(buf_len - 1);

    if copy_len > 0 {
        let slice = slice::from_raw_parts_mut(buf, buf_len);
        slice[..copy_len].copy_from_slice(&bytes[..copy_len]);
        slice[copy_len] = 0; // Null terminator
    } else {
        *buf = 0; // Empty string
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_valid_json() {
        let json = br#"{"key":"value"}"#;
        unsafe {
            assert!(json_validate(json.as_ptr(), json.len()));
        }
    }

    #[test]
    fn test_invalid_json() {
        let json = br#"{"key":"value""#; // missing }
        unsafe {
            assert!(!json_validate(json.as_ptr(), json.len()));
        }
    }

    #[test]
    fn test_null_pointer() {
        unsafe {
            assert!(!json_validate(std::ptr::null(), 0));
        }
    }

    #[test]
    fn test_empty_string() {
        let json = b"";
        unsafe {
            assert!(!json_validate(json.as_ptr(), json.len()));
        }
    }

    #[test]
    fn test_unicode() {
        let json = r#"{"name":"Hello World 👋"}"#;
        let bytes = json.as_bytes();
        unsafe {
            assert!(json_validate(bytes.as_ptr(), bytes.len()));
        }
    }

    #[test]
    fn test_array() {
        let json = br#"[1,2,3,4,5]"#;
        unsafe {
            assert!(json_validate(json.as_ptr(), json.len()));
        }
    }

    #[test]
    fn test_detailed_validation_success() {
        let json = br#"{"valid":true}"#;
        let mut error_buf = [0u8; 256];
        unsafe {
            let code = json_validate_detailed(
                json.as_ptr(),
                json.len(),
                error_buf.as_mut_ptr(),
                error_buf.len(),
            );
            assert_eq!(code, 0);
        }
    }

    #[test]
    fn test_detailed_validation_error() {
        let json = br#"{"invalid"}"#;
        let mut error_buf = [0u8; 256];
        unsafe {
            let code = json_validate_detailed(
                json.as_ptr(),
                json.len(),
                error_buf.as_mut_ptr(),
                error_buf.len(),
            );
            assert_eq!(code, 1);
        }
    }
}
