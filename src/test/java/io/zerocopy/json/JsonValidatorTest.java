package io.zerocopy.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JsonValidator} basic validation functionality.
 */
@DisplayName("JsonValidator - Basic Validation")
class JsonValidatorTest {

    @Nested
    @DisplayName("validate(String) - Simple validation")
    class ValidateStringTests {

        @Test
        @DisplayName("should return true for valid JSON object")
        void shouldValidateValidJsonObject() {
            String json = "{\"name\":\"John\",\"age\":30}";
            assertThat(JsonValidator.validate(json)).isTrue();
        }

        @Test
        @DisplayName("should return true for valid JSON array")
        void shouldValidateValidJsonArray() {
            String json = "[1,2,3,4,5]";
            assertThat(JsonValidator.validate(json)).isTrue();
        }

        @Test
        @DisplayName("should return true for valid nested JSON")
        void shouldValidateValidNestedJson() {
            String json = "{\"user\":{\"name\":\"Alice\",\"settings\":{\"theme\":\"dark\"}}}";
            assertThat(JsonValidator.validate(json)).isTrue();
        }

        @Test
        @DisplayName("should return true for empty object")
        void shouldValidateEmptyObject() {
            assertThat(JsonValidator.validate("{}")).isTrue();
        }

        @Test
        @DisplayName("should return true for empty array")
        void shouldValidateEmptyArray() {
            assertThat(JsonValidator.validate("[]")).isTrue();
        }

        @Test
        @DisplayName("should return true for JSON with special characters")
        void shouldValidateJsonWithSpecialChars() {
            String json = "{\"message\":\"Hello\\nWorld\\t!\"}";
            assertThat(JsonValidator.validate(json)).isTrue();
        }

        @Test
        @DisplayName("should return true for JSON with Unicode")
        void shouldValidateJsonWithUnicode() {
            String json = "{\"message\":\"Привет мир 🌍\"}";
            assertThat(JsonValidator.validate(json)).isTrue();
        }

        @Test
        @DisplayName("should return false for invalid JSON - missing quote")
        void shouldRejectMissingQuote() {
            String json = "{\"name:\"John\"}";
            assertThat(JsonValidator.validate(json)).isFalse();
        }

        @Test
        @DisplayName("should return false for invalid JSON - missing comma")
        void shouldRejectMissingComma() {
            String json = "{\"name\":\"John\" \"age\":30}";
            assertThat(JsonValidator.validate(json)).isFalse();
        }

        @Test
        @DisplayName("should return false for invalid JSON - trailing comma")
        void shouldRejectTrailingComma() {
            String json = "{\"name\":\"John\",}";
            assertThat(JsonValidator.validate(json)).isFalse();
        }

        @Test
        @DisplayName("should return false for invalid JSON - unclosed brace")
        void shouldRejectUnclosedBrace() {
            String json = "{\"name\":\"John\"";
            assertThat(JsonValidator.validate(json)).isFalse();
        }

        @Test
        @DisplayName("should return false for invalid JSON - extra brace")
        void shouldRejectExtraBrace() {
            String json = "{\"name\":\"John\"}}";
            assertThat(JsonValidator.validate(json)).isFalse();
        }

        @Test
        @DisplayName("should return false for plain text")
        void shouldRejectPlainText() {
            assertThat(JsonValidator.validate("not json")).isFalse();
        }

        @Test
        @DisplayName("should return false for empty string")
        void shouldRejectEmptyString() {
            assertThat(JsonValidator.validate("")).isFalse();
        }

        @Test
        @DisplayName("should throw NullPointerException for null input")
        void shouldThrowOnNull() {
            assertThatThrownBy(() -> JsonValidator.validate((String) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("JSON string cannot be null");
        }
    }

    @Nested
    @DisplayName("validate(byte[]) - Byte array validation")
    class ValidateByteArrayTests {

        @Test
        @DisplayName("should validate valid JSON byte array")
        void shouldValidateValidJsonBytes() {
            byte[] json = "{\"name\":\"John\"}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.validate(json)).isTrue();
        }

        @Test
        @DisplayName("should reject invalid JSON byte array")
        void shouldRejectInvalidJsonBytes() {
            byte[] json = "{invalid}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.validate(json)).isFalse();
        }

        @Test
        @DisplayName("should validate large JSON (> 1KB)")
        void shouldValidateLargeJson() {
            StringBuilder sb = new StringBuilder("{\"items\":[");
            for (int i = 0; i < 100; i++) {
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(i).append(",\"name\":\"item").append(i).append("\"}");
            }
            sb.append("]}");
            byte[] json = sb.toString().getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.validate(json)).isTrue();
        }

        @Test
        @DisplayName("should throw NullPointerException for null byte array")
        void shouldThrowOnNullBytes() {
            assertThatThrownBy(() -> JsonValidator.validate((byte[]) null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("JSON bytes cannot be null");
        }
    }

    @Nested
    @DisplayName("validateDetailed(String) - Detailed validation with errors")
    class ValidateDetailedStringTests {

        @Test
        @DisplayName("should return valid result for valid JSON")
        void shouldReturnValidResult() {
            String json = "{\"name\":\"John\"}";
            ValidationResult result = JsonValidator.validateDetailed(json);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrorCode()).isEqualTo(0);
            assertThat(result.getErrorMessage()).isEmpty();
        }

        @Test
        @DisplayName("should return error for invalid JSON")
        void shouldReturnErrorForInvalidJson() {
            String json = "{\"name\":\"John\"";  // Missing closing brace
            ValidationResult result = JsonValidator.validateDetailed(json);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorCode()).isNotEqualTo(0);
            assertThat(result.getErrorMessage()).isNotEmpty();
        }

        @Test
        @DisplayName("should include line and column info for syntax errors")
        void shouldIncludeLineColumnInfo() {
            String json = """
                    {
                        "name": "John",
                        "age": invalid
                    }
                    """;
            ValidationResult result = JsonValidator.validateDetailed(json);

            assertThat(result.isValid()).isFalse();
            // Line and column should be set for syntax error
            assertThat(result.getLine()).isGreaterThan(0);
            assertThat(result.getColumn()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should throw NullPointerException for null input")
        void shouldThrowOnNull() {
            assertThatThrownBy(() -> JsonValidator.validateDetailed((String) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("validateDetailed(byte[]) - Detailed byte array validation")
    class ValidateDetailedByteArrayTests {

        @Test
        @DisplayName("should return valid result for valid JSON bytes")
        void shouldReturnValidResult() {
            byte[] json = "{\"valid\":true}".getBytes(StandardCharsets.UTF_8);
            ValidationResult result = JsonValidator.validateDetailed(json);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrorCode()).isEqualTo(0);
        }

        @Test
        @DisplayName("should return error for invalid JSON bytes")
        void shouldReturnErrorForInvalidJsonBytes() {
            byte[] json = "{invalid".getBytes(StandardCharsets.UTF_8);
            ValidationResult result = JsonValidator.validateDetailed(json);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorCode()).isNotEqualTo(0);
        }

        @Test
        @DisplayName("should throw NullPointerException for null bytes")
        void shouldThrowOnNullBytes() {
            assertThatThrownBy(() -> JsonValidator.validateDetailed((byte[]) null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should validate JSON with all primitive types")
        void shouldValidateAllPrimitiveTypes() {
            String json = """
                    {
                        "string": "value",
                        "number": 42,
                        "float": 3.14,
                        "boolean": true,
                        "null": null
                    }
                    """;
            assertThat(JsonValidator.validate(json)).isTrue();
        }

        @Test
        @DisplayName("should validate deeply nested JSON")
        void shouldValidateDeeplyNestedJson() {
            String json = "{\"a\":{\"b\":{\"c\":{\"d\":{\"e\":{\"f\":\"deep\"}}}}}}";
            assertThat(JsonValidator.validate(json)).isTrue();
        }

        @Test
        @DisplayName("should validate JSON with escaped quotes")
        void shouldValidateEscapedQuotes() {
            String json = "{\"quote\":\"He said \\\"Hello\\\"\"}";
            assertThat(JsonValidator.validate(json)).isTrue();
        }

        @Test
        @DisplayName("should validate JSON with numbers in scientific notation")
        void shouldValidateScientificNotation() {
            String json = "{\"value\":1.23e-10}";
            assertThat(JsonValidator.validate(json)).isTrue();
        }

        @Test
        @DisplayName("should reject invalid UTF-8 encoding")
        void shouldRejectInvalidUtf8() {
            // Create invalid UTF-8 byte sequence
            byte[] invalidUtf8 = new byte[]{'{', '"', 'k', '"', ':', (byte) 0xFF, (byte) 0xFE, '}' };
            assertThat(JsonValidator.validate(invalidUtf8)).isFalse();
        }
    }

    @Nested
    @DisplayName("Version Info")
    class VersionTests {

        @Test
        @DisplayName("should return version string")
        void shouldReturnVersion() {
            String version = JsonValidator.getVersion();
            assertThat(version).isNotNull().isNotEmpty();
            assertThat(version).matches("\\d+\\.\\d+\\.\\d+.*");  // Semver pattern
        }
    }
}
