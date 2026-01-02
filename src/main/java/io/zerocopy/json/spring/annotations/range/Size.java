package io.zerocopy.json.spring.annotations.range;

import java.lang.annotation.*;

/**
 * Validates that an array field's size (number of items) is within a specified range [min, max].
 *
 * <p>Usage example:
 * <pre>{@code
 * @JsonSchema
 * public class UserSchema {
 *     @Required
 *     @Size(min = 1, max = 10)
 *     @JsonPath("tags")
 *     private String[] tags;
 *
 *     @Optional
 *     @Size(min = 0, max = 100)
 *     @JsonPath("permissions")
 *     private String[] permissions;
 * }
 * }</pre>
 *
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Size {

    /**
     * Minimum number of items (inclusive).
     *
     * @return minimum size
     */
    long min() default 0;

    /**
     * Maximum number of items (inclusive).
     *
     * @return maximum size
     */
    long max() default Long.MAX_VALUE;

    /**
     * Custom error message when validation fails.
     *
     * @return error message (defaults to "Field '{path}' size must be between {min} and {max}")
     */
    String message() default "";
}
