package io.zerocopy.json.spring.annotations.range;

import java.lang.annotation.*;

/**
 * Validates that a numeric field is within a specified range [min, max].
 *
 * <p>Applicable to integer and floating-point number fields.
 *
 * <p>Usage example:
 * <pre>{@code
 * @JsonSchema
 * public class UserSchema {
 *     @Required
 *     @Range(min = 18, max = 120)
 *     @JsonPath("age")
 *     private Integer age;
 *
 *     @Optional
 *     @Range(min = 0.0, max = 100.0)
 *     @JsonPath("score")
 *     private Double score;
 * }
 * }</pre>
 *
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Range {

    /**
     * Minimum value (inclusive).
     *
     * @return minimum value
     */
    double min() default Double.MIN_VALUE;

    /**
     * Maximum value (inclusive).
     *
     * @return maximum value
     */
    double max() default Double.MAX_VALUE;

    /**
     * Custom error message when validation fails.
     *
     * @return error message (defaults to "Field '{path}' must be between {min} and {max}")
     */
    String message() default "";
}
