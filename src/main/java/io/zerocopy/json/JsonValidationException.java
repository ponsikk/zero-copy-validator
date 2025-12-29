package io.zerocopy.json;

/**
 * Exception thrown when JSON validation fails.
 *
 * <p>This exception can be thrown by {@link ValidationResult#throwIfInvalid()}
 * for fail-fast validation scenarios.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * try {
 *     JsonValidator.validate(json).throwIfInvalid();
 *     // Process valid JSON
 * } catch (JsonValidationException e) {
 *     System.err.println("Invalid JSON: " + e.getMessage());
 *     System.err.println("Error code: " + e.getErrorCode());
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
public class JsonValidationException extends Exception {

    private final ValidationResult.ErrorCode errorCode;

    /**
     * Constructs a new exception with error code and message.
     *
     * @param errorCode the error code
     * @param message the error message
     */
    public JsonValidationException(ValidationResult.ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * Gets the error code that caused this exception.
     *
     * @return the error code
     */
    public ValidationResult.ErrorCode getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "JsonValidationException{" +
               "errorCode=" + errorCode +
               ", message='" + getMessage() + "'}";
    }
}
