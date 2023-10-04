package net.entellitrak.aea.gl.api.etk.sql;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.entellitrak.SQLFacade;

import net.entellitrak.aea.gl.api.java.Coercion;
import net.entellitrak.aea.gl.api.java.StringUtil;

/**
 * Utility class for converting results of {@link SQLFacade} to more usable types.
 *
 * @author Zachary.Miller
 */
public final class QueryResultUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private QueryResultUtil() {
    }

    /**
     * Convert a list of maps to a list of longs.
     * The maps must have exactly 1 entry, and the value must be one that
     * makes sense to be converted to a long.
     * This function is useful for converting the results of {@link SQLFacade#fetchList()}
     * when it has selected ids.
     *
     * @param maps the maps
     * @return the longs
     */
    public static List<Long> convertToListOfLongs(final List<Map<String, Object>> maps){
        return maps.stream()
                .map(map -> Coercion.convertToLong(map.values().iterator().next()))
                .collect(Collectors.toList());
    }

    /**
     * Convert a List of Maps to a list of Strings.
     * This is convenient for using with the result of {@link SQLFacade#fetchList()}.
     * The maps should each contain one entry, whose value is a String.
     *
     * @param maps the maps
     * @return the strings
     */
    public static List<String> convertToListOfStrings(final List<Map<String, Object>> maps) {
        return maps.stream()
                .map(map -> StringUtil.toNonEmptyString(Objects.toString(map.values().iterator().next(), null)))
                .collect(Collectors.toList());
    }
}
