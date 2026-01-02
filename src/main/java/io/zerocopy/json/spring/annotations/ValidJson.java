package io.zerocopy.json.spring.annotations;

import java.lang.annotation.*;

/**
 * Validates JSON request body using zero-copy validation before Jackson parsing.
 *
 * <p>Usage example:
 * <pre>{@code
 * @RestController
 * public class UserController {
 *     @PostMapping("/users")
 *     public ResponseEntity<User> createUser(
 *         @ValidJson(schema = UserSchema.class) String jsonBody
 *     ) {
 *         // JSON is already validated via zero-copy!
 *         return ok(userService.create(jsonBody));
 *     }
 * }
 * }</pre>
 *
 * <p>Performance: Validates JSON in 1-3 microseconds using Rust+simd-json,
 * 10-20x faster than Jackson parsing.
 *
 * @see JsonSchema
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidJson {

    /**
     * The schema class to validate against.
     * Must be annotated with {@link JsonSchema}.
     *
     * @return schema class
     */
    Class<?> schema();

    /**
     * Whether to fail fast on first validation error.
     *
     * @return true to stop at first error, false to collect all errors
     */
    boolean failFast() default true;

    /**
     * Custom error message when validation fails.
     *
     * @return error message template
     */
    String message() default "JSON validation failed";
}
