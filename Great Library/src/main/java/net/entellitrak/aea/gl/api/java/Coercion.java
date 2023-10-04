package net.entellitrak.aea.gl.api.java;

/**
 * Utility class for converting objects from one type to another.
 *
 * @author Zachary.Miller
 */
public final class Coercion {

    private static final int PARSE_LONG_BASE = 10;

    /**
     * Utility classes do not need public constructors.
     */
    private Coercion() {
    }

    /**
     * Convert an object to a Long.
     * Supported types:
     * <ul>
     *  <li>null</li>
     *  <li>{@link Number}</li>
     *  <li>{@link String}</li>
     * </ul>
     *
     * @param object the object
     * @return the long
     */
    public static Long convertToLong(final Object object) {
        if(object == null) {
            return null;
        } else if(object instanceof Number) {
            return ((Number) object).longValue();
        } else if(object instanceof String) {
            return Long.parseLong((String) object, PARSE_LONG_BASE);
        } else {
            throw new GeneralRuntimeException(
                    String.format("Do not know how to convert object to a long: %s",
                            object));
        }
    }
}
