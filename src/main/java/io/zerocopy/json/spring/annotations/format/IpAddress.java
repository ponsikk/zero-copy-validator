package io.zerocopy.json.spring.annotations.format;

import java.lang.annotation.*;

/**
 * Validates that a field contains a valid IP address (IPv4 or IPv6).
 *
 * <p>Supports both IPv4 and IPv6 formats.
 *
 * <p>Usage example:
 * <pre>{@code
 * @JsonSchema
 * public class RequestSchema {
 *     @Required
 *     @IpAddress
 *     @JsonPath("clientIp")
 *     private String clientIp;
 * }
 * }</pre>
 *
 * <p>Valid examples:
 * <ul>
 *   <li>192.168.1.1 (IPv4)</li>
 *   <li>10.0.0.1 (IPv4)</li>
 *   <li>2001:0db8:85a3::8a2e:0370:7334 (IPv6)</li>
 *   <li>::1 (IPv6 loopback)</li>
 * </ul>
 *
 * @since 0.3.0 (Phase 3: Spring Boot Integration)
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IpAddress {

    /**
     * Custom error message when validation fails.
     *
     * @return error message (defaults to "Field '{path}' must be a valid IP address")
     */
    String message() default "";
}
