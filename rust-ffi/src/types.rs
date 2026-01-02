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
    /// Format invalid (e.g., invalid email, URL, date, UUID)
    FormatInvalid = 4,
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

/// Detailed error information with line/column location
///
/// This struct is returned by validation functions to provide
/// precise error location information for better developer experience.
///
/// # C Representation
/// This struct is `#[repr(C)]` to ensure stable memory layout across FFI boundary.
#[repr(C)]
#[derive(Debug, Clone, Copy)]
pub struct DetailedError {
    /// Error code (0 = success, negative = system error, positive = validation error)
    pub code: i32,

    /// Line number where error occurred (1-indexed, 0 if unknown)
    pub line: u32,

    /// Column number where error occurred (1-indexed, 0 if unknown)
    pub column: u32,

    /// Offset in bytes from start of JSON (0 if unknown)
    pub byte_offset: usize,
}

impl DetailedError {
    /// Create success result (no error)
    pub const fn ok() -> Self {
        Self {
            code: 0,
            line: 0,
            column: 0,
            byte_offset: 0,
        }
    }

    /// Create error result with location info
    pub const fn error(code: ErrorCode, line: u32, column: u32, offset: usize) -> Self {
        Self {
            code: code as i32,
            line,
            column,
            byte_offset: offset,
        }
    }

    /// Create error result without location info
    pub const fn error_simple(code: ErrorCode) -> Self {
        Self {
            code: code as i32,
            line: 0,
            column: 0,
            byte_offset: 0,
        }
    }
}
