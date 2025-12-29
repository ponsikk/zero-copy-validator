package io.zerocopy;

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

    /**
     * Creates a validation result.
     *
     * @param errorCode the error code (0 for success)
     * @param errorMessage the error message (empty string for success)
     */
    public ValidationResult(int errorCode, String errorMessage) {
        this.errorCode = ErrorCode.fromCode(errorCode);
        this.valid = (errorCode == 0);
        this.errorMessage = errorMessage != null ? errorMessage : "";
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
     * Gets the error code.
     *
     * @return error code (OK if valid)
     */
    public ErrorCode getErrorCode() {
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
     * Throws exception if validation failed.
     *
     * @throws JsonValidationException if JSON is invalid
     */
    public void throwIfInvalid() throws JsonValidationException {
        if (!valid) {
            throw new JsonValidationException(errorCode, errorMessage);
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
            return "ValidationResult{valid=true}";
        } else {
            return "ValidationResult{valid=false, errorCode=" + errorCode +
                   ", errorMessage='" + errorMessage + "'}";
        }
    }
}
