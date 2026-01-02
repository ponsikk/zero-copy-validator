package io.zerocopy.json.spring.annotations;

import java.lang.annotation.*;

/**
 * Marks a field as required in the JSON document.
 *
 * <p>The field must exist and not be null. If the field is missing or null,
 * validation will fail.
 *
 * <p>Usage example:
 * <pre>{@code
 * @JsonSchema
 * public class UserSchema {
 *     @Required
 *     @Email
 *     @JsonPath("email")
 *     private String email;  // Must exist and be non-null
 * }
 * }</pre>
 *
 * @see Optional
 * @see JsonPath
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Required {

    /**
     * Custom error message when field is missing or null.
     *
     * @return error message (defaults to "Field '{path}' is required")
     */
    String message() default "";
}
