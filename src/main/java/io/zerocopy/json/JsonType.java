package io.zerocopy.json;

/**
 * JSON value types.
 *
 * <p>Represents the different types of values that can exist in JSON.
 *
 * @since 0.2.0
 */
public enum JsonType {
    /** JSON null value */
    NULL(0),

    /** JSON boolean (true/false) */
    BOOLEAN(1),

    /** JSON number (integer or decimal) */
    NUMBER(2),

    /** JSON string */
    STRING(3),

    /** JSON array */
    ARRAY(4),

    /** JSON object */
    OBJECT(5);

    private final int code;

    JsonType(int code) {
        this.code = code;
    }

    /**
     * Gets the numeric code for this type.
     *
     * @return type code
     */
    public int getCode() {
        return code;
    }

    /**
     * Converts numeric code to JsonType enum.
     *
     * @param code the numeric type code
     * @return corresponding JsonType
     * @throws IllegalArgumentException if code is unknown
     */
    public static JsonType fromCode(int code) {
        for (JsonType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown JsonType code: " + code);
    }
}
