package io.zerocopy.json.spring.annotations.format;

import java.lang.annotation.*;

/**
 * Validates that a field contains a valid phone number (E.164 international format).
 *
 * <p>Supports various formats with optional separators.
 *
 * <p>Usage example:
 * <pre>{@code
 * @JsonSchema
 * public class UserSchema {
 *     @Optional
 *     @PhoneNumber
 *     @JsonPath("phone")
 *     private String phone;
 * }
 * }</pre>
 *
 * <p>Valid examples:
 * <ul>
 *   <li>+1234567890</li>
 *   <li>+1-234-567-8900</li>
 *   <li>+1 (234) 567-8900</li>
 *   <li>+44-20-7946-0958</li>
 * </ul>
 *
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PhoneNumber {

    /**
     * Custom error message when validation fails.
     *
     * @return error message (defaults to "Field '{path}' must be a valid phone number")
     */
    String message() default "";
}
