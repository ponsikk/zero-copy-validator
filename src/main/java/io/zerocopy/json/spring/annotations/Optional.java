package io.zerocopy.json.spring.annotations;

import java.lang.annotation.*;

/**
 * Marks a field as optional in the JSON document.
 *
 * <p>The field may be missing or null. If present, it will still be validated
 * according to other annotations (e.g., {@code @Email}, {@code @Range}).
 *
 * <p>Usage example:
 * <pre>{@code
 * @JsonSchema
 * public class UserSchema {
 *     @Optional
 *     @Url
 *     @JsonPath("website")
 *     private String website;  // May be missing or null
 *
 *     @Optional
 *     @PhoneNumber
 *     @JsonPath("phone")
 *     private String phone;  // If present, must be valid phone number
 * }
 * }</pre>
 *
 * <p><b>Note:</b> If neither {@code @Required} nor {@code @Optional} is specified,
 * the field is treated as optional by default.
 *
 * @see Required
 * @see JsonPath
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Optional {
}
