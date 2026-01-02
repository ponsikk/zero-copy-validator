/// Format validation for JSON fields
///
/// This module provides validators for common formats like email, URL, dates, UUIDs,
/// phone numbers, and IP addresses without parsing the entire JSON document (zero-copy).
use crate::json_get::extract_field_hybrid; // OPTIMIZATION: Shared hybrid extraction
use crate::types::{max_json_size, ErrorCode};
use chrono::DateTime;
use ffi_safety_macro::ffi_safe_with_error;
use regex::Regex;
use std::net::IpAddr;
use std::slice;
use std::str;
use std::sync::OnceLock;

// ============================================
// Regex Patterns (compiled once, cached)
// ============================================

/// Email regex pattern (RFC 5322 simplified)
fn email_regex() -> &'static Regex {
    static EMAIL_REGEX: OnceLock<Regex> = OnceLock::new();
    EMAIL_REGEX.get_or_init(|| {
        Regex::new(r"^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$")
            .expect("Failed to compile email regex")
    })
}

/// URL regex pattern (HTTP/HTTPS only)
fn url_regex() -> &'static Regex {
    static URL_REGEX: OnceLock<Regex> = OnceLock::new();
    URL_REGEX.get_or_init(|| {
        Regex::new(r"^https?://[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*(?::\d+)?(?:/[^\s]*)?$")
            .expect("Failed to compile URL regex")
    })
}

/// Phone number regex pattern (E.164 international format)
/// Supports: +1234567890, +1-234-567-8900, +1 (234) 567-8900, etc.
/// Requires leading "+" sign for international format
fn phone_regex() -> &'static Regex {
    static PHONE_REGEX: OnceLock<Regex> = OnceLock::new();
    PHONE_REGEX.get_or_init(|| {
        Regex::new(
            r"^\+[1-9]\d{0,3}[-.\s]?(\(?\d{1,4}\)?)?[-.\s]?\d{1,4}[-.\s]?\d{1,4}[-.\s]?\d{1,9}$",
        )
        .expect("Failed to compile phone regex")
    })
}

// ============================================
// Email Validator
// ============================================

/// Validates that a JSON field contains a valid email address
///
/// # Safety
/// - `json_ptr` must point to valid UTF-8 encoded JSON data of size `json_len`
/// - `path_ptr` must point to valid UTF-8 encoded path string of size `path_len`
/// - All pointers must remain valid during the call
///
/// # Returns
/// - `0` (ErrorCode::Ok) - Field exists and is a valid email
/// - `2` (ErrorCode::PathNotFound) - Field doesn't exist
/// - `3` (ErrorCode::TypeMismatch) - Field exists but is not a string
/// - `4` (ErrorCode::FormatInvalid) - Field is a string but not a valid email
#[ffi_safe_with_error(-1)]
#[no_mangle]
pub unsafe extern "C" fn json_validate_email(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
) -> i32 {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() {
        return ErrorCode::NullPointer as i32;
    }

    let max_size = max_json_size();
    if json_len == 0 || json_len > max_size {
        return ErrorCode::TooLarge as i32;
    }

    // Convert to slices
    let json_slice = slice::from_raw_parts(json_ptr, json_len);
    let path_slice = slice::from_raw_parts(path_ptr, path_len);

    // Validate UTF-8
    let json_str = match str::from_utf8(json_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8 as i32,
    };

    let path_str = match str::from_utf8(path_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8 as i32,
    };

    // OPTIMIZATION: Use shared extract_field_hybrid instead of parsing whole JSON!
    // This avoids: 1) copying whole JSON, 2) parsing whole JSON tree
    // Only extracts the specific field we need using hybrid strategy
    let extracted = match extract_field_hybrid(json_str, path_str) {
        Ok(v) => v,
        Err(e) => return e as i32,
    };

    // Check if it's a string
    let email_str = match extracted.as_str() {
        Some(s) => s,
        None => return ErrorCode::TypeMismatch as i32,
    };

    // Validate email format
    if email_regex().is_match(email_str) {
        ErrorCode::Ok as i32
    } else {
        ErrorCode::FormatInvalid as i32
    }
}

// ============================================
// URL Validator
// ============================================

/// Validates that a JSON field contains a valid URL (HTTP/HTTPS only)
///
/// # Safety
/// Same safety requirements as json_validate_email
///
/// # Returns
/// - `0` (ErrorCode::Ok) - Field exists and is a valid URL
/// - `2` (ErrorCode::PathNotFound) - Field doesn't exist
/// - `3` (ErrorCode::TypeMismatch) - Field exists but is not a string
/// - `4` (ErrorCode::FormatInvalid) - Field is a string but not a valid URL
#[ffi_safe_with_error(-1)]
#[no_mangle]
pub unsafe extern "C" fn json_validate_url(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
) -> i32 {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() {
        return ErrorCode::NullPointer as i32;
    }

    let max_size = max_json_size();
    if json_len == 0 || json_len > max_size {
        return ErrorCode::TooLarge as i32;
    }

    // Convert to slices
    let json_slice = slice::from_raw_parts(json_ptr, json_len);
    let path_slice = slice::from_raw_parts(path_ptr, path_len);

    // Validate UTF-8
    let json_str = match str::from_utf8(json_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8 as i32,
    };

    let path_str = match str::from_utf8(path_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8 as i32,
    };

    // OPTIMIZATION: Use shared extract_field_hybrid (avoid parsing whole JSON)
    let extracted = match extract_field_hybrid(json_str, path_str) {
        Ok(v) => v,
        Err(e) => return e as i32,
    };

    // Check if it's a string
    let url_str = match extracted.as_str() {
        Some(s) => s,
        None => return ErrorCode::TypeMismatch as i32,
    };

    // Validate URL format
    if url_regex().is_match(url_str) {
        ErrorCode::Ok as i32
    } else {
        ErrorCode::FormatInvalid as i32
    }
}

// ============================================
// ISO 8601 Date Validator
// ============================================

/// Validates that a JSON field contains a valid ISO 8601 date/datetime
///
/// # Safety
/// Same safety requirements as json_validate_email
///
/// # Returns
/// - `0` (ErrorCode::Ok) - Field exists and is a valid ISO 8601 date
/// - `2` (ErrorCode::PathNotFound) - Field doesn't exist
/// - `3` (ErrorCode::TypeMismatch) - Field exists but is not a string
/// - `4` (ErrorCode::FormatInvalid) - Field is a string but not a valid ISO 8601 date
#[ffi_safe_with_error(-1)]
#[no_mangle]
pub unsafe extern "C" fn json_validate_iso_date(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
) -> i32 {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() {
        return ErrorCode::NullPointer as i32;
    }

    let max_size = max_json_size();
    if json_len == 0 || json_len > max_size {
        return ErrorCode::TooLarge as i32;
    }

    // Convert to slices
    let json_slice = slice::from_raw_parts(json_ptr, json_len);
    let path_slice = slice::from_raw_parts(path_ptr, path_len);

    // Validate UTF-8
    let json_str = match str::from_utf8(json_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8 as i32,
    };

    let path_str = match str::from_utf8(path_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8 as i32,
    };

    // OPTIMIZATION: Use shared extract_field_hybrid (avoid parsing whole JSON)
    let extracted = match extract_field_hybrid(json_str, path_str) {
        Ok(v) => v,
        Err(e) => return e as i32,
    };

    // Check if it's a string
    let date_str = match extracted.as_str() {
        Some(s) => s,
        None => return ErrorCode::TypeMismatch as i32,
    };

    // Try parsing as RFC 3339 (ISO 8601)
    if DateTime::parse_from_rfc3339(date_str).is_ok() {
        return ErrorCode::Ok as i32;
    }

    // Try parsing as naive date (YYYY-MM-DD)
    if chrono::NaiveDate::parse_from_str(date_str, "%Y-%m-%d").is_ok() {
        return ErrorCode::Ok as i32;
    }

    ErrorCode::FormatInvalid as i32
}

// ============================================
// UUID Validator
// ============================================

/// Validates that a JSON field contains a valid UUID (RFC 4122)
///
/// # Safety
/// Same safety requirements as json_validate_email
///
/// # Returns
/// - `0` (ErrorCode::Ok) - Field exists and is a valid UUID
/// - `2` (ErrorCode::PathNotFound) - Field doesn't exist
/// - `3` (ErrorCode::TypeMismatch) - Field exists but is not a string
/// - `4` (ErrorCode::FormatInvalid) - Field is a string but not a valid UUID
#[ffi_safe_with_error(-1)]
#[no_mangle]
pub unsafe extern "C" fn json_validate_uuid(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
) -> i32 {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() {
        return ErrorCode::NullPointer as i32;
    }

    let max_size = max_json_size();
    if json_len == 0 || json_len > max_size {
        return ErrorCode::TooLarge as i32;
    }

    // Convert to slices
    let json_slice = slice::from_raw_parts(json_ptr, json_len);
    let path_slice = slice::from_raw_parts(path_ptr, path_len);

    // Validate UTF-8
    let json_str = match str::from_utf8(json_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8 as i32,
    };

    let path_str = match str::from_utf8(path_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8 as i32,
    };

    // OPTIMIZATION: Use shared extract_field_hybrid (avoid parsing whole JSON)
    let extracted = match extract_field_hybrid(json_str, path_str) {
        Ok(v) => v,
        Err(e) => return e as i32,
    };

    // Check if it's a string
    let uuid_str = match extracted.as_str() {
        Some(s) => s,
        None => return ErrorCode::TypeMismatch as i32,
    };

    // Validate UUID format
    if uuid::Uuid::parse_str(uuid_str).is_ok() {
        ErrorCode::Ok as i32
    } else {
        ErrorCode::FormatInvalid as i32
    }
}

// ============================================
// Phone Number Validator
// ============================================

/// Validates that a JSON field contains a valid phone number (E.164 international format)
///
/// # Safety
/// Same safety requirements as json_validate_email
///
/// # Returns
/// - `0` (ErrorCode::Ok) - Field exists and is a valid phone number
/// - `2` (ErrorCode::PathNotFound) - Field doesn't exist
/// - `3` (ErrorCode::TypeMismatch) - Field exists but is not a string
/// - `4` (ErrorCode::FormatInvalid) - Field is a string but not a valid phone number
#[ffi_safe_with_error(-1)]
#[no_mangle]
pub unsafe extern "C" fn json_validate_phone_number(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
) -> i32 {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() {
        return ErrorCode::NullPointer as i32;
    }

    let max_size = max_json_size();
    if json_len == 0 || json_len > max_size {
        return ErrorCode::TooLarge as i32;
    }

    // Convert to slices
    let json_slice = slice::from_raw_parts(json_ptr, json_len);
    let path_slice = slice::from_raw_parts(path_ptr, path_len);

    // Validate UTF-8
    let json_str = match str::from_utf8(json_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8 as i32,
    };

    let path_str = match str::from_utf8(path_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8 as i32,
    };

    // OPTIMIZATION: Use shared extract_field_hybrid (avoid parsing whole JSON)
    let extracted = match extract_field_hybrid(json_str, path_str) {
        Ok(v) => v,
        Err(e) => return e as i32,
    };

    // Check if it's a string
    let phone_str = match extracted.as_str() {
        Some(s) => s,
        None => return ErrorCode::TypeMismatch as i32,
    };

    // Validate phone number format
    if phone_regex().is_match(phone_str) {
        ErrorCode::Ok as i32
    } else {
        ErrorCode::FormatInvalid as i32
    }
}

// ============================================
// IP Address Validator
// ============================================

/// Validates that a JSON field contains a valid IP address (IPv4 or IPv6)
///
/// # Safety
/// Same safety requirements as json_validate_email
///
/// # Returns
/// - `0` (ErrorCode::Ok) - Field exists and is a valid IP address
/// - `2` (ErrorCode::PathNotFound) - Field doesn't exist
/// - `3` (ErrorCode::TypeMismatch) - Field exists but is not a string
/// - `4` (ErrorCode::FormatInvalid) - Field is a string but not a valid IP address
#[ffi_safe_with_error(-1)]
#[no_mangle]
pub unsafe extern "C" fn json_validate_ip_address(
    json_ptr: *const u8,
    json_len: usize,
    path_ptr: *const u8,
    path_len: usize,
) -> i32 {
    // Input validation
    if json_ptr.is_null() || path_ptr.is_null() {
        return ErrorCode::NullPointer as i32;
    }

    let max_size = max_json_size();
    if json_len == 0 || json_len > max_size {
        return ErrorCode::TooLarge as i32;
    }

    // Convert to slices
    let json_slice = slice::from_raw_parts(json_ptr, json_len);
    let path_slice = slice::from_raw_parts(path_ptr, path_len);

    // Validate UTF-8
    let json_str = match str::from_utf8(json_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8 as i32,
    };

    let path_str = match str::from_utf8(path_slice) {
        Ok(s) => s,
        Err(_) => return ErrorCode::InvalidUtf8 as i32,
    };

    // OPTIMIZATION: Use shared extract_field_hybrid (avoid parsing whole JSON)
    let extracted = match extract_field_hybrid(json_str, path_str) {
        Ok(v) => v,
        Err(e) => return e as i32,
    };

    // Check if it's a string
    let ip_str = match extracted.as_str() {
        Some(s) => s,
        None => return ErrorCode::TypeMismatch as i32,
    };

    // Validate IP address format (both IPv4 and IPv6)
    if ip_str.parse::<IpAddr>().is_ok() {
        ErrorCode::Ok as i32
    } else {
        ErrorCode::FormatInvalid as i32
    }
}

// ============================================
// Helper Functions
// ============================================
// ============================================
// Tests
// ============================================

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_validate_email_valid() {
        let json = r#"{"user":{"email":"test@example.com"}}"#;
        let path = "user.email";

        unsafe {
            let result = json_validate_email(json.as_ptr(), json.len(), path.as_ptr(), path.len());
            assert_eq!(result, ErrorCode::Ok as i32, "Valid email should pass");
        }
    }

    #[test]
    fn test_validate_email_invalid() {
        let json = r#"{"user":{"email":"not-an-email"}}"#;
        let path = "user.email";

        unsafe {
            let result = json_validate_email(json.as_ptr(), json.len(), path.as_ptr(), path.len());
            assert_eq!(
                result,
                ErrorCode::FormatInvalid as i32,
                "Invalid email should fail"
            );
        }
    }

    #[test]
    fn test_validate_url_valid() {
        let json = r#"{"website":"https://example.com/path"}"#;
        let path = "website";

        unsafe {
            let result = json_validate_url(json.as_ptr(), json.len(), path.as_ptr(), path.len());
            assert_eq!(result, ErrorCode::Ok as i32, "Valid URL should pass");
        }
    }

    #[test]
    fn test_validate_url_invalid() {
        let json = r#"{"website":"not a url"}"#;
        let path = "website";

        unsafe {
            let result = json_validate_url(json.as_ptr(), json.len(), path.as_ptr(), path.len());
            assert_eq!(
                result,
                ErrorCode::FormatInvalid as i32,
                "Invalid URL should fail"
            );
        }
    }

    #[test]
    fn test_validate_iso_date_valid() {
        let json = r#"{"createdAt":"2024-12-30T10:30:00Z"}"#;
        let path = "createdAt";

        unsafe {
            let result =
                json_validate_iso_date(json.as_ptr(), json.len(), path.as_ptr(), path.len());
            assert_eq!(result, ErrorCode::Ok as i32, "Valid ISO date should pass");
        }
    }

    #[test]
    fn test_validate_iso_date_simple() {
        let json = r#"{"date":"2024-12-30"}"#;
        let path = "date";

        unsafe {
            let result =
                json_validate_iso_date(json.as_ptr(), json.len(), path.as_ptr(), path.len());
            assert_eq!(
                result,
                ErrorCode::Ok as i32,
                "Valid simple date should pass"
            );
        }
    }

    #[test]
    fn test_validate_uuid_valid() {
        let json = r#"{"id":"550e8400-e29b-41d4-a716-446655440000"}"#;
        let path = "id";

        unsafe {
            let result = json_validate_uuid(json.as_ptr(), json.len(), path.as_ptr(), path.len());
            assert_eq!(result, ErrorCode::Ok as i32, "Valid UUID should pass");
        }
    }

    #[test]
    fn test_validate_uuid_invalid() {
        let json = r#"{"id":"not-a-uuid"}"#;
        let path = "id";

        unsafe {
            let result = json_validate_uuid(json.as_ptr(), json.len(), path.as_ptr(), path.len());
            assert_eq!(
                result,
                ErrorCode::FormatInvalid as i32,
                "Invalid UUID should fail"
            );
        }
    }
}
