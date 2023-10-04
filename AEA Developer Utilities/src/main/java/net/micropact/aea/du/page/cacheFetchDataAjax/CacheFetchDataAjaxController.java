package net.micropact.aea.du.page.cacheFetchDataAjax;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.cache.Cache;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.ContentType;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.utility.Utility;

/**
 * This is the controller for a page which produces a JSON representation of the entire {@link Cache}.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class CacheFetchDataAjaxController implements PageController {

    /**
     * The number of elements to truncate a collection to.
     */
    private static final long COLLECTION_BOUND = 5;

    /**
     * The number of entries to truncate a map to.
     */
    private static final long MAP_BOUND = 20;

    /**
     * The number of characters to truncate strings to.
     */
    private static final int STRING_BOUND = 255;

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final Parameters parameters = etk.getParameters();

        final String action = parameters.getSingle("action");

        final Response response;

        /* The two actions we support are:
         *  - loadInitialData which will load all of the cache keys and truncate the data.
         *  - loadUnbounded which will load all data for a particular key without truncating it.
         */
        switch(action) {
            case "loadInitialData":
                response = loadInitialData(etk);
                break;
            case "loadUnbounded":
                response = loadUnbounded(etk);
                break;
            default:
                throw new GeneralRuntimeException(String.format("Unknown Action: %s", action));
        }

        return response;
    }

    /**
     * Loads unbounded data for a single key.
     *
     * @param etk entellitrak execution context
     * @return the response
     */
    private static Response loadUnbounded(final PageExecutionContext etk) {
        final Parameters parameters = etk.getParameters();
        final Cache<String, Serializable> cache = etk.getSerializableCache();

        final String key = parameters.getSingle("key");

        final TextResponse response = etk.createTextResponse();
        response.setContentType(ContentType.JSON);

        response.put("out", new Gson().toJson(buildDescription(cache.load(key), false)));

        return response;
    }

    /**
     * Load the initial data.
     * This data will be bounded.
     *
     * @param etk entellitrak execution context
     * @return the initial data
     */
    private static Response loadInitialData(final PageExecutionContext etk) {
        final Cache<String, Serializable> cache = etk.getSerializableCache();

        final List<Object> cacheEntries = cache.getKeys().stream()
                .map(key -> {
                    final Map<String, Object> cacheEntryMap = new TreeMap<>();
                    cacheEntryMap.put("key", key);
                    cacheEntryMap.put("value", buildDescription(cache.load(key), true));

                    return cacheEntryMap;
                })
                .collect(Collectors.toList());

        TextResponse response;

        response = etk.createTextResponse();
        response.setContentType(ContentType.JSON);
        final Gson gson = new GsonBuilder().serializeNulls().create();
		response.put("out", gson.toJson(cacheEntries));
        return response;
    }

    /**
     * This function is for semi-intelligently comparing two objects.
     * The order of the comparison goes as follows:
     * - nulls first
     * - class name 2nd
     * - if objects are same class and comparable, use their default compare
     * - compare the toString of the values
     *
     * @param o1 The first object
     * @param o2 The second object
     * @return A negative integer if o1 &lt; o2, 0 if o1 = o2, a positive integer if o1 &gt; o2
     */
    static int arbitraryCompare(final Object o1, final Object o2){
        final int returnValue;

        if(o1 == null){
            returnValue = o2 == null ? 0 : -1;
        }else if(o2 == null){
            returnValue = 1;
        }else if(Objects.equals(o1.getClass(), o2.getClass()) && o1 instanceof Comparable){
            @SuppressWarnings("unchecked")
            final Comparable<Object> o1Cast = (Comparable<Object>) o1;

            returnValue = o1Cast.compareTo(o2);
        }else{
            final int classNameComparison = o1.getClass().getName().compareTo(o2.getClass().getName());
            returnValue = classNameComparison != 0
                    ? classNameComparison
                            : o1.toString().compareTo(o2.toString());
        }

        return returnValue;
    }

    /**
     * This method builds a description of an object with information which can subsequently be serialized to JSON.
     * <p>
     *  The truncated key indicates that the value, or any of its subvalues (in the case of collections/maps) has been truncated.
     * </p>
     * <p>
     *  Examples of the format are:
     *  <pre>
     *  {type: null,
     *  isTruncated: false}
     *  {type: "map",
     *   className: "java.util.HashMap",
     *   values: [{key: &lt;recursive call&gt;, value: &lt;recursive-call&gt;}],
     *   totalSize: 21,
     *   isTruncated: true}
     *   {type: "collection",
     *    className: "java.util.LinkedList",
     *    value: [&lt;recursive-calls&gt;],
     *    totalSize: 352,
     *    isTruncated: true}
     *   {type: "other",
     *    className: "java.lang.String",
     *    value: "Hello World!",
     *    isTruncated: true}
     *   </pre>
     *
     * @param object The object to build a description for
     * @param truncate whether or not the description strings and collections should be truncated
     * @return A map with a description of the object.
     */
    private static Map<String, Object> buildDescription(final Object object, final boolean truncate){
        /* We use a TreeMap because it is nicer for testing to maintain the order of the map keys. */
        final Map<String, Object> map = new TreeMap<>();

        if(object == null){
            // Handle null
            map.put("type", null);
            map.put("isTruncated", false);
        }else if(object instanceof Map<?, ?>){
            final Map<?, ?> objectMap = (Map<?, ?>) object;

            // Handle Maps
            map.put("type", "map");
            map.put("className", object.getClass().getName());

            // We will sort the Map Entries for user convenience.
            final List<Entry<?, ?>> sortedEntries = new ArrayList<>(objectMap.entrySet());
            Collections.sort(sortedEntries, (entry1, entry2) -> {
                final int keyCompare = arbitraryCompare(entry1.getKey(), entry2.getKey());
                return keyCompare != 0
                        ? keyCompare
                                : arbitraryCompare(entry1.getValue(), entry2.getValue());
            });

            final Stream<Entry<?, ?>> entriesStream = sortedEntries.stream();
            final Stream<Entry<?, ?>> boundedEntriesStream = truncate ? entriesStream.limit(MAP_BOUND) : entriesStream;

            final List<Map<String, Object>> mapEntries = boundedEntriesStream
                    .map(entry -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
                        {"key", buildDescription(entry.getKey(), truncate)},
                        {"value", buildDescription(entry.getValue(), truncate)},
                    }))
                    .collect(Collectors.toList());

            map.put("values", mapEntries);

            final int totalSize = objectMap.size();
            map.put("totalSize", totalSize);

            /* A map is truncated if the map is truncated or any of its keys or values has been truncated. */
            map.put("isTruncated", mapEntries.size() < objectMap.size()
                    || mapEntries.stream()
                    .anyMatch(mapEntry -> {
                        @SuppressWarnings("unchecked")
                        final Map<String, Object> keyDescription = (Map<String, Object>) mapEntry.get("key");
                        @SuppressWarnings("unchecked")
                        final Map<String, Object> valueDescription = (Map<String, Object>) mapEntry.get("value");
                        return (boolean) keyDescription.get("isTruncated")
                                || (boolean) valueDescription.get("isTruncated");
                    }));

        }else if (object instanceof Collection<?>){
            // Handle Collections
            map.put("type", "collection");
            map.put("className", object.getClass().getName());

            final Collection<?> collection = (Collection<?>) object;
            final Stream<?> itemsStream = collection.stream();

            final Stream<?> truncatedItemsStream = truncate ? itemsStream.limit(COLLECTION_BOUND) : itemsStream;

            final List<Map<String, Object>> theValues = truncatedItemsStream
                    .map(item -> buildDescription(item, truncate))
                    .collect(Collectors.toList());

            // Sort non-ordered Collections to make it easier for the user
            if(!(object instanceof List<?>)){
                Collections.sort(theValues, CacheFetchDataAjaxController::arbitraryCompare);
            }

            final int totalSize = collection.size();
            map.put("totalSize", totalSize);
            map.put("value", theValues);

            /* A collection is truncated if the collection itself was truncated, or whether any of its values was
             * truncated. */
            map.put("isTruncated", theValues.size() < collection.size()
                    || theValues
                    .stream()
                    .anyMatch(value -> (boolean) value.get("isTruncated")));
        }else{
            // Handle all others
            map.put("type", "other");
            map.put("className", object.getClass().getName());

            final String stringValue = object.toString();
            final String truncatedValue = truncate
                    ? stringValue.substring(0, Math.min(STRING_BOUND, stringValue.length()))
                    : stringValue;

            map.put("value", truncatedValue);
            map.put("isTruncated", truncatedValue.length() < stringValue.length());
        }

        return map;
    }
}
