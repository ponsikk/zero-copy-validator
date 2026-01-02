package io.zerocopy.json;

import io.zerocopy.json.internal.NativeLib;

import java.nio.charset.StandardCharsets;

/**
 * High-performance JSON validator using Rust + SIMD.
 *
 * <p>This validator is 10-20x faster than Jackson/Gson for validation tasks
 * due to zero-copy architecture and SIMD optimizations in the Rust backend.
 *
 * <h2>Usage Examples:</h2>
 * <pre>{@code
 * // Simple validation
 * boolean isValid = JsonValidator.validate("{\"key\":\"value\"}");
 *
 * // Detailed validation with error messages
 * ValidationResult result = JsonValidator.validateDetailed("{\"invalid json");
 * if (!result.isValid()) {
 *     System.out.println("Error: " + result.getErrorMessage());
 * }
 *
 * // Validate byte array (zero-copy)
 * byte[] jsonBytes = loadFromFile();
 * boolean isValid = JsonValidator.validate(jsonBytes);
 * }</pre>
 *
 * <h2>Thread Safety:</h2>
 * <p>All methods are thread-safe and can be called concurrently.
 *
 * @since 0.1.0
 */
public final class JsonValidator {

    private JsonValidator() {
        // Prevent instantiation
    }

    /**
     * Validates JSON string.
     *
     * <p>This is the simplest method for quick validation checks.
     * If you need error details, use {@link #validateDetailed(String)} instead.
     *
     * @param json the JSON string to validate
     * @return {@code true} if JSON is valid, {@code false} otherwise
     * @throws NullPointerException if json is null
     * @see #validateDetailed(String)
     */
    public static boolean validate(String json) {
        if (json == null) {
            throw new NullPointerException("JSON string cannot be null");
        }

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.validateJson(bytes);
    }

    /**
     * Validates JSON byte array (zero-copy).
     *
     * <p>This method is more efficient than {@link #validate(String)} as it
     * avoids String → byte[] conversion. Use this when working with raw bytes
     * from files, network, etc.
     *
     * @param jsonBytes the JSON data as UTF-8 encoded bytes
     * @return {@code true} if JSON is valid, {@code false} otherwise
     * @throws NullPointerException if jsonBytes is null
     */
    public static boolean validate(byte[] jsonBytes) {
        if (jsonBytes == null) {
            throw new NullPointerException("JSON bytes cannot be null");
        }

        return NativeLib.validateJson(jsonBytes);
    }

    /**
     * Validates JSON and returns detailed result with error message.
     *
     * <p>Use this method when you need to know why validation failed.
     *
     * @param json the JSON string to validate
     * @return {@link ValidationResult} containing validation status and error details
     * @throws NullPointerException if json is null
     * @see ValidationResult
     */
    public static ValidationResult validateDetailed(String json) {
        if (json == null) {
            throw new NullPointerException("JSON string cannot be null");
        }

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.validateJsonDetailed(bytes);
    }

    /**
     * Validates JSON byte array and returns detailed result (zero-copy).
     *
     * @param jsonBytes the JSON data as UTF-8 encoded bytes
     * @return {@link ValidationResult} containing validation status and error details
     * @throws NullPointerException if jsonBytes is null
     */
    public static ValidationResult validateDetailed(byte[] jsonBytes) {
        if (jsonBytes == null) {
            throw new NullPointerException("JSON bytes cannot be null");
        }

        return NativeLib.validateJsonDetailed(jsonBytes);
    }

    /**
     * Extracts string value from JSON without full parsing (zero-copy).
     *
     * <p>This method is significantly faster than parsing entire JSON
     * when you only need specific fields.
     *
     * <h2>Supported paths:</h2>
     * <ul>
     *   <li>"name" - simple field</li>
     *   <li>"user.email" - nested field</li>
     *   <li>"items.0.id" - array element (by index)</li>
     *   <li>"$.user.name" - JSONPath style</li>
     * </ul>
     *
     * @param json the JSON string
     * @param path the path to the field
     * @return extracted string value
     * @throws NullPointerException if json or path is null
     * @throws RuntimeException if extraction fails (path not found, type mismatch, etc.)
     */
    public static String getString(String json, String path) {
        if (json == null) {
            throw new NullPointerException("JSON string cannot be null");
        }
        if (path == null) {
            throw new NullPointerException("Path cannot be null");
        }

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.getString(bytes, path);
    }

    /**
     * Extracts string value from JSON byte array (zero-copy).
     *
     * @param jsonBytes the JSON data as UTF-8 encoded bytes
     * @param path the path to the field
     * @return extracted string value
     * @throws NullPointerException if jsonBytes or path is null
     * @throws RuntimeException if extraction fails
     */
    public static String getString(byte[] jsonBytes, String path) {
        if (jsonBytes == null) {
            throw new NullPointerException("JSON bytes cannot be null");
        }
        if (path == null) {
            throw new NullPointerException("Path cannot be null");
        }

        return NativeLib.getString(jsonBytes, path);
    }

    /**
     * Extracts numeric value from JSON without full parsing.
     *
     * @param json the JSON string
     * @param path the path to the field
     * @return extracted number as double
     * @throws NullPointerException if json or path is null
     * @throws RuntimeException if extraction fails
     */
    public static double getNumber(String json, String path) {
        if (json == null) {
            throw new NullPointerException("JSON string cannot be null");
        }
        if (path == null) {
            throw new NullPointerException("Path cannot be null");
        }

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.getNumber(bytes, path);
    }

    /**
     * Extracts numeric value from JSON byte array.
     *
     * @param jsonBytes the JSON data as UTF-8 encoded bytes
     * @param path the path to the field
     * @return extracted number as double
     * @throws NullPointerException if jsonBytes or path is null
     * @throws RuntimeException if extraction fails
     */
    public static double getNumber(byte[] jsonBytes, String path) {
        if (jsonBytes == null) {
            throw new NullPointerException("JSON bytes cannot be null");
        }
        if (path == null) {
            throw new NullPointerException("Path cannot be null");
        }

        return NativeLib.getNumber(jsonBytes, path);
    }

    /**
     * Extracts boolean value from JSON without full parsing.
     *
     * @param json the JSON string
     * @param path the path to the field
     * @return extracted boolean value
     * @throws NullPointerException if json or path is null
     * @throws RuntimeException if extraction fails
     */
    public static boolean getBoolean(String json, String path) {
        if (json == null) {
            throw new NullPointerException("JSON string cannot be null");
        }
        if (path == null) {
            throw new NullPointerException("Path cannot be null");
        }

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.getBoolean(bytes, path);
    }

    /**
     * Extracts boolean value from JSON byte array.
     *
     * @param jsonBytes the JSON data as UTF-8 encoded bytes
     * @param path the path to the field
     * @return extracted boolean value
     * @throws NullPointerException if jsonBytes or path is null
     * @throws RuntimeException if extraction fails
     */
    public static boolean getBoolean(byte[] jsonBytes, String path) {
        if (jsonBytes == null) {
            throw new NullPointerException("JSON bytes cannot be null");
        }
        if (path == null) {
            throw new NullPointerException("Path cannot be null");
        }

        return NativeLib.getBoolean(jsonBytes, path);
    }

    // ============================================
    // Phase 2: Advanced Validators
    // ============================================

    /**
     * Validates that a JSON field has the expected type.
     *
     * @param json the JSON string
     * @param path the path to the field
     * @param expectedType the expected JSON type
     * @return true if field exists and has expected type
     * @throws NullPointerException if any argument is null
     */
    public static boolean validateFieldType(String json, String path, JsonType expectedType) {
        if (json == null || path == null || expectedType == null) {
            throw new NullPointerException();
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.validateFieldType(bytes, path, expectedType.getCode());
    }

    /**
     * Validates field type (zero-copy byte array version).
     */
    public static boolean validateFieldType(byte[] jsonBytes, String path, JsonType expectedType) {
        if (jsonBytes == null || path == null || expectedType == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateFieldType(jsonBytes, path, expectedType.getCode());
    }

    /**
     * Checks if a JSON field exists.
     *
     * @param json the JSON string
     * @param path the path to the field
     * @return true if field exists
     * @throws NullPointerException if any argument is null
     */
    public static boolean fieldExists(String json, String path) {
        if (json == null || path == null) {
            throw new NullPointerException();
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.fieldExists(bytes, path);
    }

    /**
     * Checks if field exists (zero-copy byte array version).
     */
    public static boolean fieldExists(byte[] jsonBytes, String path) {
        if (jsonBytes == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.fieldExists(jsonBytes, path);
    }

    /**
     * Checks if a JSON field is null.
     *
     * @param json the JSON string
     * @param path the path to the field
     * @return true if field exists AND is null
     * @throws NullPointerException if any argument is null
     */
    public static boolean fieldIsNull(String json, String path) {
        if (json == null || path == null) {
            throw new NullPointerException();
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.fieldIsNull(bytes, path);
    }

    /**
     * Checks if field is null (zero-copy byte array version).
     */
    public static boolean fieldIsNull(byte[] jsonBytes, String path) {
        if (jsonBytes == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.fieldIsNull(jsonBytes, path);
    }

    // ============================================
    // Phase 2.2: Range Validators
    // ============================================

    /**
     * Validates that a numeric field is within the specified range.
     *
     * @param json the JSON string
     * @param path the path to the field
     * @param min minimum value (inclusive)
     * @param max maximum value (inclusive)
     * @return true if field is within range, false otherwise
     * @throws NullPointerException if any argument is null
     */
    public static boolean validateNumberRange(String json, String path, double min, double max) {
        if (json == null || path == null) {
            throw new NullPointerException();
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.validateNumberRange(bytes, path, min, max) == 0;
    }

    /**
     * Validates number range (zero-copy byte array version).
     */
    public static boolean validateNumberRange(byte[] jsonBytes, String path, double min, double max) {
        if (jsonBytes == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateNumberRange(jsonBytes, path, min, max) == 0;
    }

    /**
     * Validates that a string field length is within the specified range.
     *
     * @param json the JSON string
     * @param path the path to the field
     * @param minLength minimum string length (inclusive)
     * @param maxLength maximum string length (inclusive)
     * @return true if string length is within range, false otherwise
     * @throws NullPointerException if any argument is null
     */
    public static boolean validateStringLength(String json, String path, int minLength, int maxLength) {
        if (json == null || path == null) {
            throw new NullPointerException();
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.validateStringLength(bytes, path, minLength, maxLength) == 0;
    }

    /**
     * Validates string length (zero-copy byte array version).
     */
    public static boolean validateStringLength(byte[] jsonBytes, String path, int minLength, int maxLength) {
        if (jsonBytes == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateStringLength(jsonBytes, path, minLength, maxLength) == 0;
    }

    /**
     * Validates that an array size is within the specified range.
     *
     * @param json the JSON string
     * @param path the path to the field
     * @param minItems minimum number of items (inclusive)
     * @param maxItems maximum number of items (inclusive)
     * @return true if array size is within range, false otherwise
     * @throws NullPointerException if any argument is null
     */
    public static boolean validateArraySize(String json, String path, int minItems, int maxItems) {
        if (json == null || path == null) {
            throw new NullPointerException();
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.validateArraySize(bytes, path, minItems, maxItems) == 0;
    }

    /**
     * Validates array size (zero-copy byte array version).
     */
    public static boolean validateArraySize(byte[] jsonBytes, String path, int minItems, int maxItems) {
        if (jsonBytes == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateArraySize(jsonBytes, path, minItems, maxItems) == 0;
    }

    // ============================================
    // Phase 2.3: Format Validators
    // ============================================

    /**
     * Validates that a field contains a valid email address (RFC 5322 simplified).
     *
     * @param json JSON string
     * @param path JSONPath to the field
     * @return true if field contains a valid email, false otherwise
     * @throws NullPointerException if any argument is null
     */
    public static boolean validateEmail(String json, String path) {
        if (json == null || path == null) {
            throw new NullPointerException();
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.validateEmail(bytes, path) == 0;
    }

    /**
     * Validates email format (zero-copy byte array version).
     */
    public static boolean validateEmail(byte[] jsonBytes, String path) {
        if (jsonBytes == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateEmail(jsonBytes, path) == 0;
    }

    /**
     * Validates that a field contains a valid URL (HTTP/HTTPS only).
     *
     * @param json JSON string
     * @param path JSONPath to the field
     * @return true if field contains a valid URL, false otherwise
     * @throws NullPointerException if any argument is null
     */
    public static boolean validateUrl(String json, String path) {
        if (json == null || path == null) {
            throw new NullPointerException();
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.validateUrl(bytes, path) == 0;
    }

    /**
     * Validates URL format (zero-copy byte array version).
     */
    public static boolean validateUrl(byte[] jsonBytes, String path) {
        if (jsonBytes == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateUrl(jsonBytes, path) == 0;
    }

    /**
     * Validates that a field contains a valid ISO 8601 date/datetime.
     * Supports both full datetime (e.g., "2024-12-30T10:30:00Z") and simple date (e.g., "2024-12-30").
     *
     * @param json JSON string
     * @param path JSONPath to the field
     * @return true if field contains a valid ISO 8601 date, false otherwise
     * @throws NullPointerException if any argument is null
     */
    public static boolean validateIsoDate(String json, String path) {
        if (json == null || path == null) {
            throw new NullPointerException();
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.validateIsoDate(bytes, path) == 0;
    }

    /**
     * Validates ISO 8601 date format (zero-copy byte array version).
     */
    public static boolean validateIsoDate(byte[] jsonBytes, String path) {
        if (jsonBytes == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateIsoDate(jsonBytes, path) == 0;
    }

    /**
     * Validates that a field contains a valid UUID (RFC 4122).
     * Supports all UUID formats (with or without hyphens).
     *
     * @param json JSON string
     * @param path JSONPath to the field
     * @return true if field contains a valid UUID, false otherwise
     * @throws NullPointerException if any argument is null
     */
    public static boolean validateUuid(String json, String path) {
        if (json == null || path == null) {
            throw new NullPointerException();
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.validateUuid(bytes, path) == 0;
    }

    /**
     * Validates UUID format (zero-copy byte array version).
     */
    public static boolean validateUuid(byte[] jsonBytes, String path) {
        if (jsonBytes == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateUuid(jsonBytes, path) == 0;
    }

    /**
     * Validates that a field contains a valid phone number (E.164 international format).
     * Supports formats like: +1234567890, +1-234-567-8900, +1 (234) 567-8900, etc.
     *
     * @param json JSON string
     * @param path JSONPath to the field
     * @return true if field contains a valid phone number, false otherwise
     * @throws NullPointerException if any argument is null
     */
    public static boolean validatePhoneNumber(String json, String path) {
        if (json == null || path == null) {
            throw new NullPointerException();
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.validatePhoneNumber(bytes, path) == 0;
    }

    /**
     * Validates phone number format (zero-copy byte array version).
     */
    public static boolean validatePhoneNumber(byte[] jsonBytes, String path) {
        if (jsonBytes == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validatePhoneNumber(jsonBytes, path) == 0;
    }

    /**
     * Validates that a field contains a valid IP address (IPv4 or IPv6).
     * Examples: "192.168.1.1", "2001:0db8:85a3::8a2e:0370:7334"
     *
     * @param json JSON string
     * @param path JSONPath to the field
     * @return true if field contains a valid IP address, false otherwise
     * @throws NullPointerException if any argument is null
     */
    public static boolean validateIpAddress(String json, String path) {
        if (json == null || path == null) {
            throw new NullPointerException();
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.validateIpAddress(bytes, path) == 0;
    }

    /**
     * Validates IP address format (zero-copy byte array version).
     */
    public static boolean validateIpAddress(byte[] jsonBytes, String path) {
        if (jsonBytes == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateIpAddress(jsonBytes, path) == 0;
    }

    // ============================================
    // ZERO-COPY API (DirectByteBuffer)
    // ============================================

    /**
     * Validates JSON using TRUE zero-copy approach with DirectByteBuffer.
     *
     * <p><b>Performance:</b> This is the fastest validation method as it eliminates
     * heap → off-heap memory copy. Use this when you already have JSON data in a
     * DirectByteBuffer (e.g., from network I/O, memory-mapped files, Netty channels).
     *
     * <p><b>When to use:</b>
     * <ul>
     *   <li>High-frequency validation (millions of requests/sec)</li>
     *   <li>Large JSON payloads (> 1MB)</li>
     *   <li>Already using DirectByteBuffer in your pipeline (Netty, gRPC, etc.)</li>
     *   <li>Memory-mapped file processing</li>
     * </ul>
     *
     * <p><b>For simple use cases</b>, use {@link #validate(String)} instead.
     *
     * <h2>Example:</h2>
     * <pre>{@code
     * // Allocate off-heap buffer
     * ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
     *
     * // Read JSON directly into buffer (zero-copy from network/file)
     * channel.read(buffer);
     * buffer.flip();
     *
     * // Validate without any copying
     * boolean valid = JsonValidator.validateZeroCopy(buffer);
     * }</pre>
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON (must be direct!)
     * @return {@code true} if JSON is valid, {@code false} otherwise
     * @throws NullPointerException if buffer is null
     * @throws IllegalArgumentException if buffer is not direct (use ByteBuffer.allocateDirect())
     * @see #validate(byte[])
     * @see #validateDetailedZeroCopy(java.nio.ByteBuffer)
     * @since 0.1.0
     */
    public static boolean validateZeroCopy(java.nio.ByteBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException("Buffer cannot be null");
        }
        return NativeLib.validateJsonZeroCopy(buffer);
    }

    /**
     * Validates JSON with detailed error information using zero-copy approach.
     *
     * <p>Combines TRUE zero-copy validation with precise error location (line, column, byte offset).
     * Perfect for high-performance scenarios where you need both speed and detailed error reporting.
     *
     * <p><b>Performance benefit:</b> No heap → off-heap copy + precise error location.
     *
     * <h2>Example:</h2>
     * <pre>{@code
     * ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
     * // ... load JSON into buffer ...
     *
     * ValidationResult result = JsonValidator.validateDetailedZeroCopy(buffer);
     * if (!result.isValid()) {
     *     System.err.printf("Error at line %d, column %d: %s%n",
     *         result.getLine(), result.getColumn(), result.getErrorMessage());
     * }
     * }</pre>
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON
     * @return {@link ValidationResult} with error details and location
     * @throws NullPointerException if buffer is null
     * @throws IllegalArgumentException if buffer is not direct
     * @see #validateDetailed(byte[])
     * @see #validateZeroCopy(java.nio.ByteBuffer)
     * @since 0.1.0
     */
    public static ValidationResult validateDetailedZeroCopy(java.nio.ByteBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException("Buffer cannot be null");
        }
        return NativeLib.validateJsonDetailedZeroCopy(buffer);
    }

    /**
     * Extracts string value from JSON using zero-copy approach with DirectByteBuffer.
     *
     * <p><b>Performance:</b> Fastest string extraction method. Combines:
     * <ul>
     *   <li>Zero-copy JSON input (no heap → off-heap copy)</li>
     *   <li>Dynamic buffer allocation (no size limits)</li>
     *   <li>Partial parsing (no full JSON tree construction)</li>
     * </ul>
     *
     * <h2>Example:</h2>
     * <pre>{@code
     * ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
     * // ... load JSON into buffer ...
     *
     * String email = JsonValidator.getStringZeroCopy(buffer, "user.email");
     * String name = JsonValidator.getStringZeroCopy(buffer, "user.name");
     * }</pre>
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON
     * @param path JSONPath query (e.g., "user.name", "items.0.id")
     * @return extracted string value
     * @throws NullPointerException if buffer or path is null
     * @throws IllegalArgumentException if buffer is not direct
     * @throws RuntimeException if extraction fails (path not found, type mismatch, etc.)
     * @see #getString(byte[], String)
     * @since 0.1.0
     */
    public static String getStringZeroCopy(java.nio.ByteBuffer buffer, String path) {
        if (buffer == null) {
            throw new NullPointerException("Buffer cannot be null");
        }
        if (path == null) {
            throw new NullPointerException("Path cannot be null");
        }
        return NativeLib.getStringZeroCopy(buffer, path);
    }

    /**
     * Extracts numeric value from JSON using zero-copy approach with DirectByteBuffer.
     *
     * <p><b>Performance:</b> No heap → off-heap copy, partial parsing only.
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON
     * @param path JSONPath query
     * @return extracted number as double
     * @throws NullPointerException if buffer or path is null
     * @throws IllegalArgumentException if buffer is not direct
     * @throws RuntimeException if extraction fails
     * @see #getNumber(byte[], String)
     * @since 0.1.0
     */
    public static double getNumberZeroCopy(java.nio.ByteBuffer buffer, String path) {
        if (buffer == null) {
            throw new NullPointerException("Buffer cannot be null");
        }
        if (path == null) {
            throw new NullPointerException("Path cannot be null");
        }
        return NativeLib.getNumberZeroCopy(buffer, path);
    }

    /**
     * Extracts boolean value from JSON using zero-copy approach with DirectByteBuffer.
     *
     * <p><b>Performance:</b> No heap → off-heap copy, partial parsing only.
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON
     * @param path JSONPath query
     * @return extracted boolean value
     * @throws NullPointerException if buffer or path is null
     * @throws IllegalArgumentException if buffer is not direct
     * @throws RuntimeException if extraction fails
     * @see #getBoolean(byte[], String)
     * @since 0.1.0
     */
    public static boolean getBooleanZeroCopy(java.nio.ByteBuffer buffer, String path) {
        if (buffer == null) {
            throw new NullPointerException("Buffer cannot be null");
        }
        if (path == null) {
            throw new NullPointerException("Path cannot be null");
        }
        return NativeLib.getBooleanZeroCopy(buffer, path);
    }

    // ============================================
    // ZERO-COPY VALIDATORS (DirectByteBuffer)
    // ============================================

    /**
     * Validates number range using TRUE zero-copy approach with DirectByteBuffer.
     *
     * <p><b>Performance:</b> No heap → off-heap copy + partial parsing (only extracts target field).
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON
     * @param path JSONPath query
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return true if valid, false otherwise
     * @throws NullPointerException if buffer or path is null
     * @throws IllegalArgumentException if buffer is not direct
     * @since 0.1.0
     */
    public static boolean validateNumberRangeZeroCopy(java.nio.ByteBuffer buffer, String path, double min, double max) {
        if (buffer == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateNumberRangeZeroCopy(buffer, path, min, max) == 0;
    }

    /**
     * Validates string length using TRUE zero-copy approach with DirectByteBuffer.
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON
     * @param path JSONPath query
     * @param minLength Minimum string length (inclusive)
     * @param maxLength Maximum string length (inclusive)
     * @return true if valid, false otherwise
     * @throws NullPointerException if buffer or path is null
     * @throws IllegalArgumentException if buffer is not direct
     * @since 0.1.0
     */
    public static boolean validateStringLengthZeroCopy(java.nio.ByteBuffer buffer, String path, int minLength, int maxLength) {
        if (buffer == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateStringLengthZeroCopy(buffer, path, minLength, maxLength) == 0;
    }

    /**
     * Validates array size using TRUE zero-copy approach with DirectByteBuffer.
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON
     * @param path JSONPath query
     * @param minItems Minimum number of items (inclusive)
     * @param maxItems Maximum number of items (inclusive)
     * @return true if valid, false otherwise
     * @throws NullPointerException if buffer or path is null
     * @throws IllegalArgumentException if buffer is not direct
     * @since 0.1.0
     */
    public static boolean validateArraySizeZeroCopy(java.nio.ByteBuffer buffer, String path, int minItems, int maxItems) {
        if (buffer == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateArraySizeZeroCopy(buffer, path, minItems, maxItems) == 0;
    }

    /**
     * Validates email format using TRUE zero-copy approach with DirectByteBuffer.
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON
     * @param path JSONPath query
     * @return true if valid email, false otherwise
     * @throws NullPointerException if buffer or path is null
     * @throws IllegalArgumentException if buffer is not direct
     * @since 0.1.0
     */
    public static boolean validateEmailZeroCopy(java.nio.ByteBuffer buffer, String path) {
        if (buffer == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateEmailZeroCopy(buffer, path) == 0;
    }

    /**
     * Validates URL format using TRUE zero-copy approach with DirectByteBuffer.
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON
     * @param path JSONPath query
     * @return true if valid URL, false otherwise
     * @throws NullPointerException if buffer or path is null
     * @throws IllegalArgumentException if buffer is not direct
     * @since 0.1.0
     */
    public static boolean validateUrlZeroCopy(java.nio.ByteBuffer buffer, String path) {
        if (buffer == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateUrlZeroCopy(buffer, path) == 0;
    }

    /**
     * Validates ISO 8601 date format using TRUE zero-copy approach with DirectByteBuffer.
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON
     * @param path JSONPath query
     * @return true if valid ISO date, false otherwise
     * @throws NullPointerException if buffer or path is null
     * @throws IllegalArgumentException if buffer is not direct
     * @since 0.1.0
     */
    public static boolean validateIsoDateZeroCopy(java.nio.ByteBuffer buffer, String path) {
        if (buffer == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateIsoDateZeroCopy(buffer, path) == 0;
    }

    /**
     * Validates UUID format using TRUE zero-copy approach with DirectByteBuffer.
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON
     * @param path JSONPath query
     * @return true if valid UUID, false otherwise
     * @throws NullPointerException if buffer or path is null
     * @throws IllegalArgumentException if buffer is not direct
     * @since 0.1.0
     */
    public static boolean validateUuidZeroCopy(java.nio.ByteBuffer buffer, String path) {
        if (buffer == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateUuidZeroCopy(buffer, path) == 0;
    }

    /**
     * Validates phone number format using TRUE zero-copy approach with DirectByteBuffer.
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON
     * @param path JSONPath query
     * @return true if valid phone number, false otherwise
     * @throws NullPointerException if buffer or path is null
     * @throws IllegalArgumentException if buffer is not direct
     * @since 0.1.0
     */
    public static boolean validatePhoneNumberZeroCopy(java.nio.ByteBuffer buffer, String path) {
        if (buffer == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validatePhoneNumberZeroCopy(buffer, path) == 0;
    }

    /**
     * Validates IP address format using TRUE zero-copy approach with DirectByteBuffer.
     *
     * @param buffer Direct ByteBuffer containing UTF-8 encoded JSON
     * @param path JSONPath query
     * @return true if valid IP address, false otherwise
     * @throws NullPointerException if buffer or path is null
     * @throws IllegalArgumentException if buffer is not direct
     * @since 0.1.0
     */
    public static boolean validateIpAddressZeroCopy(java.nio.ByteBuffer buffer, String path) {
        if (buffer == null || path == null) {
            throw new NullPointerException();
        }
        return NativeLib.validateIpAddressZeroCopy(buffer, path) == 0;
    }

    /**
     * Gets the version of the validator library.
     *
     * @return version string (e.g., "0.1.0")
     */
    public static String getVersion() {
        return "0.1.0-SNAPSHOT";
    }
}
