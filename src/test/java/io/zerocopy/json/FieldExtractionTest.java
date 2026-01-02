package io.zerocopy.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JsonValidator} field extraction functionality.
 */
@DisplayName("JsonValidator - Field Extraction")
class FieldExtractionTest {

    @Nested
    @DisplayName("getString(String, String) - String extraction")
    class GetStringTests {

        @Test
        @DisplayName("should extract simple string field")
        void shouldExtractSimpleString() {
            String json = "{\"name\":\"John\"}";
            String result = JsonValidator.getString(json, "name");
            assertThat(result).isEqualTo("John");
        }

        @Test
        @DisplayName("should extract nested string field")
        void shouldExtractNestedString() {
            String json = "{\"user\":{\"name\":\"Alice\"}}";
            String result = JsonValidator.getString(json, "user.name");
            assertThat(result).isEqualTo("Alice");
        }

        @Test
        @DisplayName("should extract deeply nested string")
        void shouldExtractDeeplyNestedString() {
            String json = "{\"a\":{\"b\":{\"c\":{\"value\":\"deep\"}}}}";
            String result = JsonValidator.getString(json, "a.b.c.value");
            assertThat(result).isEqualTo("deep");
        }

        @Test
        @DisplayName("should extract string from array by index")
        void shouldExtractFromArray() {
            String json = "{\"items\":[\"first\",\"second\",\"third\"]}";
            String result = JsonValidator.getString(json, "items.0");
            assertThat(result).isEqualTo("first");
        }

        @Test
        @DisplayName("should extract nested field from array element")
        void shouldExtractNestedFromArray() {
            String json = "{\"users\":[{\"name\":\"Alice\"},{\"name\":\"Bob\"}]}";
            String result = JsonValidator.getString(json, "users.1.name");
            assertThat(result).isEqualTo("Bob");
        }

        @Test
        @DisplayName("should extract string with special characters")
        void shouldExtractSpecialChars() {
            String json = "{\"message\":\"Hello\\nWorld\\t!\"}";
            String result = JsonValidator.getString(json, "message");
            assertThat(result).isEqualTo("Hello\nWorld\t!");
        }

        @Test
        @DisplayName("should extract Unicode string")
        void shouldExtractUnicode() {
            String json = "{\"message\":\"Привет мир 🌍\"}";
            String result = JsonValidator.getString(json, "message");
            assertThat(result).isEqualTo("Привет мир 🌍");
        }

        @Test
        @DisplayName("should extract empty string")
        void shouldExtractEmptyString() {
            String json = "{\"value\":\"\"}";
            String result = JsonValidator.getString(json, "value");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should support JSONPath style with $.")
        void shouldSupportJsonPathStyle() {
            String json = "{\"user\":{\"name\":\"Test\"}}";
            String result = JsonValidator.getString(json, "$.user.name");
            assertThat(result).isEqualTo("Test");
        }

        @Test
        @DisplayName("should throw RuntimeException for path not found")
        void shouldThrowOnPathNotFound() {
            String json = "{\"name\":\"John\"}";
            assertThatThrownBy(() -> JsonValidator.getString(json, "nonexistent"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should throw RuntimeException for type mismatch")
        void shouldThrowOnTypeMismatch() {
            String json = "{\"age\":30}";
            assertThatThrownBy(() -> JsonValidator.getString(json, "age"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException for null JSON")
        void shouldThrowOnNullJson() {
            assertThatThrownBy(() -> JsonValidator.getString((String) null, "path"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException for null path")
        void shouldThrowOnNullPath() {
            assertThatThrownBy(() -> JsonValidator.getString("{}", null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getString(byte[], String) - Byte array string extraction")
    class GetStringBytesTests {

        @Test
        @DisplayName("should extract string from byte array")
        void shouldExtractStringFromBytes() {
            byte[] json = "{\"name\":\"John\"}".getBytes(StandardCharsets.UTF_8);
            String result = JsonValidator.getString(json, "name");
            assertThat(result).isEqualTo("John");
        }

        @Test
        @DisplayName("should extract nested string from bytes")
        void shouldExtractNestedFromBytes() {
            byte[] json = "{\"user\":{\"email\":\"test@example.com\"}}".getBytes(StandardCharsets.UTF_8);
            String result = JsonValidator.getString(json, "user.email");
            assertThat(result).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("should throw NullPointerException for null bytes")
        void shouldThrowOnNullBytes() {
            assertThatThrownBy(() -> JsonValidator.getString((byte[]) null, "path"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getNumber(String, String) - Number extraction")
    class GetNumberTests {

        @Test
        @DisplayName("should extract integer as double")
        void shouldExtractInteger() {
            String json = "{\"age\":30}";
            double result = JsonValidator.getNumber(json, "age");
            assertThat(result).isEqualTo(30.0);
        }

        @Test
        @DisplayName("should extract floating point number")
        void shouldExtractFloat() {
            String json = "{\"price\":19.99}";
            double result = JsonValidator.getNumber(json, "price");
            assertThat(result).isEqualTo(19.99);
        }

        @Test
        @DisplayName("should extract negative number")
        void shouldExtractNegative() {
            String json = "{\"temperature\":-5.5}";
            double result = JsonValidator.getNumber(json, "temperature");
            assertThat(result).isEqualTo(-5.5);
        }

        @Test
        @DisplayName("should extract zero")
        void shouldExtractZero() {
            String json = "{\"value\":0}";
            double result = JsonValidator.getNumber(json, "value");
            assertThat(result).isEqualTo(0.0);
        }

        @Test
        @DisplayName("should extract nested number")
        void shouldExtractNestedNumber() {
            String json = "{\"user\":{\"age\":25}}";
            double result = JsonValidator.getNumber(json, "user.age");
            assertThat(result).isEqualTo(25.0);
        }

        @Test
        @DisplayName("should extract number from array")
        void shouldExtractFromArray() {
            String json = "{\"scores\":[95,87,92]}";
            double result = JsonValidator.getNumber(json, "scores.1");
            assertThat(result).isEqualTo(87.0);
        }

        @Test
        @DisplayName("should extract scientific notation")
        void shouldExtractScientificNotation() {
            String json = "{\"value\":1.23e-10}";
            double result = JsonValidator.getNumber(json, "value");
            assertThat(result).isCloseTo(1.23e-10, within(1e-20));
        }

        @Test
        @DisplayName("should extract large number")
        void shouldExtractLargeNumber() {
            String json = "{\"value\":9007199254740991}";  // MAX_SAFE_INTEGER
            double result = JsonValidator.getNumber(json, "value");
            assertThat(result).isEqualTo(9007199254740991.0);
        }

        @Test
        @DisplayName("should throw RuntimeException for path not found")
        void shouldThrowOnPathNotFound() {
            String json = "{\"age\":30}";
            assertThatThrownBy(() -> JsonValidator.getNumber(json, "nonexistent"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should throw RuntimeException for type mismatch")
        void shouldThrowOnTypeMismatch() {
            String json = "{\"name\":\"John\"}";
            assertThatThrownBy(() -> JsonValidator.getNumber(json, "name"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException for null JSON")
        void shouldThrowOnNullJson() {
            assertThatThrownBy(() -> JsonValidator.getNumber((String) null, "path"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getNumber(byte[], String) - Byte array number extraction")
    class GetNumberBytesTests {

        @Test
        @DisplayName("should extract number from byte array")
        void shouldExtractNumberFromBytes() {
            byte[] json = "{\"age\":42}".getBytes(StandardCharsets.UTF_8);
            double result = JsonValidator.getNumber(json, "age");
            assertThat(result).isEqualTo(42.0);
        }

        @Test
        @DisplayName("should extract nested number from bytes")
        void shouldExtractNestedFromBytes() {
            byte[] json = "{\"data\":{\"value\":99.5}}".getBytes(StandardCharsets.UTF_8);
            double result = JsonValidator.getNumber(json, "data.value");
            assertThat(result).isEqualTo(99.5);
        }

        @Test
        @DisplayName("should throw NullPointerException for null bytes")
        void shouldThrowOnNullBytes() {
            assertThatThrownBy(() -> JsonValidator.getNumber((byte[]) null, "path"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getBoolean(String, String) - Boolean extraction")
    class GetBooleanTests {

        @Test
        @DisplayName("should extract true value")
        void shouldExtractTrue() {
            String json = "{\"active\":true}";
            boolean result = JsonValidator.getBoolean(json, "active");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should extract false value")
        void shouldExtractFalse() {
            String json = "{\"active\":false}";
            boolean result = JsonValidator.getBoolean(json, "active");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should extract nested boolean")
        void shouldExtractNestedBoolean() {
            String json = "{\"user\":{\"verified\":true}}";
            boolean result = JsonValidator.getBoolean(json, "user.verified");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should extract boolean from array")
        void shouldExtractFromArray() {
            String json = "{\"flags\":[true,false,true]}";
            boolean result = JsonValidator.getBoolean(json, "flags.1");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should throw RuntimeException for path not found")
        void shouldThrowOnPathNotFound() {
            String json = "{\"active\":true}";
            assertThatThrownBy(() -> JsonValidator.getBoolean(json, "nonexistent"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should throw RuntimeException for type mismatch")
        void shouldThrowOnTypeMismatch() {
            String json = "{\"name\":\"John\"}";
            assertThatThrownBy(() -> JsonValidator.getBoolean(json, "name"))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("should throw NullPointerException for null JSON")
        void shouldThrowOnNullJson() {
            assertThatThrownBy(() -> JsonValidator.getBoolean((String) null, "path"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getBoolean(byte[], String) - Byte array boolean extraction")
    class GetBooleanBytesTests {

        @Test
        @DisplayName("should extract boolean from byte array")
        void shouldExtractBooleanFromBytes() {
            byte[] json = "{\"enabled\":true}".getBytes(StandardCharsets.UTF_8);
            boolean result = JsonValidator.getBoolean(json, "enabled");
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should extract nested boolean from bytes")
        void shouldExtractNestedFromBytes() {
            byte[] json = "{\"settings\":{\"debug\":false}}".getBytes(StandardCharsets.UTF_8);
            boolean result = JsonValidator.getBoolean(json, "settings.debug");
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("should throw NullPointerException for null bytes")
        void shouldThrowOnNullBytes() {
            assertThatThrownBy(() -> JsonValidator.getBoolean((byte[]) null, "path"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Performance & Edge Cases")
    class PerformanceTests {

        @Test
        @DisplayName("should extract field from large JSON efficiently")
        void shouldHandleLargeJson() {
            StringBuilder sb = new StringBuilder("{\"target\":\"value\",\"data\":[");
            for (int i = 0; i < 1000; i++) {
                if (i > 0) sb.append(",");
                sb.append("{\"id\":").append(i).append(",\"name\":\"item").append(i).append("\"}");
            }
            sb.append("]}");

            String json = sb.toString();
            String result = JsonValidator.getString(json, "target");
            assertThat(result).isEqualTo("value");
        }

        @Test
        @DisplayName("should extract long string value")
        void shouldExtractLongString() {
            String longValue = "x".repeat(10000);
            String json = "{\"data\":\"" + longValue + "\"}";
            String result = JsonValidator.getString(json, "data");
            assertThat(result).hasSize(10000);
        }

        @Test
        @DisplayName("should extract from deeply nested structure")
        void shouldHandleDeeplyNested() {
            String json = "{\"l1\":{\"l2\":{\"l3\":{\"l4\":{\"l5\":{\"value\":\"deep\"}}}}}}";
            String result = JsonValidator.getString(json, "l1.l2.l3.l4.l5.value");
            assertThat(result).isEqualTo("deep");
        }
    }
}
