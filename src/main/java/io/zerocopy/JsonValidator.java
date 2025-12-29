package io.zerocopy;

import io.zerocopy.internal.NativeLib;

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
 * boolean isValid = JsonValidator.isValid("{\"key\":\"value\"}");
 *
 * // Detailed validation with error messages
 * ValidationResult result = JsonValidator.validate("{\"invalid json");
 * if (!result.isValid()) {
 *     System.out.println("Error: " + result.getErrorMessage());
 * }
 *
 * // Validate byte array (zero-copy)
 * byte[] jsonBytes = loadFromFile();
 * boolean isValid = JsonValidator.isValid(jsonBytes);
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
     * If you need error details, use {@link #validate(String)} instead.
     *
     * @param json the JSON string to validate
     * @return {@code true} if JSON is valid, {@code false} otherwise
     * @throws NullPointerException if json is null
     * @see #validate(String)
     */
    public static boolean isValid(String json) {
        if (json == null) {
            throw new NullPointerException("JSON string cannot be null");
        }

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return NativeLib.validateJson(bytes);
    }

    /**
     * Validates JSON byte array (zero-copy).
     *
     * <p>This method is more efficient than {@link #isValid(String)} as it
     * avoids String → byte[] conversion. Use this when working with raw bytes
     * from files, network, etc.
     *
     * @param jsonBytes the JSON data as UTF-8 encoded bytes
     * @return {@code true} if JSON is valid, {@code false} otherwise
     * @throws NullPointerException if jsonBytes is null
     */
    public static boolean isValid(byte[] jsonBytes) {
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
    public static ValidationResult validate(String json) {
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
    public static ValidationResult validate(byte[] jsonBytes) {
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

    /**
     * Gets the version of the validator library.
     *
     * @return version string (e.g., "0.1.0")
     */
    public static String getVersion() {
        return "0.1.0-SNAPSHOT";
    }
}
