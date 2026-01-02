package io.zerocopy.json.spring.annotations.range;

import java.lang.annotation.*;

/**
 * Validates that a string field's length is within a specified range [min, max].
 *
 * <p>Counts UTF-8 characters, not bytes.
 *
 * <p>Usage example:
 * <pre>{@code
 * @JsonSchema
 * public class UserSchema {
 *     @Required
 *     @Length(min = 1, max = 100)
 *     @JsonPath("name")
 *     private String name;
 *
 *     @Required
 *     @Length(min = 8, max = 64)
 *     @JsonPath("password")
 *     private String password;
 *
 *     @Optional
 *     @Length(min = 0, max = 500)
 *     @JsonPath("bio")
 *     private String bio;
 * }
 * }</pre>
 *
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Length {

    /**
     * Minimum length (inclusive).
     *
     * @return minimum length
     */
    long min() default 0;

    /**
     * Maximum length (inclusive).
     *
     * @return maximum length
     */
    long max() default Long.MAX_VALUE;

    /**
     * Custom error message when validation fails.
     *
     * @return error message (defaults to "Field '{path}' length must be between {min} and {max}")
     */
    String message() default "";
}
