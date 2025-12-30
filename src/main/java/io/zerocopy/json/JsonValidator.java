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

    /**
     * Gets the version of the validator library.
     *
     * @return version string (e.g., "0.1.0")
     */
    public static String getVersion() {
        return "0.1.0-SNAPSHOT";
    }
}
