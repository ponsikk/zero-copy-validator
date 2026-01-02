package io.zerocopy.json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JsonValidator} validator methods (field type, range, format).
 */
@DisplayName("JsonValidator - Validators")
class ValidatorsTest {

    @Nested
    @DisplayName("Field Type Validation")
    class FieldTypeTests {

        @Test
        @DisplayName("should validate STRING type")
        void shouldValidateStringType() {
            String json = "{\"name\":\"John\"}";
            assertThat(JsonValidator.validateFieldType(json, "name", JsonType.STRING)).isTrue();
            assertThat(JsonValidator.validateFieldType(json, "name", JsonType.NUMBER)).isFalse();
        }

        @Test
        @DisplayName("should validate NUMBER type")
        void shouldValidateNumberType() {
            String json = "{\"age\":30}";
            assertThat(JsonValidator.validateFieldType(json, "age", JsonType.NUMBER)).isTrue();
            assertThat(JsonValidator.validateFieldType(json, "age", JsonType.STRING)).isFalse();
        }

        @Test
        @DisplayName("should validate BOOLEAN type")
        void shouldValidateBooleanType() {
            String json = "{\"active\":true}";
            assertThat(JsonValidator.validateFieldType(json, "active", JsonType.BOOLEAN)).isTrue();
            assertThat(JsonValidator.validateFieldType(json, "active", JsonType.NUMBER)).isFalse();
        }

        @Test
        @DisplayName("should validate NULL type")
        void shouldValidateNullType() {
            String json = "{\"value\":null}";
            assertThat(JsonValidator.validateFieldType(json, "value", JsonType.NULL)).isTrue();
            assertThat(JsonValidator.validateFieldType(json, "value", JsonType.STRING)).isFalse();
        }

        @Test
        @DisplayName("should validate OBJECT type")
        void shouldValidateObjectType() {
            String json = "{\"user\":{\"name\":\"John\"}}";
            assertThat(JsonValidator.validateFieldType(json, "user", JsonType.OBJECT)).isTrue();
            assertThat(JsonValidator.validateFieldType(json, "user", JsonType.ARRAY)).isFalse();
        }

        @Test
        @DisplayName("should validate ARRAY type")
        void shouldValidateArrayType() {
            String json = "{\"items\":[1,2,3]}";
            assertThat(JsonValidator.validateFieldType(json, "items", JsonType.ARRAY)).isTrue();
            assertThat(JsonValidator.validateFieldType(json, "items", JsonType.OBJECT)).isFalse();
        }

        @Test
        @DisplayName("should validate nested field type")
        void shouldValidateNestedFieldType() {
            String json = "{\"user\":{\"age\":25}}";
            assertThat(JsonValidator.validateFieldType(json, "user.age", JsonType.NUMBER)).isTrue();
        }

        @Test
        @DisplayName("should return false for nonexistent field")
        void shouldReturnFalseForNonexistent() {
            String json = "{\"name\":\"John\"}";
            assertThat(JsonValidator.validateFieldType(json, "age", JsonType.NUMBER)).isFalse();
        }

        @Test
        @DisplayName("should validate type from byte array")
        void shouldValidateTypeFromBytes() {
            byte[] json = "{\"value\":42}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.validateFieldType(json, "value", JsonType.NUMBER)).isTrue();
        }
    }

    @Nested
    @DisplayName("Field Existence")
    class FieldExistsTests {

        @Test
        @DisplayName("should return true if field exists")
        void shouldReturnTrueIfExists() {
            String json = "{\"name\":\"John\"}";
            assertThat(JsonValidator.fieldExists(json, "name")).isTrue();
        }

        @Test
        @DisplayName("should return false if field does not exist")
        void shouldReturnFalseIfNotExists() {
            String json = "{\"name\":\"John\"}";
            assertThat(JsonValidator.fieldExists(json, "age")).isFalse();
        }

        @Test
        @DisplayName("should return true for nested field")
        void shouldReturnTrueForNested() {
            String json = "{\"user\":{\"email\":\"test@example.com\"}}";
            assertThat(JsonValidator.fieldExists(json, "user.email")).isTrue();
        }

        @Test
        @DisplayName("should return true for null field")
        void shouldReturnTrueForNull() {
            String json = "{\"value\":null}";
            assertThat(JsonValidator.fieldExists(json, "value")).isTrue();
        }

        @Test
        @DisplayName("should return true for array element")
        void shouldReturnTrueForArrayElement() {
            String json = "{\"items\":[1,2,3]}";
            assertThat(JsonValidator.fieldExists(json, "items.0")).isTrue();
            assertThat(JsonValidator.fieldExists(json, "items.5")).isFalse();
        }

        @Test
        @DisplayName("should check existence from byte array")
        void shouldCheckExistenceFromBytes() {
            byte[] json = "{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.fieldExists(json, "key")).isTrue();
        }
    }

    @Nested
    @DisplayName("Field Null Check")
    class FieldIsNullTests {

        @Test
        @DisplayName("should return true if field is null")
        void shouldReturnTrueIfNull() {
            String json = "{\"value\":null}";
            assertThat(JsonValidator.fieldIsNull(json, "value")).isTrue();
        }

        @Test
        @DisplayName("should return false if field is not null")
        void shouldReturnFalseIfNotNull() {
            String json = "{\"value\":\"something\"}";
            assertThat(JsonValidator.fieldIsNull(json, "value")).isFalse();
        }

        @Test
        @DisplayName("should return false if field does not exist")
        void shouldReturnFalseIfNotExists() {
            String json = "{\"name\":\"John\"}";
            assertThat(JsonValidator.fieldIsNull(json, "age")).isFalse();
        }

        @Test
        @DisplayName("should check null for nested field")
        void shouldCheckNullForNested() {
            String json = "{\"user\":{\"email\":null}}";
            assertThat(JsonValidator.fieldIsNull(json, "user.email")).isTrue();
        }

        @Test
        @DisplayName("should check null from byte array")
        void shouldCheckNullFromBytes() {
            byte[] json = "{\"data\":null}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.fieldIsNull(json, "data")).isTrue();
        }
    }

    @Nested
    @DisplayName("Range Validators - Number Range")
    class NumberRangeTests {

        @Test
        @DisplayName("should validate number within range")
        void shouldValidateNumberInRange() {
            String json = "{\"age\":25}";
            assertThat(JsonValidator.validateNumberRange(json, "age", 0, 120)).isTrue();
        }

        @Test
        @DisplayName("should reject number below min")
        void shouldRejectBelowMin() {
            String json = "{\"age\":-5}";
            assertThat(JsonValidator.validateNumberRange(json, "age", 0, 120)).isFalse();
        }

        @Test
        @DisplayName("should reject number above max")
        void shouldRejectAboveMax() {
            String json = "{\"age\":150}";
            assertThat(JsonValidator.validateNumberRange(json, "age", 0, 120)).isFalse();
        }

        @Test
        @DisplayName("should validate number at min boundary")
        void shouldValidateAtMinBoundary() {
            String json = "{\"value\":0}";
            assertThat(JsonValidator.validateNumberRange(json, "value", 0, 100)).isTrue();
        }

        @Test
        @DisplayName("should validate number at max boundary")
        void shouldValidateAtMaxBoundary() {
            String json = "{\"value\":100}";
            assertThat(JsonValidator.validateNumberRange(json, "value", 0, 100)).isTrue();
        }

        @Test
        @DisplayName("should validate floating point range")
        void shouldValidateFloatRange() {
            String json = "{\"temperature\":22.5}";
            assertThat(JsonValidator.validateNumberRange(json, "temperature", 20.0, 25.0)).isTrue();
        }

        @Test
        @DisplayName("should validate nested number range")
        void shouldValidateNestedRange() {
            String json = "{\"user\":{\"age\":30}}";
            assertThat(JsonValidator.validateNumberRange(json, "user.age", 18, 65)).isTrue();
        }

        @Test
        @DisplayName("should validate from byte array")
        void shouldValidateFromBytes() {
            byte[] json = "{\"score\":85}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.validateNumberRange(json, "score", 0, 100)).isTrue();
        }
    }

    @Nested
    @DisplayName("Range Validators - String Length")
    class StringLengthTests {

        @Test
        @DisplayName("should validate string length within range")
        void shouldValidateStringLengthInRange() {
            String json = "{\"password\":\"secret123\"}";
            assertThat(JsonValidator.validateStringLength(json, "password", 8, 64)).isTrue();
        }

        @Test
        @DisplayName("should reject string too short")
        void shouldRejectTooShort() {
            String json = "{\"password\":\"abc\"}";
            assertThat(JsonValidator.validateStringLength(json, "password", 8, 64)).isFalse();
        }

        @Test
        @DisplayName("should reject string too long")
        void shouldRejectTooLong() {
            String json = "{\"password\":\"" + "x".repeat(100) + "\"}";
            assertThat(JsonValidator.validateStringLength(json, "password", 8, 64)).isFalse();
        }

        @Test
        @DisplayName("should validate at min length boundary")
        void shouldValidateAtMinBoundary() {
            String json = "{\"code\":\"12345678\"}";
            assertThat(JsonValidator.validateStringLength(json, "code", 8, 16)).isTrue();
        }

        @Test
        @DisplayName("should validate at max length boundary")
        void shouldValidateAtMaxBoundary() {
            String json = "{\"code\":\"1234567890123456\"}";
            assertThat(JsonValidator.validateStringLength(json, "code", 8, 16)).isTrue();
        }

        @Test
        @DisplayName("should validate empty string if min is 0")
        void shouldValidateEmptyString() {
            String json = "{\"value\":\"\"}";
            assertThat(JsonValidator.validateStringLength(json, "value", 0, 100)).isTrue();
        }

        @Test
        @DisplayName("should count Unicode characters correctly")
        void shouldCountUnicode() {
            String json = "{\"message\":\"Hello🌍\"}";  // 6 characters
            assertThat(JsonValidator.validateStringLength(json, "message", 5, 10)).isTrue();
        }

        @Test
        @DisplayName("should validate nested string length")
        void shouldValidateNestedLength() {
            String json = "{\"user\":{\"name\":\"Alice\"}}";
            assertThat(JsonValidator.validateStringLength(json, "user.name", 3, 20)).isTrue();
        }

        @Test
        @DisplayName("should validate from byte array")
        void shouldValidateFromBytes() {
            byte[] json = "{\"username\":\"john_doe\"}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.validateStringLength(json, "username", 3, 20)).isTrue();
        }
    }

    @Nested
    @DisplayName("Range Validators - Array Size")
    class ArraySizeTests {

        @Test
        @DisplayName("should validate array size within range")
        void shouldValidateArraySizeInRange() {
            String json = "{\"items\":[1,2,3,4,5]}";
            assertThat(JsonValidator.validateArraySize(json, "items", 1, 10)).isTrue();
        }

        @Test
        @DisplayName("should reject array too small")
        void shouldRejectTooSmall() {
            String json = "{\"items\":[]}";
            assertThat(JsonValidator.validateArraySize(json, "items", 1, 10)).isFalse();
        }

        @Test
        @DisplayName("should reject array too large")
        void shouldRejectTooLarge() {
            String json = "{\"items\":[1,2,3,4,5,6,7,8,9,10,11]}";
            assertThat(JsonValidator.validateArraySize(json, "items", 1, 10)).isFalse();
        }

        @Test
        @DisplayName("should validate at min size boundary")
        void shouldValidateAtMinBoundary() {
            String json = "{\"items\":[1]}";
            assertThat(JsonValidator.validateArraySize(json, "items", 1, 10)).isTrue();
        }

        @Test
        @DisplayName("should validate at max size boundary")
        void shouldValidateAtMaxBoundary() {
            String json = "{\"items\":[1,2,3,4,5,6,7,8,9,10]}";
            assertThat(JsonValidator.validateArraySize(json, "items", 1, 10)).isTrue();
        }

        @Test
        @DisplayName("should validate empty array if min is 0")
        void shouldValidateEmptyArray() {
            String json = "{\"items\":[]}";
            assertThat(JsonValidator.validateArraySize(json, "items", 0, 10)).isTrue();
        }

        @Test
        @DisplayName("should validate nested array size")
        void shouldValidateNestedArraySize() {
            String json = "{\"data\":{\"values\":[1,2,3]}}";
            assertThat(JsonValidator.validateArraySize(json, "data.values", 1, 5)).isTrue();
        }

        @Test
        @DisplayName("should validate from byte array")
        void shouldValidateFromBytes() {
            byte[] json = "{\"tags\":[\"a\",\"b\",\"c\"]}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.validateArraySize(json, "tags", 1, 10)).isTrue();
        }
    }

    @Nested
    @DisplayName("Format Validators - Email")
    class EmailValidatorTests {

        @Test
        @DisplayName("should validate valid email")
        void shouldValidateValidEmail() {
            String json = "{\"email\":\"user@example.com\"}";
            assertThat(JsonValidator.validateEmail(json, "email")).isTrue();
        }

        @Test
        @DisplayName("should validate email with subdomain")
        void shouldValidateSubdomain() {
            String json = "{\"email\":\"user@mail.example.com\"}";
            assertThat(JsonValidator.validateEmail(json, "email")).isTrue();
        }

        @Test
        @DisplayName("should validate email with plus sign")
        void shouldValidatePlusSign() {
            String json = "{\"email\":\"user+tag@example.com\"}";
            assertThat(JsonValidator.validateEmail(json, "email")).isTrue();
        }

        @Test
        @DisplayName("should reject invalid email - no @")
        void shouldRejectNoAt() {
            String json = "{\"email\":\"userexample.com\"}";
            assertThat(JsonValidator.validateEmail(json, "email")).isFalse();
        }

        @Test
        @DisplayName("should reject invalid email - no domain")
        void shouldRejectNoDomain() {
            String json = "{\"email\":\"user@\"}";
            assertThat(JsonValidator.validateEmail(json, "email")).isFalse();
        }

        @Test
        @DisplayName("should validate nested email")
        void shouldValidateNestedEmail() {
            String json = "{\"user\":{\"email\":\"test@example.com\"}}";
            assertThat(JsonValidator.validateEmail(json, "user.email")).isTrue();
        }

        @Test
        @DisplayName("should validate from byte array")
        void shouldValidateFromBytes() {
            byte[] json = "{\"contact\":\"info@company.com\"}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.validateEmail(json, "contact")).isTrue();
        }
    }

    @Nested
    @DisplayName("Format Validators - URL")
    class UrlValidatorTests {

        @Test
        @DisplayName("should validate HTTP URL")
        void shouldValidateHttpUrl() {
            String json = "{\"url\":\"http://example.com\"}";
            assertThat(JsonValidator.validateUrl(json, "url")).isTrue();
        }

        @Test
        @DisplayName("should validate HTTPS URL")
        void shouldValidateHttpsUrl() {
            String json = "{\"url\":\"https://example.com/path\"}";
            assertThat(JsonValidator.validateUrl(json, "url")).isTrue();
        }

        @Test
        @DisplayName("should validate URL with query params")
        void shouldValidateQueryParams() {
            String json = "{\"url\":\"https://example.com/page?id=123&name=test\"}";
            assertThat(JsonValidator.validateUrl(json, "url")).isTrue();
        }

        @Test
        @DisplayName("should reject invalid URL - no protocol")
        void shouldRejectNoProtocol() {
            String json = "{\"url\":\"example.com\"}";
            assertThat(JsonValidator.validateUrl(json, "url")).isFalse();
        }

        @Test
        @DisplayName("should validate from byte array")
        void shouldValidateFromBytes() {
            byte[] json = "{\"website\":\"https://www.example.com\"}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.validateUrl(json, "website")).isTrue();
        }
    }

    @Nested
    @DisplayName("Format Validators - ISO Date")
    class IsoDateValidatorTests {

        @Test
        @DisplayName("should validate ISO date")
        void shouldValidateIsoDate() {
            String json = "{\"date\":\"2024-12-30\"}";
            assertThat(JsonValidator.validateIsoDate(json, "date")).isTrue();
        }

        @Test
        @DisplayName("should validate ISO datetime")
        void shouldValidateIsoDatetime() {
            String json = "{\"timestamp\":\"2024-12-30T10:30:00Z\"}";
            assertThat(JsonValidator.validateIsoDate(json, "timestamp")).isTrue();
        }

        @Test
        @DisplayName("should validate ISO datetime with timezone")
        void shouldValidateTimezone() {
            String json = "{\"timestamp\":\"2024-12-30T10:30:00+03:00\"}";
            assertThat(JsonValidator.validateIsoDate(json, "timestamp")).isTrue();
        }

        @Test
        @DisplayName("should reject invalid date format")
        void shouldRejectInvalidFormat() {
            String json = "{\"date\":\"12/30/2024\"}";
            assertThat(JsonValidator.validateIsoDate(json, "date")).isFalse();
        }

        @Test
        @DisplayName("should validate from byte array")
        void shouldValidateFromBytes() {
            byte[] json = "{\"created\":\"2024-01-01T00:00:00Z\"}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.validateIsoDate(json, "created")).isTrue();
        }
    }

    @Nested
    @DisplayName("Format Validators - UUID")
    class UuidValidatorTests {

        @Test
        @DisplayName("should validate UUID with hyphens")
        void shouldValidateUuidWithHyphens() {
            String json = "{\"id\":\"550e8400-e29b-41d4-a716-446655440000\"}";
            assertThat(JsonValidator.validateUuid(json, "id")).isTrue();
        }

        @Test
        @DisplayName("should validate UUID without hyphens")
        void shouldValidateUuidWithoutHyphens() {
            String json = "{\"id\":\"550e8400e29b41d4a716446655440000\"}";
            assertThat(JsonValidator.validateUuid(json, "id")).isTrue();
        }

        @Test
        @DisplayName("should reject invalid UUID - wrong length")
        void shouldRejectWrongLength() {
            String json = "{\"id\":\"550e8400-e29b-41d4\"}";
            assertThat(JsonValidator.validateUuid(json, "id")).isFalse();
        }

        @Test
        @DisplayName("should reject invalid UUID - invalid characters")
        void shouldRejectInvalidChars() {
            String json = "{\"id\":\"550e8400-e29b-41d4-a716-44665544000g\"}";
            assertThat(JsonValidator.validateUuid(json, "id")).isFalse();
        }

        @Test
        @DisplayName("should validate from byte array")
        void shouldValidateFromBytes() {
            byte[] json = "{\"uuid\":\"123e4567-e89b-12d3-a456-426614174000\"}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.validateUuid(json, "uuid")).isTrue();
        }
    }

    @Nested
    @DisplayName("Format Validators - Phone Number")
    class PhoneNumberValidatorTests {

        @Test
        @DisplayName("should validate E.164 phone number")
        void shouldValidateE164() {
            String json = "{\"phone\":\"+1234567890\"}";
            assertThat(JsonValidator.validatePhoneNumber(json, "phone")).isTrue();
        }

        @Test
        @DisplayName("should validate phone with hyphens")
        void shouldValidateWithHyphens() {
            String json = "{\"phone\":\"+1-234-567-8900\"}";
            assertThat(JsonValidator.validatePhoneNumber(json, "phone")).isTrue();
        }

        @Test
        @DisplayName("should validate phone with spaces")
        void shouldValidateWithSpaces() {
            String json = "{\"phone\":\"+1 234 567 8900\"}";
            assertThat(JsonValidator.validatePhoneNumber(json, "phone")).isTrue();
        }

        @Test
        @DisplayName("should reject phone without plus")
        void shouldRejectWithoutPlus() {
            String json = "{\"phone\":\"1234567890\"}";
            assertThat(JsonValidator.validatePhoneNumber(json, "phone")).isFalse();
        }

        @Test
        @DisplayName("should validate from byte array")
        void shouldValidateFromBytes() {
            byte[] json = "{\"contact\":\"+79991234567\"}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.validatePhoneNumber(json, "contact")).isTrue();
        }
    }

    @Nested
    @DisplayName("Format Validators - IP Address")
    class IpAddressValidatorTests {

        @Test
        @DisplayName("should validate IPv4 address")
        void shouldValidateIpv4() {
            String json = "{\"ip\":\"192.168.1.1\"}";
            assertThat(JsonValidator.validateIpAddress(json, "ip")).isTrue();
        }

        @Test
        @DisplayName("should validate IPv6 address")
        void shouldValidateIpv6() {
            String json = "{\"ip\":\"2001:0db8:85a3::8a2e:0370:7334\"}";
            assertThat(JsonValidator.validateIpAddress(json, "ip")).isTrue();
        }

        @Test
        @DisplayName("should validate IPv6 short form")
        void shouldValidateIpv6Short() {
            String json = "{\"ip\":\"::1\"}";
            assertThat(JsonValidator.validateIpAddress(json, "ip")).isTrue();
        }

        @Test
        @DisplayName("should reject invalid IPv4 - out of range")
        void shouldRejectInvalidIpv4() {
            String json = "{\"ip\":\"256.1.1.1\"}";
            assertThat(JsonValidator.validateIpAddress(json, "ip")).isFalse();
        }

        @Test
        @DisplayName("should validate from byte array")
        void shouldValidateFromBytes() {
            byte[] json = "{\"server\":\"10.0.0.1\"}".getBytes(StandardCharsets.UTF_8);
            assertThat(JsonValidator.validateIpAddress(json, "server")).isTrue();
        }
    }
}
