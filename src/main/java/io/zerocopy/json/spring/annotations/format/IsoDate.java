package io.zerocopy.json.spring.annotations.format;

import java.lang.annotation.*;

/**
 * Validates that a field contains a valid ISO 8601 date or datetime.
 *
 * <p>Supports both full datetime and date-only formats.
 *
 * <p>Usage example:
 * <pre>{@code
 * @JsonSchema
 * public class UserSchema {
 *     @Required
 *     @IsoDate
 *     @JsonPath("createdAt")
 *     private String createdAt;
 *
 *     @Optional
 *     @IsoDate
 *     @JsonPath("birthDate")
 *     private String birthDate;
 * }
 * }</pre>
 *
 * <p>Valid examples:
 * <ul>
 *   <li>2024-12-30T10:30:00Z (full datetime with timezone)</li>
 *   <li>2024-12-30T10:30:00 (datetime without timezone)</li>
 *   <li>2024-12-30 (date only)</li>
 * </ul>
 *
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IsoDate {

    /**
     * Custom error message when validation fails.
     *
     * @return error message (defaults to "Field '{path}' must be a valid ISO 8601 date")
     */
    String message() default "";
}
