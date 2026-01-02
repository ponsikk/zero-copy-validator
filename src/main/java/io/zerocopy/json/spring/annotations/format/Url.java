package io.zerocopy.json.spring.annotations.format;

import java.lang.annotation.*;

/**
 * Validates that a field contains a valid URL (HTTP/HTTPS only).
 *
 * <p>Only HTTP and HTTPS schemes are supported. Other protocols will fail validation.
 *
 * <p>Usage example:
 * <pre>{@code
 * @JsonSchema
 * public class UserSchema {
 *     @Optional
 *     @Url
 *     @JsonPath("website")
 *     private String website;
 * }
 * }</pre>
 *
 * <p>Valid examples:
 * <ul>
 *   <li>https://example.com</li>
 *   <li>http://localhost:8080</li>
 *   <li>https://api.example.com/v1/users</li>
 * </ul>
 *
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Url {

    /**
     * Custom error message when validation fails.
     *
     * @return error message (defaults to "Field '{path}' must be a valid URL")
     */
    String message() default "";
}
