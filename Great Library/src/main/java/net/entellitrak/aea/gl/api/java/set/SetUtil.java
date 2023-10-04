package net.entellitrak.aea.gl.api.java.set;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for dealing with {@link Set}.
 *
 * @author Zachary.Miller
 */
public final class SetUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private SetUtil() {}

    /**
     * Return the difference of 2 sets.
     * That is, all items which are in the first set, but not in the 2nd set.
     * This method does not modify either set, but instead returns a new set.
     *
     * @param <T> The type of the elements in the sets
     * @param minuend the minuend
     * @param subtrahend the subtrahend
     * @return the difference
     */
    public static <T> Set<T> setDifference(final Set<T> minuend, final Set<T> subtrahend) {
        final Set<T> difference = new HashSet<>(minuend);
        difference.removeAll(subtrahend);
        return difference;
    }
}
