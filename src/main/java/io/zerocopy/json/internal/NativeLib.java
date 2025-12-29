package io.zerocopy.json.internal;

import io.zerocopy.json.ValidationResult;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Internal JNI wrapper for Rust native library.
 *
 * <p><b>WARNING:</b> This class is internal API and subject to change.
 * Do not use directly! Use {@link io.zerocopy.json.JsonValidator} instead.
 *
 * @since 0.1.0
 */
public final class NativeLib {

    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup LIBRARY;
    private static final MethodHandle JSON_VALIDATE;
    private static final MethodHandle JSON_VALIDATE_DETAILED;
    private static final MethodHandle JSON_GET_STRING;
    private static final MethodHandle JSON_GET_NUMBER;
    private static final MethodHandle JSON_GET_BOOL;

    // Phase 2: Validators
    private static final MethodHandle JSON_VALIDATE_FIELD_TYPE;
    private static final MethodHandle JSON_FIELD_EXISTS;
    private static final MethodHandle JSON_FIELD_IS_NULL;

    // Phase 2.2: Range Validators
    private static final MethodHandle JSON_VALIDATE_NUMBER_RANGE;
    private static final MethodHandle JSON_VALIDATE_STRING_LENGTH;
    private static final MethodHandle JSON_VALIDATE_ARRAY_SIZE;

    private static final int ERROR_BUFFER_SIZE = 1024;
    private static final int STRING_BUFFER_SIZE = 4096;

    static {
        try {
            // Load native library
            loadNativeLibrary();

            // Lookup symbols
            LIBRARY = SymbolLookup.loaderLookup();

            // json_validate(ptr: *const u8, len: usize) -> bool
            JSON_VALIDATE = LINKER.downcallHandle(
                    LIBRARY.find("json_validate").orElseThrow(
                            () -> new UnsatisfiedLinkError("Symbol 'json_validate' not found")),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_BOOLEAN,  // return bool
                            ValueLayout.ADDRESS,        // ptr: *const u8
                            ValueLayout.JAVA_LONG       // len: usize
                    )
            );

            // json_validate_detailed(ptr: *const u8, len: usize,
            //                        error_buf: *mut u8, error_buf_len: usize) -> i32
            JSON_VALIDATE_DETAILED = LINKER.downcallHandle(
                    LIBRARY.find("json_validate_detailed").orElseThrow(
                            () -> new UnsatisfiedLinkError("Symbol 'json_validate_detailed' not found")),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT,       // return i32
                            ValueLayout.ADDRESS,        // ptr: *const u8
                            ValueLayout.JAVA_LONG,      // len: usize
                            ValueLayout.ADDRESS,        // error_buf: *mut u8
                            ValueLayout.JAVA_LONG       // error_buf_len: usize
                    )
            );

            // json_get_string(json_ptr, json_len, path_ptr, path_len, output_buf, output_buf_len) -> i32
            JSON_GET_STRING = LINKER.downcallHandle(
                    LIBRARY.find("json_get_string").orElseThrow(
                            () -> new UnsatisfiedLinkError("Symbol 'json_get_string' not found")),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT,       // return i32
                            ValueLayout.ADDRESS,        // json_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // json_len: usize
                            ValueLayout.ADDRESS,        // path_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // path_len: usize
                            ValueLayout.ADDRESS,        // output_buf: *mut u8
                            ValueLayout.JAVA_LONG       // output_buf_len: usize
                    )
            );

            // json_get_number(json_ptr, json_len, path_ptr, path_len, output: *mut f64) -> i32
            JSON_GET_NUMBER = LINKER.downcallHandle(
                    LIBRARY.find("json_get_number").orElseThrow(
                            () -> new UnsatisfiedLinkError("Symbol 'json_get_number' not found")),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT,       // return i32
                            ValueLayout.ADDRESS,        // json_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // json_len: usize
                            ValueLayout.ADDRESS,        // path_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // path_len: usize
                            ValueLayout.ADDRESS         // output: *mut f64
                    )
            );

            // json_get_bool(json_ptr, json_len, path_ptr, path_len, output: *mut bool) -> i32
            JSON_GET_BOOL = LINKER.downcallHandle(
                    LIBRARY.find("json_get_bool").orElseThrow(
                            () -> new UnsatisfiedLinkError("Symbol 'json_get_bool' not found")),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT,       // return i32
                            ValueLayout.ADDRESS,        // json_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // json_len: usize
                            ValueLayout.ADDRESS,        // path_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // path_len: usize
                            ValueLayout.ADDRESS         // output: *mut bool
                    )
            );

            // Phase 2: Validators
            // json_validate_field_type(json_ptr, json_len, path_ptr, path_len, expected_type: i32) -> bool
            JSON_VALIDATE_FIELD_TYPE = LINKER.downcallHandle(
                    LIBRARY.find("json_validate_field_type").orElseThrow(
                            () -> new UnsatisfiedLinkError("Symbol 'json_validate_field_type' not found")),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_BOOLEAN,   // return bool
                            ValueLayout.ADDRESS,        // json_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // json_len: usize
                            ValueLayout.ADDRESS,        // path_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // path_len: usize
                            ValueLayout.JAVA_INT        // expected_type: i32
                    )
            );

            // json_field_exists(json_ptr, json_len, path_ptr, path_len) -> bool
            JSON_FIELD_EXISTS = LINKER.downcallHandle(
                    LIBRARY.find("json_field_exists").orElseThrow(
                            () -> new UnsatisfiedLinkError("Symbol 'json_field_exists' not found")),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_BOOLEAN,   // return bool
                            ValueLayout.ADDRESS,        // json_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // json_len: usize
                            ValueLayout.ADDRESS,        // path_ptr: *const u8
                            ValueLayout.JAVA_LONG       // path_len: usize
                    )
            );

            // json_field_is_null(json_ptr, json_len, path_ptr, path_len) -> bool
            JSON_FIELD_IS_NULL = LINKER.downcallHandle(
                    LIBRARY.find("json_field_is_null").orElseThrow(
                            () -> new UnsatisfiedLinkError("Symbol 'json_field_is_null' not found")),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_BOOLEAN,   // return bool
                            ValueLayout.ADDRESS,        // json_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // json_len: usize
                            ValueLayout.ADDRESS,        // path_ptr: *const u8
                            ValueLayout.JAVA_LONG       // path_len: usize
                    )
            );

            // Phase 2.2: Range Validators
            // json_validate_number_range(json_ptr, json_len, path_ptr, path_len, min: f64, max: f64) -> i32
            JSON_VALIDATE_NUMBER_RANGE = LINKER.downcallHandle(
                    LIBRARY.find("json_validate_number_range").orElseThrow(
                            () -> new UnsatisfiedLinkError("Symbol 'json_validate_number_range' not found")),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT,       // return i32
                            ValueLayout.ADDRESS,        // json_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // json_len: usize
                            ValueLayout.ADDRESS,        // path_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // path_len: usize
                            ValueLayout.JAVA_DOUBLE,    // min: f64
                            ValueLayout.JAVA_DOUBLE     // max: f64
                    )
            );

            // json_validate_string_length(json_ptr, json_len, path_ptr, path_len, min_len: usize, max_len: usize) -> i32
            JSON_VALIDATE_STRING_LENGTH = LINKER.downcallHandle(
                    LIBRARY.find("json_validate_string_length").orElseThrow(
                            () -> new UnsatisfiedLinkError("Symbol 'json_validate_string_length' not found")),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT,       // return i32
                            ValueLayout.ADDRESS,        // json_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // json_len: usize
                            ValueLayout.ADDRESS,        // path_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // path_len: usize
                            ValueLayout.JAVA_LONG,      // min_len: usize
                            ValueLayout.JAVA_LONG       // max_len: usize
                    )
            );

            // json_validate_array_size(json_ptr, json_len, path_ptr, path_len, min_items: usize, max_items: usize) -> i32
            JSON_VALIDATE_ARRAY_SIZE = LINKER.downcallHandle(
                    LIBRARY.find("json_validate_array_size").orElseThrow(
                            () -> new UnsatisfiedLinkError("Symbol 'json_validate_array_size' not found")),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT,       // return i32
                            ValueLayout.ADDRESS,        // json_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // json_len: usize
                            ValueLayout.ADDRESS,        // path_ptr: *const u8
                            ValueLayout.JAVA_LONG,      // path_len: usize
                            ValueLayout.JAVA_LONG,      // min_items: usize
                            ValueLayout.JAVA_LONG       // max_items: usize
                    )
            );

        } catch (Throwable e) {
            throw new ExceptionInInitializerError("Failed to load native library: " + e.getMessage());
        }
    }

    private NativeLib() {
        // Prevent instantiation
    }

    /**
     * Validates JSON (simple boolean result).
     *
     * @param jsonBytes UTF-8 encoded JSON data
     * @return true if valid, false otherwise
     */
    public static boolean validateJson(byte[] jsonBytes) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocateFrom(ValueLayout.JAVA_BYTE, jsonBytes);
            return (boolean) JSON_VALIDATE.invoke(segment, (long) jsonBytes.length);
        } catch (Throwable e) {
            // If FFI call fails, assume invalid JSON
            return false;
        }
    }

    /**
     * Validates JSON with detailed error information.
     *
     * @param jsonBytes UTF-8 encoded JSON data
     * @return ValidationResult with error details
     */
    public static ValidationResult validateJsonDetailed(byte[] jsonBytes) {
        try (Arena arena = Arena.ofConfined()) {
            // Allocate memory for JSON input
            MemorySegment jsonSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, jsonBytes);

            // Allocate buffer for error message
            MemorySegment errorBuffer = arena.allocate(ERROR_BUFFER_SIZE);

            // Call Rust function
            int errorCode = (int) JSON_VALIDATE_DETAILED.invoke(
                    jsonSegment,
                    (long) jsonBytes.length,
                    errorBuffer,
                    (long) ERROR_BUFFER_SIZE
            );

            // Extract error message
            String errorMessage = extractCString(errorBuffer, ERROR_BUFFER_SIZE);

            return new ValidationResult(errorCode, errorMessage);

        } catch (Throwable e) {
            // If FFI call fails completely, return generic error
            return new ValidationResult(-1, "FFI call failed: " + e.getMessage());
        }
    }

    /**
     * Extracts string value from JSON by path (zero-copy).
     *
     * @param jsonBytes UTF-8 encoded JSON data
     * @param path JSONPath query (e.g., "user.name" or "$.items.0.id")
     * @return extracted string value
     * @throws RuntimeException if extraction fails
     */
    public static String getString(byte[] jsonBytes, String path) {
        try (Arena arena = Arena.ofConfined()) {
            // Allocate memory for JSON and path
            MemorySegment jsonSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, jsonBytes);
            MemorySegment pathSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, path.getBytes());

            // Allocate buffer for result string
            MemorySegment outputBuffer = arena.allocate(STRING_BUFFER_SIZE);

            // Call Rust function
            int errorCode = (int) JSON_GET_STRING.invoke(
                    jsonSegment,
                    (long) jsonBytes.length,
                    pathSegment,
                    (long) path.getBytes().length,
                    outputBuffer,
                    (long) STRING_BUFFER_SIZE
            );

            // Check result
            if (errorCode != 0) {
                throw new RuntimeException("Failed to extract string: error code " + errorCode);
            }

            // Extract result
            return extractCString(outputBuffer, STRING_BUFFER_SIZE);

        } catch (Throwable e) {
            throw new RuntimeException("getString failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts numeric value from JSON by path.
     *
     * @param jsonBytes UTF-8 encoded JSON data
     * @param path JSONPath query
     * @return extracted number
     * @throws RuntimeException if extraction fails
     */
    public static double getNumber(byte[] jsonBytes, String path) {
        try (Arena arena = Arena.ofConfined()) {
            // Allocate memory for JSON and path
            MemorySegment jsonSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, jsonBytes);
            MemorySegment pathSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, path.getBytes());

            // Allocate output for f64
            MemorySegment outputSegment = arena.allocate(ValueLayout.JAVA_DOUBLE);

            // Call Rust function
            int errorCode = (int) JSON_GET_NUMBER.invoke(
                    jsonSegment,
                    (long) jsonBytes.length,
                    pathSegment,
                    (long) path.getBytes().length,
                    outputSegment
            );

            // Check result
            if (errorCode != 0) {
                throw new RuntimeException("Failed to extract number: error code " + errorCode);
            }

            // Extract result
            return outputSegment.get(ValueLayout.JAVA_DOUBLE, 0);

        } catch (Throwable e) {
            throw new RuntimeException("getNumber failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts boolean value from JSON by path.
     *
     * @param jsonBytes UTF-8 encoded JSON data
     * @param path JSONPath query
     * @return extracted boolean
     * @throws RuntimeException if extraction fails
     */
    public static boolean getBoolean(byte[] jsonBytes, String path) {
        try (Arena arena = Arena.ofConfined()) {
            // Allocate memory for JSON and path
            MemorySegment jsonSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, jsonBytes);
            MemorySegment pathSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, path.getBytes());

            // Allocate output for bool
            MemorySegment outputSegment = arena.allocate(ValueLayout.JAVA_BOOLEAN);

            // Call Rust function
            int errorCode = (int) JSON_GET_BOOL.invoke(
                    jsonSegment,
                    (long) jsonBytes.length,
                    pathSegment,
                    (long) path.getBytes().length,
                    outputSegment
            );

            // Check result
            if (errorCode != 0) {
                throw new RuntimeException("Failed to extract boolean: error code " + errorCode);
            }

            // Extract result
            return outputSegment.get(ValueLayout.JAVA_BOOLEAN, 0);

        } catch (Throwable e) {
            throw new RuntimeException("getBoolean failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts null-terminated C string from memory segment.
     */
    private static String extractCString(MemorySegment segment, int maxLen) {
        byte[] bytes = new byte[maxLen];
        for (int i = 0; i < maxLen; i++) {
            byte b = segment.get(ValueLayout.JAVA_BYTE, i);
            if (b == 0) {
                // Found null terminator
                return new String(bytes, 0, i);
            }
            bytes[i] = b;
        }
        return new String(bytes);
    }

    /**
     * Loads the native library from resources.
     */
    private static void loadNativeLibrary() {
        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        String libraryName;
        String libraryPath;
        String platform;

        // Normalize architecture name
        boolean isArm = arch.contains("aarch64") || arch.contains("arm64");

        // Determine library name and path based on OS and architecture
        if (os.contains("win")) {
            libraryName = "json_validator_ffi.dll";
            platform = "windows-x86_64"; // Windows is typically x86_64
            libraryPath = "/native/" + platform + "/" + libraryName;
        } else if (os.contains("mac")) {
            libraryName = "libjson_validator_ffi.dylib";
            platform = isArm ? "darwin-aarch64" : "darwin-x86_64";
            libraryPath = "/native/" + platform + "/" + libraryName;
        } else if (os.contains("nix") || os.contains("nux")) {
            libraryName = "libjson_validator_ffi.so";
            platform = isArm ? "linux-aarch64" : "linux-x86_64";
            libraryPath = "/native/" + platform + "/" + libraryName;
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + os);
        }

        try {
            // Try to load from resources (for JAR distribution)
            InputStream libStream = NativeLib.class.getResourceAsStream(libraryPath);

            if (libStream != null) {
                // Extract to temp file
                Path tempFile = Files.createTempFile("json_validator_ffi", libraryName);
                tempFile.toFile().deleteOnExit();

                Files.copy(libStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
                libStream.close();

                System.load(tempFile.toAbsolutePath().toString());
            } else {
                // Fallback: try to load from java.library.path
                System.loadLibrary("json_validator_ffi");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load native library: " + e.getMessage(), e);
        }
    }

    // ============================================
    // Phase 2: Validators
    // ============================================

    /**
     * Validates that a JSON field has the expected type.
     *
     * @param jsonBytes UTF-8 encoded JSON data
     * @param path JSONPath query (e.g., "user.name")
     * @param expectedType Expected JsonType ordinal
     * @return true if field exists and has expected type
     */
    public static boolean validateFieldType(byte[] jsonBytes, String path, int expectedType) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment jsonSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, jsonBytes);
            MemorySegment pathSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, path.getBytes());

            return (boolean) JSON_VALIDATE_FIELD_TYPE.invoke(
                    jsonSegment,
                    (long) jsonBytes.length,
                    pathSegment,
                    (long) path.getBytes().length,
                    expectedType
            );
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Checks if a JSON field exists at the given path.
     *
     * @param jsonBytes UTF-8 encoded JSON data
     * @param path JSONPath query
     * @return true if field exists (regardless of type or value)
     */
    public static boolean fieldExists(byte[] jsonBytes, String path) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment jsonSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, jsonBytes);
            MemorySegment pathSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, path.getBytes());

            return (boolean) JSON_FIELD_EXISTS.invoke(
                    jsonSegment,
                    (long) jsonBytes.length,
                    pathSegment,
                    (long) path.getBytes().length
            );
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * Checks if a JSON field is null.
     *
     * @param jsonBytes UTF-8 encoded JSON data
     * @param path JSONPath query
     * @return true if field exists AND is null
     */
    public static boolean fieldIsNull(byte[] jsonBytes, String path) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment jsonSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, jsonBytes);
            MemorySegment pathSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, path.getBytes());

            return (boolean) JSON_FIELD_IS_NULL.invoke(
                    jsonSegment,
                    (long) jsonBytes.length,
                    pathSegment,
                    (long) path.getBytes().length
            );
        } catch (Throwable e) {
            return false;
        }
    }

    // ============================================
    // Phase 2.2: Range Validators
    // ============================================

    /**
     * Validates that a numeric field is within the specified range.
     *
     * @param jsonBytes UTF-8 encoded JSON data
     * @param path JSONPath query
     * @param min Minimum value (inclusive)
     * @param max Maximum value (inclusive)
     * @return 0 if valid, error code otherwise
     */
    public static int validateNumberRange(byte[] jsonBytes, String path, double min, double max) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment jsonSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, jsonBytes);
            MemorySegment pathSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, path.getBytes());

            return (int) JSON_VALIDATE_NUMBER_RANGE.invoke(
                    jsonSegment,
                    (long) jsonBytes.length,
                    pathSegment,
                    (long) path.getBytes().length,
                    min,
                    max
            );
        } catch (Throwable e) {
            return -1; // Generic error
        }
    }

    /**
     * Validates that a string field length is within the specified range.
     *
     * @param jsonBytes UTF-8 encoded JSON data
     * @param path JSONPath query
     * @param minLength Minimum string length (inclusive)
     * @param maxLength Maximum string length (inclusive)
     * @return 0 if valid, error code otherwise
     */
    public static int validateStringLength(byte[] jsonBytes, String path, int minLength, int maxLength) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment jsonSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, jsonBytes);
            MemorySegment pathSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, path.getBytes());

            return (int) JSON_VALIDATE_STRING_LENGTH.invoke(
                    jsonSegment,
                    (long) jsonBytes.length,
                    pathSegment,
                    (long) path.getBytes().length,
                    (long) minLength,
                    (long) maxLength
            );
        } catch (Throwable e) {
            return -1; // Generic error
        }
    }

    /**
     * Validates that an array size is within the specified range.
     *
     * @param jsonBytes UTF-8 encoded JSON data
     * @param path JSONPath query
     * @param minItems Minimum number of items (inclusive)
     * @param maxItems Maximum number of items (inclusive)
     * @return 0 if valid, error code otherwise
     */
    public static int validateArraySize(byte[] jsonBytes, String path, int minItems, int maxItems) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment jsonSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, jsonBytes);
            MemorySegment pathSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, path.getBytes());

            return (int) JSON_VALIDATE_ARRAY_SIZE.invoke(
                    jsonSegment,
                    (long) jsonBytes.length,
                    pathSegment,
                    (long) path.getBytes().length,
                    (long) minItems,
                    (long) maxItems
            );
        } catch (Throwable e) {
            return -1; // Generic error
        }
    }
}
