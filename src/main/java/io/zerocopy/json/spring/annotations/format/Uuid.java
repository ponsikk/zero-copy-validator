package io.zerocopy.json.spring.annotations.format;

import java.lang.annotation.*;

/**
 * Validates that a field contains a valid UUID (RFC 4122).
 *
 * <p>Supports all UUID formats (with or without hyphens).
 *
 * <p>Usage example:
 * <pre>{@code
 * @JsonSchema
 * public class RequestSchema {
 *     @Required
 *     @Uuid
 *     @JsonPath("requestId")
 *     private String requestId;
 *
 *     @Optional
 *     @Uuid
 *     @JsonPath("userId")
 *     private String userId;
 * }
 * }</pre>
 *
 * <p>Valid examples:
 * <ul>
 *   <li>550e8400-e29b-41d4-a716-446655440000 (with hyphens)</li>
 *   <li>550e8400e29b41d4a716446655440000 (without hyphens)</li>
 * </ul>
 *
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Uuid {

    /**
     * Custom error message when validation fails.
     *
     * @return error message (defaults to "Field '{path}' must be a valid UUID")
     */
    String message() default "";
}
