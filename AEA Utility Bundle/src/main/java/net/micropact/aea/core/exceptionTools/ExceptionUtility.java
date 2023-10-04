package net.micropact.aea.core.exceptionTools;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.wrappedAPIs.ExceptionUtilsWrapper;

/**
 * This class contains utility methods for dealing with {@link Exception} objects.
 *
 * @author zachary.miller
 */
public final class ExceptionUtility {

    /**
     * Utility methods do not need constructors.
     */
    private ExceptionUtility(){}

    /**
     * This method converts the stack trace of an exception into a String for human-readable debugging.
     *
     * @param exception The exception to get the stack trace for.
     * @return A stack trace for the object.
     */
    public static String convertStackTraceToString(final Exception exception) {
        try(StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter)){

            exception.printStackTrace(printWriter);

            return stringWriter.toString();
        } catch (final IOException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Get a full stack trace string for a throwable.
     *
     * @param throwable the throwable to get the stack trace for
     * @return the string representation
     */
    public static String getFullStackTrace(final Throwable throwable){
        return ExceptionUtilsWrapper.getFullStackTrace(throwable);
    }

    /**
     * Get a partial stack trace string representation for a throwable.
     *
     * @param throwable the throwable to get the stack trace for
     * @return the string representation
     */
    public static String getStackTrace(final Throwable throwable) {
        return ExceptionUtilsWrapper.getStackTrace(throwable);
    }
}
