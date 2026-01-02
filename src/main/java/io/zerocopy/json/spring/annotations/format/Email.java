package io.zerocopy.json.spring.annotations.format;

import java.lang.annotation.*;

/**
 * Validates that a field contains a valid email address (RFC 5322 simplified).
 *
 * <p>Uses Rust regex validation for high performance.
 *
 * <p>Usage example:
 * <pre>{@code
 * @JsonSchema
 * public class UserSchema {
 *     @Required
 *     @Email
 *     @JsonPath("email")
 *     private String email;
 * }
 * }</pre>
 *
 * <p>Valid examples:
 * <ul>
 *   <li>user@example.com</li>
 *   <li>john.doe@company.co.uk</li>
 *   <li>test+tag@gmail.com</li>
 * </ul>
 *
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Email {

    /**
     * Custom error message when validation fails.
     *
     * @return error message (defaults to "Field '{path}' must be a valid email")
     */
    String message() default "";
}
