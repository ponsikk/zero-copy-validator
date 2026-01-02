package io.zerocopy.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link ValidationResult}.
 */
@DisplayName("ValidationResult")
class ValidationResultTest {

    @Nested
    @DisplayName("Constructor and Getters")
    class ConstructorTests {

        @Test
        @DisplayName("should create result with all fields")
        void shouldCreateResultWithAllFields() {
            ValidationResult result = new ValidationResult(
                    1,           // errorCode
                    "Syntax error at line 2",  // errorMessage
                    2,           // line
                    5,           // column
                    42           // byteOffset
            );

            assertThat(result.getErrorCode()).isEqualTo(1);
            assertThat(result.getErrorMessage()).isEqualTo("Syntax error at line 2");
            assertThat(result.getLine()).isEqualTo(2);
            assertThat(result.getColumn()).isEqualTo(5);
            assertThat(result.getByteOffset()).isEqualTo(42);
        }

        @Test
        @DisplayName("should create valid result")
        void shouldCreateValidResult() {
            ValidationResult result = new ValidationResult(0, "", 0, 0, 0);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrorCode()).isEqualTo(0);
            assertThat(result.getErrorMessage()).isEmpty();
        }

        @Test
        @DisplayName("should create invalid result")
        void shouldCreateInvalidResult() {
            ValidationResult result = new ValidationResult(1, "Error", 1, 1, 10);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorCode()).isEqualTo(1);
            assertThat(result.getErrorMessage()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("isValid() Method")
    class IsValidTests {

        @Test
        @DisplayName("should return true for error code 0")
        void shouldReturnTrueForZero() {
            ValidationResult result = new ValidationResult(0, "", 0, 0, 0);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should return false for positive error code")
        void shouldReturnFalseForPositive() {
            ValidationResult result = new ValidationResult(1, "Syntax error", 1, 1, 0);
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("should return false for negative error code")
        void shouldReturnFalseForNegative() {
            ValidationResult result = new ValidationResult(-1, "Null pointer", 0, 0, 0);
            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Error Information")
    class ErrorInfoTests {

        @Test
        @DisplayName("should provide line number for syntax errors")
        void shouldProvideLineNumber() {
            ValidationResult result = new ValidationResult(1, "Syntax error", 5, 10, 42);
            assertThat(result.getLine()).isEqualTo(5);
        }

        @Test
        @DisplayName("should provide column number for syntax errors")
        void shouldProvideColumnNumber() {
            ValidationResult result = new ValidationResult(1, "Syntax error", 5, 10, 42);
            assertThat(result.getColumn()).isEqualTo(10);
        }

        @Test
        @DisplayName("should provide byte offset")
        void shouldProvideByteOffset() {
            ValidationResult result = new ValidationResult(1, "Syntax error", 5, 10, 42);
            assertThat(result.getByteOffset()).isEqualTo(42);
        }

        @Test
        @DisplayName("should have zero line/column for valid result")
        void shouldHaveZeroLineColumnForValid() {
            ValidationResult result = new ValidationResult(0, "", 0, 0, 0);
            assertThat(result.getLine()).isEqualTo(0);
            assertThat(result.getColumn()).isEqualTo(0);
            assertThat(result.getByteOffset()).isEqualTo(0);
        }

        @Test
        @DisplayName("should have empty message for valid result")
        void shouldHaveEmptyMessageForValid() {
            ValidationResult result = new ValidationResult(0, "", 0, 0, 0);
            assertThat(result.getErrorMessage()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Error Codes")
    class ErrorCodeTests {

        @Test
        @DisplayName("should distinguish success (0)")
        void shouldDistinguishSuccess() {
            ValidationResult result = new ValidationResult(0, "", 0, 0, 0);
            assertThat(result.getErrorCode()).isEqualTo(0);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("should handle syntax error (1)")
        void shouldHandleSyntaxError() {
            ValidationResult result = new ValidationResult(1, "JSON syntax error", 2, 5, 10);
            assertThat(result.getErrorCode()).isEqualTo(1);
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorMessage()).contains("syntax");
        }

        @Test
        @DisplayName("should handle null pointer (-1)")
        void shouldHandleNullPointer() {
            ValidationResult result = new ValidationResult(-1, "Null pointer", 0, 0, 0);
            assertThat(result.getErrorCode()).isEqualTo(-1);
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("should handle invalid UTF-8 (-2)")
        void shouldHandleInvalidUtf8() {
            ValidationResult result = new ValidationResult(-2, "Invalid UTF-8 encoding", 0, 0, 0);
            assertThat(result.getErrorCode()).isEqualTo(-2);
            assertThat(result.isValid()).isFalse();
        }

        @Test
        @DisplayName("should handle too large (-3)")
        void shouldHandleTooLarge() {
            ValidationResult result = new ValidationResult(-3, "JSON too large or empty", 0, 0, 0);
            assertThat(result.getErrorCode()).isEqualTo(-3);
            assertThat(result.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("toString() Method")
    class ToStringTests {

        @Test
        @DisplayName("should provide readable string representation for valid result")
        void shouldProvideReadableStringForValid() {
            ValidationResult result = new ValidationResult(0, "", 0, 0, 0);
            String str = result.toString();

            assertThat(str).contains("ValidationResult");
            assertThat(str).contains("valid");
        }

        @Test
        @DisplayName("should provide readable string representation for invalid result")
        void shouldProvideReadableStringForInvalid() {
            ValidationResult result = new ValidationResult(1, "Syntax error at line 2", 2, 5, 15);
            String str = result.toString();

            assertThat(str).contains("ValidationResult");
            assertThat(str).contains("invalid")
                    .contains("errorCode=1")
                    .contains("line=2")
                    .contains("column=5");
        }
    }

    @Nested
    @DisplayName("Integration with JsonValidator")
    class IntegrationTests {

        @Test
        @DisplayName("should get valid result from validateDetailed()")
        void shouldGetValidResult() {
            String json = "{\"valid\":true}";
            ValidationResult result = JsonValidator.validateDetailed(json);

            assertThat(result).isNotNull();
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrorCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("should get invalid result with error details")
        void shouldGetInvalidResultWithDetails() {
            String json = "{invalid}";
            ValidationResult result = JsonValidator.validateDetailed(json);

            assertThat(result).isNotNull();
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorCode()).isNotEqualTo(0);
            assertThat(result.getErrorMessage()).isNotEmpty();
        }

        @Test
        @DisplayName("should get result with location info for syntax errors")
        void shouldGetLocationInfo() {
            String json = """
                    {
                        "name": "John",
                        "age": invalid
                    }
                    """;
            ValidationResult result = JsonValidator.validateDetailed(json);

            assertThat(result.isValid()).isFalse();
            // Should have line and column set
            assertThat(result.getLine()).isGreaterThan(0);
            assertThat(result.getColumn()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle null error message")
        void shouldHandleNullMessage() {
            ValidationResult result = new ValidationResult(1, null, 0, 0, 0);
            assertThat(result.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("should handle empty error message")
        void shouldHandleEmptyMessage() {
            ValidationResult result = new ValidationResult(1, "", 0, 0, 0);
            assertThat(result.getErrorMessage()).isEmpty();
        }

        @Test
        @DisplayName("should handle large line numbers")
        void shouldHandleLargeLineNumbers() {
            ValidationResult result = new ValidationResult(1, "Error", 999999, 100, 5000000);
            assertThat(result.getLine()).isEqualTo(999999);
            assertThat(result.getColumn()).isEqualTo(100);
            assertThat(result.getByteOffset()).isEqualTo(5000000);
        }

        @Test
        @DisplayName("should handle zero values")
        void shouldHandleZeroValues() {
            ValidationResult result = new ValidationResult(0, "", 0, 0, 0);
            assertThat(result.getLine()).isEqualTo(0);
            assertThat(result.getColumn()).isEqualTo(0);
            assertThat(result.getByteOffset()).isEqualTo(0);
        }
    }
}
