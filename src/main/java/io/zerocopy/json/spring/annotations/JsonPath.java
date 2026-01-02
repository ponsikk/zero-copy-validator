package io.zerocopy.json.spring.annotations;

import java.lang.annotation.*;

/**
 * Specifies the path to a field in the JSON document.
 *
 * <p>Supports various path formats:
 * <ul>
 *   <li>{@code "name"} - simple field</li>
 *   <li>{@code "user.email"} - nested field</li>
 *   <li>{@code "items.0.id"} - array element by index</li>
 *   <li>{@code "$.user.name"} - JSONPath style ($ is optional)</li>
 * </ul>
 *
 * <p>Usage example:
 * <pre>{@code
 * @JsonSchema
 * public class UserSchema {
 *     @Required
 *     @JsonPath("user.email")
 *     private String email;
 *
 *     @Optional
 *     @JsonPath("user.profile.bio")
 *     private String bio;
 * }
 * }</pre>
 *
 * @see JsonSchema
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonPath {

    /**
     * The path to the field in the JSON document.
     *
     * @return JSON path (e.g., "user.email", "items.0.id")
     */
    String value();
}
