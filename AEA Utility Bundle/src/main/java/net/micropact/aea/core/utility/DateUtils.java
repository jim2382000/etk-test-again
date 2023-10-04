package net.micropact.aea.core.utility;

import java.util.Date;
import java.util.Optional;

/**
 * Utility methods for working with dates.
 *
 * @author Zachary.Miller
 */
public final class DateUtils {

    /**
     * The date format string for ISO-8601.
     */
    public static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'Z'";

    /**
     * The date time format string for ISO-8601 to minutes.
     */
    public static final String ISO_8601_DATE_TIME_TO_MINUTES_FORMAT = "yyyy-MM-dd'T'HH:mm'Z'";

    /**
     * Utility classes do not need public constructors.
     */
    private DateUtils() {}

    /**
     * Copy a date.
     * This is often needed because dates in java are mutable.
     * Does safely handle null.
     *
     * @param date the date to copy
     * @return the new date
     */
    public static Date copyDate(final Date date) {
        return Optional.ofNullable(date)
                .map(theDate -> new Date(theDate.getTime()))
                .orElse(null);
    }
}
