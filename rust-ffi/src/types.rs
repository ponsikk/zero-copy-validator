/// Common types and constants for the JSON validator
use std::env;

/// Maximum JSON size - configurable via MAX_JSON_SIZE env var
/// Default: 500MB
pub fn max_json_size() -> usize {
    env::var("MAX_JSON_SIZE")
        .ok()
        .and_then(|v| v.parse().ok())
        .unwrap_or(500 * 1024 * 1024) // 500MB default
}

/// Maximum JSON nesting depth - configurable via MAX_JSON_DEPTH env var
/// Default: 128 levels
#[allow(dead_code)]
pub fn max_json_depth() -> usize {
    env::var("MAX_JSON_DEPTH")
        .ok()
        .and_then(|v| v.parse().ok())
        .unwrap_or(128)
}

/// Maximum error message buffer size
#[allow(dead_code)]
pub const MAX_ERROR_BUFFER: usize = 1024;

/// Error codes returned by validation functions
#[repr(i32)]
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum ErrorCode {
    /// Success - JSON is valid
    Ok = 0,
    /// Null pointer provided
    NullPointer = -1,
    /// Invalid UTF-8 encoding
    InvalidUtf8 = -2,
    /// JSON is too large or empty
    TooLarge = -3,
    /// JSON syntax error
    SyntaxError = 1,
    /// JSONPath not found
    PathNotFound = 2,
    /// Type mismatch (e.g., expected string, got number)
    TypeMismatch = 3,
}

/// JSON value types
#[repr(i32)]
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum JsonType {
    Null = 0,
    Boolean = 1,
    Number = 2,
    String = 3,
    Array = 4,
    Object = 5,
}

impl ErrorCode {
    /// Convert error code to i32
    pub fn as_i32(self) -> i32 {
        self as i32
    }
}
