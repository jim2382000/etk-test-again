package net.entellitrak.aea.gl.api.java;

/**
 * A general {@link RuntimeException} class which is primarily useful if you want
 * to throw a {@link RuntimeException} but do not want to have tools like SonarQube
 * flag the use of {@link RuntimeException}.
 *
 * @author Zachary.Miller
 */
public class GeneralRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Simple constructor.
     *
     * @param message the message
     */
    public GeneralRuntimeException(final String message) {
        super(message);
    }

    /**
     * Simple constructor.
     *
     * @param message the message
     * @param cause the cause
     */
    public GeneralRuntimeException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Simple constructor.
     *
     * @param cause the cause
     */
    public GeneralRuntimeException(final Throwable cause) {
        super(cause);
    }
}
