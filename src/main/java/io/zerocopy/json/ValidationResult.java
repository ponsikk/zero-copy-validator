package io.zerocopy.json;

import java.util.Objects;

/**
 * Result of JSON validation containing status and error details.
 *
 * <p>This class is immutable and thread-safe.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * ValidationResult result = JsonValidator.validate("{\"invalid\"");
 *
 * if (result.isValid()) {
 *     // Process valid JSON
 * } else {
 *     System.err.println("Validation failed: " + result.getErrorMessage());
 *     System.err.println("Error code: " + result.getErrorCode());
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
public final class ValidationResult {

    /**
     * Error codes returned by the validator.
     */
    public enum ErrorCode {
        /** No error - JSON is valid */
        OK(0),

        /** Null pointer passed to validator */
        NULL_POINTER(-1),

        /** Invalid UTF-8 encoding in input */
        INVALID_UTF8(-2),

        /** JSON data is too large (> 100MB) or empty */
        TOO_LARGE(-3),

        /** JSON syntax error */
        SYNTAX_ERROR(1);

        private final int code;

        ErrorCode(int code) {
            this.code = code;
        }

        /**
         * Gets the numeric error code.
         *
         * @return error code value
         */
        public int getCode() {
            return code;
        }

        /**
         * Converts numeric code to ErrorCode enum.
         *
         * @param code the numeric error code
         * @return corresponding ErrorCode
         * @throws IllegalArgumentException if code is unknown
         */
        public static ErrorCode fromCode(int code) {
            for (ErrorCode ec : values()) {
                if (ec.code == code) {
                    return ec;
                }
            }
            throw new IllegalArgumentException("Unknown error code: " + code);
        }
    }

    private final boolean valid;
    private final ErrorCode errorCode;
    private final String errorMessage;

    // Error location information (0 if unknown)
    private final int line;
    private final int column;
    private final long byteOffset;

    /**
     * Creates a validation result.
     *
     * @param errorCode the error code (0 for success)
     * @param errorMessage the error message (empty string for success)
     */
    public ValidationResult(int errorCode, String errorMessage) {
        this(errorCode, errorMessage, 0, 0, 0);
    }

    /**
     * Creates a validation result with error location.
     *
     * @param errorCode the error code (0 for success)
     * @param errorMessage the error message (empty string for success)
     * @param line line number (1-indexed, 0 if unknown)
     * @param column column number (1-indexed, 0 if unknown)
     * @param byteOffset byte offset from start (0 if unknown)
     */
    public ValidationResult(int errorCode, String errorMessage, int line, int column, long byteOffset) {
        this.errorCode = ErrorCode.fromCode(errorCode);
        this.valid = (errorCode == 0);
        this.errorMessage = errorMessage;
        this.line = line;
        this.column = column;
        this.byteOffset = byteOffset;
    }

    /**
     * Checks if JSON is valid.
     *
     * @return {@code true} if JSON is valid, {@code false} otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Gets the error code as integer value.
     *
     * @return error code (0 if valid, negative for input errors, positive for JSON errors)
     */
    public int getErrorCode() {
        return errorCode.getCode();
    }

    /**
     * Gets the error code as enum.
     *
     * @return error code enum
     */
    public ErrorCode getErrorCodeEnum() {
        return errorCode;
    }

    /**
     * Gets the error message.
     *
     * @return error message (empty string if valid)
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Gets the line number where error occurred.
     *
     * @return line number (1-indexed), or 0 if unknown
     */
    public int getLine() {
        return line;
    }

    /**
     * Gets the column number where error occurred.
     *
     * @return column number (1-indexed), or 0 if unknown
     */
    public int getColumn() {
        return column;
    }

    /**
     * Gets the byte offset where error occurred.
     *
     * @return byte offset from start of JSON, or 0 if unknown
     */
    public long getByteOffset() {
        return byteOffset;
    }

    /**
     * Gets formatted error location string.
     *
     * @return formatted location (e.g., "at line 5, column 12") or empty if unknown
     */
    public String getErrorLocation() {
        if (line > 0 && column > 0) {
            return String.format("at line %d, column %d", line, column);
        } else if (byteOffset > 0) {
            return String.format("at byte offset %d", byteOffset);
        }
        return "";
    }

    /**
     * Throws exception if validation failed.
     *
     * @throws JsonValidationException if JSON is invalid
     */
    public void throwIfInvalid() throws JsonValidationException {
        if (!valid) {
            String fullMessage = errorMessage;
            if (line > 0 && column > 0) {
                fullMessage = String.format("%s at line %d, column %d", errorMessage, line, column);
            }
            throw new JsonValidationException(errorCode, fullMessage);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return valid == that.valid &&
               errorCode == that.errorCode &&
               Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, errorCode, errorMessage);
    }

    @Override
    public String toString() {
        if (valid) {
            return "ValidationResult{status=valid}";
        } else {
            StringBuilder sb = new StringBuilder("ValidationResult{status=invalid, errorCode=")
                    .append(errorCode.getCode())
                    .append(", errorMessage='").append(errorMessage).append("'");

            if (line > 0 && column > 0) {
                sb.append(", line=").append(line)
                  .append(", column=").append(column);
            }
            if (byteOffset > 0) {
                sb.append(", byteOffset=").append(byteOffset);
            }

            return sb.append("}").toString();
        }
    }
}
