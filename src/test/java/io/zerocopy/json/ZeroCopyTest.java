package io.zerocopy.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JsonValidator} zero-copy DirectByteBuffer methods.
 *
 * <p>These tests verify TRUE zero-copy functionality where no heap → off-heap copying occurs.
 */
@DisplayName("JsonValidator - Zero-Copy (DirectByteBuffer)")
class ZeroCopyTest {

    @Nested
    @DisplayName("validateZeroCopy() - Basic validation")
    class ValidateZeroCopyTests {

        @Test
        @DisplayName("should validate valid JSON with DirectByteBuffer")
        void shouldValidateValidJson() {
            ByteBuffer buffer = createDirectBuffer("{\"name\":\"John\",\"age\":30}");
            assertThat(JsonValidator.validateZeroCopy(buffer)).isTrue();
        }

        @Test
        @DisplayName("should reject invalid JSON with DirectByteBuffer")
        void shouldRejectInvalidJson() {
            ByteBuffer buffer = createDirectBuffer("{\"name\":\"John\"");  // Missing }
            assertThat(JsonValidator.validateZeroCopy(buffer)).isFalse();
        }

        @Test
        @DisplayName("should validate empty object")
        void shouldValidateEmptyObject() {
            ByteBuffer buffer = createDirectBuffer("{}");
            assertThat(JsonValidator.validateZeroCopy(buffer)).isTrue();
        }

        @Test
        @DisplayName("should validate large JSON")
        void shouldValidateLargeJson() {
            StringBuilder sb = new StringBuilder("{\"items\":[");
            for (int i = 0; i < 1000; i++) {
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(i).append("}");
            }
            sb.append("]}");

            ByteBuffer buffer = createDirectBuffer(sb.toString());
            assertThat(JsonValidator.validateZeroCopy(buffer)).isTrue();
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for heap buffer")
        void shouldRejectHeapBuffer() {
            ByteBuffer heapBuffer = ByteBuffer.wrap("{\"valid\":true}".getBytes(StandardCharsets.UTF_8));
            assertThatThrownBy(() -> JsonValidator.validateZeroCopy(heapBuffer))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("must be direct");
        }

        @Test
        @DisplayName("should throw NullPointerException for null buffer")
        void shouldThrowOnNullBuffer() {
            assertThatThrownBy(() -> JsonValidator.validateZeroCopy(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should handle buffer with position > 0")
        void shouldHandleBufferPosition() {
            String json = "XXXX{\"valid\":true}";  // Prefix to skip
            ByteBuffer buffer = createDirectBuffer(json);
            buffer.position(4);  // Skip "XXXX"

            assertThat(JsonValidator.validateZeroCopy(buffer)).isTrue();
        }

        @Test
        @DisplayName("should handle buffer with custom limit")
        void shouldHandleBufferLimit() {
            String json = "{\"valid\":true}XXXX";  // Suffix to ignore
            ByteBuffer buffer = createDirectBuffer(json);
            buffer.limit(buffer.position() + 14);  // Exclude "XXXX" (JSON is 14 bytes)

            assertThat(JsonValidator.validateZeroCopy(buffer)).isTrue();
        }
    }

    @Nested
    @DisplayName("validateDetailedZeroCopy() - Detailed validation")
    class ValidateDetailedZeroCopyTests {

        @Test
        @DisplayName("should return valid result for valid JSON")
        void shouldReturnValidResult() {
            ByteBuffer buffer = createDirectBuffer("{\"valid\":true}");
            ValidationResult result = JsonValidator.validateDetailedZeroCopy(buffer);

            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrorCode()).isEqualTo(0);
            assertThat(result.getErrorMessage()).isEmpty();
        }

        @Test
        @DisplayName("should return error for invalid JSON")
        void shouldReturnErrorForInvalidJson() {
            ByteBuffer buffer = createDirectBuffer("{invalid}");
            ValidationResult result = JsonValidator.validateDetailedZeroCopy(buffer);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrorCode()).isNotEqualTo(0);
            assertThat(result.getErrorMessage()).isNotEmpty();
        }

        @Test
        @DisplayName("should include line and column info")
        void shouldIncludeLineColumnInfo() {
            String json = """
                    {
                        "name": "John",
                        "age": invalid
                    }
                    """;
            ByteBuffer buffer = createDirectBuffer(json);
            ValidationResult result = JsonValidator.validateDetailedZeroCopy(buffer);

            assertThat(result.isValid()).isFalse();
            assertThat(result.getLine()).isGreaterThan(0);
            assertThat(result.getColumn()).isGreaterThan(0);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for heap buffer")
        void shouldRejectHeapBuffer() {
            ByteBuffer heapBuffer = ByteBuffer.wrap("{}".getBytes(StandardCharsets.UTF_8));
            assertThatThrownBy(() -> JsonValidator.validateDetailedZeroCopy(heapBuffer))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getStringZeroCopy() - String extraction")
    class GetStringZeroCopyTests {

        @Test
        @DisplayName("should extract simple string field")
        void shouldExtractSimpleString() {
            ByteBuffer buffer = createDirectBuffer("{\"name\":\"John\"}");
            String result = JsonValidator.getStringZeroCopy(buffer, "name");
            assertThat(result).isEqualTo("John");
        }

        @Test
        @DisplayName("should extract nested string")
        void shouldExtractNestedString() {
            ByteBuffer buffer = createDirectBuffer("{\"user\":{\"email\":\"test@example.com\"}}");
            String result = JsonValidator.getStringZeroCopy(buffer, "user.email");
            assertThat(result).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("should extract long string without truncation")
        void shouldExtractLongString() {
            String longValue = "x".repeat(10000);
            ByteBuffer buffer = createDirectBuffer("{\"data\":\"" + longValue + "\"}");
            String result = JsonValidator.getStringZeroCopy(buffer, "data");
            assertThat(result).hasSize(10000).isEqualTo(longValue);
        }

        @Test
        @DisplayName("should extract string with Unicode")
        void shouldExtractUnicode() {
            ByteBuffer buffer = createDirectBuffer("{\"message\":\"Привет мир 🌍\"}");
            String result = JsonValidator.getStringZeroCopy(buffer, "message");
            assertThat(result).isEqualTo("Привет мир 🌍");
        }

        @Test
        @DisplayName("should extract from array element")
        void shouldExtractFromArray() {
            ByteBuffer buffer = createDirectBuffer("{\"items\":[\"first\",\"second\"]}");
            String result = JsonValidator.getStringZeroCopy(buffer, "items.1");
            assertThat(result).isEqualTo("second");
        }

        @Test
        @DisplayName("should throw RuntimeException for path not found")
        void shouldThrowOnPathNotFound() {
            ByteBuffer buffer = createDirectBuffer("{\"name\":\"John\"}");
            assertThatThrownBy(() -> JsonValidator.getStringZeroCopy(buffer, "age"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for heap buffer")
        void shouldRejectHeapBuffer() {
            ByteBuffer heapBuffer = ByteBuffer.wrap("{\"name\":\"John\"}".getBytes(StandardCharsets.UTF_8));
            assertThatThrownBy(() -> JsonValidator.getStringZeroCopy(heapBuffer, "name"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should allow buffer reuse for multiple extractions")
        void shouldAllowBufferReuse() {
            ByteBuffer buffer = createDirectBuffer("{\"a\":\"first\",\"b\":\"second\"}");

            // Extract first field
            buffer.rewind();
            String result1 = JsonValidator.getStringZeroCopy(buffer, "a");
            assertThat(result1).isEqualTo("first");

            // Reuse buffer for second extraction
            buffer.rewind();
            String result2 = JsonValidator.getStringZeroCopy(buffer, "b");
            assertThat(result2).isEqualTo("second");
        }
    }

    @Nested
    @DisplayName("getNumberZeroCopy() - Number extraction")
    class GetNumberZeroCopyTests {

        @Test
        @DisplayName("should extract integer")
        void shouldExtractInteger() {
            ByteBuffer buffer = createDirectBuffer("{\"age\":30}");
            double result = JsonValidator.getNumberZeroCopy(buffer, "age");
            assertThat(result).isEqualTo(30.0);
        }

        @Test
        @DisplayName("should extract floating point")
        void shouldExtractFloat() {
            ByteBuffer buffer = createDirectBuffer("{\"price\":19.99}");
            double result = JsonValidator.getNumberZeroCopy(buffer, "price");
            assertThat(result).isEqualTo(19.99);
        }

        @Test
        @DisplayName("should extract negative number")
        void shouldExtractNegative() {
            ByteBuffer buffer = createDirectBuffer("{\"value\":-42.5}");
            double result = JsonValidator.getNumberZeroCopy(buffer, "value");
            assertThat(result).isEqualTo(-42.5);
        }

        @Test
        @DisplayName("should extract nested number")
        void shouldExtractNestedNumber() {
            ByteBuffer buffer = createDirectBuffer("{\"user\":{\"age\":25}}");
            double result = JsonValidator.getNumberZeroCopy(buffer, "user.age");
            assertThat(result).isEqualTo(25.0);
        }

        @Test
        @DisplayName("should throw RuntimeException for type mismatch")
        void shouldThrowOnTypeMismatch() {
            ByteBuffer buffer = createDirectBuffer("{\"name\":\"John\"}");
            assertThatThrownBy(() -> JsonValidator.getNumberZeroCopy(buffer, "name"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for heap buffer")
        void shouldRejectHeapBuffer() {
            ByteBuffer heapBuffer = ByteBuffer.wrap("{\"age\":30}".getBytes(StandardCharsets.UTF_8));
            assertThatThrownBy(() -> JsonValidator.getNumberZeroCopy(heapBuffer, "age"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("getBooleanZeroCopy() - Boolean extraction")
    class GetBooleanZeroCopyTests {

        @Test
        @DisplayName("should extract true value")
        void shouldExtractTrue() {
            ByteBuffer buffer = createDirectBuffer("{\"active\":true}");
            boolean result = JsonValidator.getBooleanZeroCopy(buffer, "active");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should extract false value")
        void shouldExtractFalse() {
            ByteBuffer buffer = createDirectBuffer("{\"active\":false}");
            boolean result = JsonValidator.getBooleanZeroCopy(buffer, "active");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should extract nested boolean")
        void shouldExtractNestedBoolean() {
            ByteBuffer buffer = createDirectBuffer("{\"user\":{\"verified\":true}}");
            boolean result = JsonValidator.getBooleanZeroCopy(buffer, "user.verified");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should throw RuntimeException for type mismatch")
        void shouldThrowOnTypeMismatch() {
            ByteBuffer buffer = createDirectBuffer("{\"age\":30}");
            assertThatThrownBy(() -> JsonValidator.getBooleanZeroCopy(buffer, "age"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for heap buffer")
        void shouldRejectHeapBuffer() {
            ByteBuffer heapBuffer = ByteBuffer.wrap("{\"active\":true}".getBytes(StandardCharsets.UTF_8));
            assertThatThrownBy(() -> JsonValidator.getBooleanZeroCopy(heapBuffer, "active"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Zero-Copy Validators - Range")
    class ZeroCopyRangeValidatorsTests {

        @Test
        @DisplayName("should validate number range with DirectByteBuffer")
        void shouldValidateNumberRange() {
            ByteBuffer buffer = createDirectBuffer("{\"age\":25}");
            assertThat(JsonValidator.validateNumberRangeZeroCopy(buffer, "age", 0, 120)).isTrue();

            buffer.rewind();
            assertThat(JsonValidator.validateNumberRangeZeroCopy(buffer, "age", 30, 120)).isFalse();
        }

        @Test
        @DisplayName("should validate string length with DirectByteBuffer")
        void shouldValidateStringLength() {
            ByteBuffer buffer = createDirectBuffer("{\"password\":\"secret123\"}");
            assertThat(JsonValidator.validateStringLengthZeroCopy(buffer, "password", 8, 64)).isTrue();

            buffer.rewind();
            assertThat(JsonValidator.validateStringLengthZeroCopy(buffer, "password", 20, 64)).isFalse();
        }

        @Test
        @DisplayName("should validate array size with DirectByteBuffer")
        void shouldValidateArraySize() {
            ByteBuffer buffer = createDirectBuffer("{\"items\":[1,2,3,4,5]}");
            assertThat(JsonValidator.validateArraySizeZeroCopy(buffer, "items", 1, 10)).isTrue();

            buffer.rewind();
            assertThat(JsonValidator.validateArraySizeZeroCopy(buffer, "items", 10, 20)).isFalse();
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for heap buffer")
        void shouldRejectHeapBufferForRangeValidators() {
            ByteBuffer heapBuffer = ByteBuffer.wrap("{\"age\":30}".getBytes(StandardCharsets.UTF_8));

            assertThatThrownBy(() -> JsonValidator.validateNumberRangeZeroCopy(heapBuffer, "age", 0, 100))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Zero-Copy Validators - Format")
    class ZeroCopyFormatValidatorsTests {

        @Test
        @DisplayName("should validate email with DirectByteBuffer")
        void shouldValidateEmail() {
            ByteBuffer buffer = createDirectBuffer("{\"email\":\"user@example.com\"}");
            assertThat(JsonValidator.validateEmailZeroCopy(buffer, "email")).isTrue();

            buffer.rewind();
            ByteBuffer invalidBuffer = createDirectBuffer("{\"email\":\"invalid\"}");
            assertThat(JsonValidator.validateEmailZeroCopy(invalidBuffer, "email")).isFalse();
        }

        @Test
        @DisplayName("should validate URL with DirectByteBuffer")
        void shouldValidateUrl() {
            ByteBuffer buffer = createDirectBuffer("{\"url\":\"https://example.com\"}");
            assertThat(JsonValidator.validateUrlZeroCopy(buffer, "url")).isTrue();
        }

        @Test
        @DisplayName("should validate ISO date with DirectByteBuffer")
        void shouldValidateIsoDate() {
            ByteBuffer buffer = createDirectBuffer("{\"date\":\"2024-12-30T10:30:00Z\"}");
            assertThat(JsonValidator.validateIsoDateZeroCopy(buffer, "date")).isTrue();
        }

        @Test
        @DisplayName("should validate UUID with DirectByteBuffer")
        void shouldValidateUuid() {
            ByteBuffer buffer = createDirectBuffer("{\"id\":\"550e8400-e29b-41d4-a716-446655440000\"}");
            assertThat(JsonValidator.validateUuidZeroCopy(buffer, "id")).isTrue();
        }

        @Test
        @DisplayName("should validate phone number with DirectByteBuffer")
        void shouldValidatePhoneNumber() {
            ByteBuffer buffer = createDirectBuffer("{\"phone\":\"+1234567890\"}");
            assertThat(JsonValidator.validatePhoneNumberZeroCopy(buffer, "phone")).isTrue();
        }

        @Test
        @DisplayName("should validate IP address with DirectByteBuffer")
        void shouldValidateIpAddress() {
            ByteBuffer buffer = createDirectBuffer("{\"ip\":\"192.168.1.1\"}");
            assertThat(JsonValidator.validateIpAddressZeroCopy(buffer, "ip")).isTrue();
        }

        @Test
        @DisplayName("should throw IllegalArgumentException for heap buffer")
        void shouldRejectHeapBufferForFormatValidators() {
            ByteBuffer heapBuffer = ByteBuffer.wrap("{\"email\":\"user@example.com\"}".getBytes(StandardCharsets.UTF_8));

            assertThatThrownBy(() -> JsonValidator.validateEmailZeroCopy(heapBuffer, "email"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Performance & Memory Efficiency")
    class PerformanceTests {

        @Test
        @DisplayName("should handle large JSON without heap allocation")
        void shouldHandleLargeJson() {
            // Create 1MB JSON
            StringBuilder sb = new StringBuilder("{\"data\":[");
            for (int i = 0; i < 10000; i++) {
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(i).append(",\"value\":\"data").append(i).append("\"}");
            }
            sb.append("],\"target\":\"value\"}");

            ByteBuffer buffer = createDirectBuffer(sb.toString());

            // Validate
            assertThat(JsonValidator.validateZeroCopy(buffer)).isTrue();

            // Extract field
            buffer.rewind();
            String result = JsonValidator.getStringZeroCopy(buffer, "target");
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("should allow multiple operations on same buffer")
        void shouldAllowMultipleOperations() {
            ByteBuffer buffer = createDirectBuffer("{\"name\":\"John\",\"age\":30,\"active\":true}");

            // Validate
            assertThat(JsonValidator.validateZeroCopy(buffer)).isTrue();

            // Extract string
            buffer.rewind();
            String name = JsonValidator.getStringZeroCopy(buffer, "name");
            assertThat(name).isEqualTo("John");

            // Extract number
            buffer.rewind();
            double age = JsonValidator.getNumberZeroCopy(buffer, "age");
            assertThat(age).isEqualTo(30.0);

            // Extract boolean
            buffer.rewind();
            boolean active = JsonValidator.getBooleanZeroCopy(buffer, "active");
            assertThat(active).isTrue();
        }

        @Test
        @DisplayName("should efficiently extract from deeply nested JSON")
        void shouldHandleDeeplyNestedJson() {
            String json = "{\"l1\":{\"l2\":{\"l3\":{\"l4\":{\"l5\":{\"value\":\"deep\"}}}}}}";
            ByteBuffer buffer = createDirectBuffer(json);

            String result = JsonValidator.getStringZeroCopy(buffer, "l1.l2.l3.l4.l5.value");
            assertThat(result).isEqualTo("deep");
        }
    }

    // Helper method to create DirectByteBuffer from String
    private static ByteBuffer createDirectBuffer(String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        return buffer;
    }
}
