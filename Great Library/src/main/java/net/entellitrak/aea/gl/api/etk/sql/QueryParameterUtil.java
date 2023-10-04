package net.entellitrak.aea.gl.api.etk.sql;

import java.util.Collections;
import java.util.List;

/**
 * Utility class for dealing with SQL query parameters.
 *
 * @author Zachary.Miller
 */
public final class QueryParameterUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private QueryParameterUtil() {
    }

    /**
     * This method is used to get around the limitation that
     * {@link com.entellitrak.SQLFacade#setParameter(String, Object)} fails when the Object is an empty list.
     * When passed a list, this method will will either return the list (if the list is not empty),
     * or return a list just containing null, ensuring that calling setParameter with the result of this function
     * should not fail.
     *
     * @param <T> Type of the items in the List
     * @param list The list to be converted to a parameter list
     * @return A List which is either list, or a list containing just null.
     */
    public static <T> List<T> toNonEmptyParameterList(final List<T> list){
        final List<T> returnList;

        if(list.isEmpty()){
            returnList = Collections.singletonList(null);
        }else{
            returnList = list;
        }

        return returnList;
    }
}
