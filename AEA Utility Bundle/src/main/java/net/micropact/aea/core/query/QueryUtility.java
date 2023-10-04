package net.micropact.aea.core.query;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.entellitrak.SQLFacade;

import net.micropact.aea.core.utility.StreamUtils;

/**
 * This class contains methods to make it easier to deal with the query results and related functionality by providing
 * methods such as converting the return types of {@link com.entellitrak.SQLFacade#fetchList()} to a regular list.
 *
 * @author zmiller
 */
public final class QueryUtility {

    /**
     * The maximum number of elements we should use within an IN clause.
     * The maximum in oracle is 1000 and is greater than that in SQL Server so we will try to stick to 1000.
     */
    public static final long IN_CLAUSE_LIMIT = 1000;

    /**
     * Utility classes do not need public constructors.
     */
    private QueryUtility(){}

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
            returnList = Arrays.asList((T) null);
        }else{
            returnList = list;
        }

        return returnList;
    }

    /**
     * <p>
     *  Method for batch retrieving records based on relatively simple SELECT queries with IN clause which would exceed
     *  the allowed number of parameters in IN clauses within entellitrak. This method is only appropriate when the
     *  row(s) returned by each parameter are completely independent. Most large real-world IN clauses will fall into
     *  this scenario. For instance if loading a bunch of objects (or their children) by trackingId, the rows
     *  corresponding to one trackingId do not affect the rows returned by another. Other scenarios, such as selecting
     *  records NOT IN some large list are not appropriate for this method.
     * </p>
     * <p>
     *  Gets around the limitation of entellitrak and its various databases restrictions around in clause limits.
     *  Oracle for instance limits each in clause to 1000 parameters and SQL server limits the total parameters
     *  for an entire query to 2000.
     * </p>
     * <p>
     *  Allows only a <em>single</em> parameter to be batched.
     * </p>
     * <p>
     *  <strong>Since the query will be split up and executed multiple times, certain clauses (such as ORDER BY) will
     *  not work as expected.</strong>
     * </p>
     *
     * @param sqlFacade the sql facade containing the query to be executed multiple times and all parameters set with
     *      the exception of parameterName
     * @param parameterName the name of the parameter which is is being split into smaller batches
     * @param parameterValues the values of the parameter which is being batched
     * @return the combined query results
     */
    public static List<Map<String, Object>> fetchListBatched(
            final SQLFacade sqlFacade,
            final String parameterName,
            final List<?> parameterValues) {
        return StreamUtils.chunk(IN_CLAUSE_LIMIT, parameterValues.stream())
                .flatMap(parameterValuesChunk
                    -> sqlFacade
                    .setParameter(parameterName, parameterValuesChunk.collect(Collectors.toList()))
                    .fetchList()
                    .stream())
                .collect(Collectors.toList());
    }

    /**
     * Converts the results of {@link com.entellitrak.SQLFacade#fetchList()} to a simple list for the common use-case
     * of selecting only a single column from the database.
     *
     * @param <T> type of the selected column
     * @param queryResults The query results coming from {@link com.entellitrak.SQLFacade#fetchList()}
     * @return A list containing only the first (there should only be one) value from each of the maps.
     */
    public static <T> List<T> toSimpleList(final List<? extends Map<?, ?>> queryResults){
        return queryResults.stream()
                .map(map -> {
                    @SuppressWarnings("unchecked")
                    final T item = (T) map.values().iterator().next();
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * This is a utility method which converts a list of numbers to a list of longs.
     * This is useful in part because we get a lot of {@link java.math.BigDecimal}s back from the database when they
     * actually represent numbers that entellitrak treats as longs in other places.
     *
     * @param numbers list of numbers to be converted
     * @return the converted list of numbers
     */
    public static List<Long> numbersToLongs(final List<? extends Number> numbers){
        return numbers.stream()
                .map(Coersion::toLong)
                .collect(Collectors.toList());
    }

    /**
     * This is convenience method which can convert the results of an {@link com.entellitrak.SQLFacade#fetchMap()}
     * call which only returns a single column of type number, to a {@link List} of {@link Long}.
     *
     * <p>
     *  When calling this method it is your responsibility to make sure that the Maps only contain a single entry and
     *  that the value of that entry is a {@link Number}
     * </p>
     *
     * @param queryResults The list of Maps which contains the Longs as values.
     * @return The list of longs
     */
    public static List<Long> mapsToLongs(final List<? extends Map<?, ?>> queryResults){
        return numbersToLongs(QueryUtility.toSimpleList(queryResults));
    }
}
