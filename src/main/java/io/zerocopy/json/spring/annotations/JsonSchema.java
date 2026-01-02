package io.zerocopy.json.spring.annotations;

import java.lang.annotation.*;

/**
 * Marks a class as a JSON validation schema.
 *
 * <p>Usage example:
 * <pre>{@code
 * @JsonSchema
 * public class UserSchema {
 *     @Required
 *     @Email
 *     @JsonPath("email")
 *     private String email;
 *
 *     @Required
 *     @Range(min = 18, max = 120)
 *     @JsonPath("age")
 *     private Integer age;
 *
 *     @Optional
 *     @Url
 *     @JsonPath("website")
 *     private String website;
 * }
 * }</pre>
 *
 * <p>Schema classes are processed at runtime via reflection to build
 * validation rules. Fields must be annotated with {@link JsonPath} to
 * specify the path in the JSON document.
 *
 * @see ValidJson
 * @see JsonPath
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JsonSchema {

    /**
     * Schema name for debugging and error messages.
     *
     * @return schema name (defaults to class simple name)
     */
    String value() default "";

    /**
     * Whether this schema allows additional fields not defined in the schema.
     *
     * @return true to allow extra fields, false to reject them
     */
    boolean allowAdditionalFields() default true;
}
