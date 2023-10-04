package net.micropact.aea.du.page.rdoTextSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.configuration.DataObjectType;
import com.entellitrak.configuration.DataType;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.query.EscapeLike;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/**
 * This class is the controller code for a page which will do a text-search of RDOs. It accepts search criteria and
 * returns search results.
 *
 * <p>
 * This class is very dynamically typed (uses Maps and Lists instead of other objects). This could be changed, but it
 * seemed like a lot of effort for what might not be much benefit.
 * </p>
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class RDOTextSearchController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final Parameters parameters = etk.getParameters();

        final TextResponse response = etk.createTextResponse();

        /* Get the parameters */

        /* Flag to indicate whether or not the form is being served for the first time. */
        final boolean isUpdate = "1".equals(parameters.getSingle("update"));
        final String searchText = parameters.getSingle("searchText");
        // entellitrak ids of data types to be searched
        final List<String> dataTypesParameter = parameters.getField("dataTypes");
        // business keys of data objects to be searched
        final List<String> dataObjectsParameter = parameters.getField("dataObjects");

        setBreadcrumb(response);

        // This list will contain errors that we encounter as we try to process the request
        final List<String> errors = new ArrayList<>();

        /*
         * We don't want to search on blank text, but we don't want to give an error when they bring up the page for the
         * first time.
         */
        if (isUpdate && "".equals(searchText)) {
            errors.add("You must enter Search Text");
        }

        final List<String> dataObjectsParameterNonNull = Optional.ofNullable(dataObjectsParameter)
            .orElse(Collections.emptyList());

        /*
         * Get the parameters in a form which is easier to work with. We don't want nulls, and we will set up defaults
         * for parameters which need them.
         */
        final List<String> dataTypesParameterNonNull = Optional.ofNullable(dataTypesParameter)
            .orElse(Collections.emptyList());
        final List<String> selectedDataTypesNames = !isUpdate
            ? Arrays.asList(
                DataType.TEXT.name(),
                DataType.LONG_TEXT.name())
            : dataTypesParameterNonNull;

        final List<ItemSelected<DataObject>> dataObjectsSelected = getSelectedDataObjects(etk, isUpdate,
            dataObjectsParameterNonNull);

        final List<DataObject> selectedDataObjects = filterSelectedDataObjects(dataObjectsSelected);

        final List<ItemSelected<DataType>> dataTypesSelected = getDataTypes(selectedDataTypesNames);
        final Set<DataType> selectedDataTypes = dataTypesSelected.stream()
            .filter(ItemSelected::isSelected)
            .map(ItemSelected::getItem)
            .collect(Collectors.toSet());

        final boolean doSearch = isUpdate && errors.isEmpty();

        final Gson gson = new GsonBuilder().serializeNulls().create();

        final String searchResults = doSearch
            ? gson.toJson(performSearch(etk, searchText, selectedDataObjects, selectedDataTypes))
            : "[]";

        response.put("errors", gson.toJson(errors));
        response.put("doSearch", gson.toJson(doSearch));
        response.put("searchText", gson.toJson(searchText));
        response.put("dataTypes", gson.toJson(datafyDataTypesSelected(dataTypesSelected)));
        response.put("dataObjects", gson.toJson(datafyObjectsSelected(dataObjectsSelected)));
        response.put("searchResults", searchResults);

        return response;
    }

    /**
     * Set the breadcrumb for the response.
     *
     * @param response
     *            the response
     */
    private static void setBreadcrumb(final TextResponse response) {
        BreadcrumbUtility.setBreadcrumbAndTitle(response,
            BreadcrumbUtility.addLastChildFluent(
                DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                new SimpleBreadcrumb("RDO Text Search",
                    "page.request.do?page=du.page.rdoTextSearch")));
    }

    /**
     * Get a data-representation of the data objects and whether they are selected in order to be passed to the view
     * code.
     *
     * @param dataObjectsSelected
     *            the data objects selected
     * @return the object for serialization
     */
    private static Object datafyObjectsSelected(final List<ItemSelected<DataObject>> dataObjectsSelected) {
        return dataObjectsSelected.stream()
            .map(dataObjectSelected -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
                { "BUSINESS_KEY", dataObjectSelected.getItem().getBusinessKey() },
                { "LABEL", dataObjectSelected.getItem().getLabel() },
                { "selected", dataObjectSelected.isSelected() },
            }))
            .collect(Collectors.toList());
    }

    /**
     * Get a data representation of the data types and whether they are selected in order to be passed to the view code.
     *
     * @param dataTypesSelected
     *            the data types selected
     * @return the object for serialization
     */
    private static Object datafyDataTypesSelected(final List<ItemSelected<DataType>> dataTypesSelected) {
        return dataTypesSelected.stream()
            .map(dataTypeSelected -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
                { "value", dataTypeSelected.getItem().name() },
                { "display", dataTypeSelected.getItem().getName() },
                { "selected", dataTypeSelected.isSelected() },
            }))
            .collect(Collectors.toList());
    }

    /**
     * This method takes a list of data object maps which have a "selected" key. It returns the business keys of just
     * the objects which have been selected.
     *
     * @param dataObjects
     *            Maps containing information about data objects as well as the "selected" key.
     * @return A list of the business keys of data objects which are selected
     */
    private static List<DataObject> filterSelectedDataObjects(final List<ItemSelected<DataObject>> dataObjects) {
        return dataObjects.stream()
            .filter(ItemSelected::isSelected)
            .map(ItemSelected::getItem)
            .collect(Collectors.toList());
    }

    /**
     * Adds a "selected" key to the data object indicating whether or not it should be searched. Data Objects will be
     * searchable if we are on an update version of the form and they have been selected for search.
     *
     * @param etk
     *            entellitrak execution context
     * @param isUpdate
     *            flag indicating whether or not we are on the update version of the form.
     * @param submittedObjectBusinessKeys
     *            The business keys that the user has selected to search over.
     * @return the data objects and whether they are selected
     */
    private static List<ItemSelected<DataObject>> getSelectedDataObjects(
        final ExecutionContext etk,
        final boolean isUpdate,
        final List<String> submittedObjectBusinessKeys) {
        return getRDOs(etk)
            .stream()
            .map(dataObject -> new ItemSelected<>(
                dataObject,
                !isUpdate || submittedObjectBusinessKeys.contains(dataObject.getBusinessKey())))
            .collect(Collectors.toList());
    }

    /**
     * This method will get the data type information for the data types which are searchable and will have a flag
     * indicating whether or not the data type has been selected to be searched.
     *
     * @param selectedDataTypes
     *            the data types hat have been selected to search
     * @return Maps of data type information
     */
    private static List<ItemSelected<DataType>> getDataTypes(final List<String> selectedDataTypes) {
        return Stream.of(DataType.TEXT, DataType.LONG_TEXT)
            .map(dataType -> new ItemSelected<>(dataType, selectedDataTypes.contains(dataType.name())))
            .collect(Collectors.toList());
    }

    /**
     * This method actually performs a search for the text within all RDOs.
     *
     * @param etk
     *            entellitrak execution context
     * @param searchText
     *            The text that the user wishes to search for
     * @param selectedDataObjects
     *            The business keys of the data objects which should be searched
     * @param selectedDataTypes
     *            The data types that the user wishes to search over
     * @return A list of search results. It is a deeply nested data structure. If you are interested in the exact
     *         format, it would be easiest to look at the result of it using your browser's developer tools.
     */
    private static List<Map<String, Object>> performSearch(final ExecutionContext etk,
        final String searchText,
        final List<DataObject> selectedDataObjects,
        final Set<DataType> selectedDataTypes) {
        return selectedDataObjects
            .stream()
            .map(dataObject -> getResultsForTable(etk, searchText, selectedDataTypes, dataObject))
            .filter(results -> !((Collection<?>) results.get("records")).isEmpty())
            .collect(Collectors.toList());
    }

    /**
     * This method will look for all occurrences of searchText within a particular reference data object.
     *
     * @param etk
     *            entellitrak execution context
     * @param searchText
     *            text to be searched for
     * @param selectedDataTypes
     *            The data types which should be searched over
     * @param dataObject
     *            the data object
     * @return A Map containing all the results for a particular RDO. Because of how the View code uses this result, the
     *         returned Map includes information about the RDO itself such as its Label.
     */
    private static Map<String, Object> getResultsForTable(final ExecutionContext etk,
        final String searchText,
        final Set<DataType> selectedDataTypes,
        final DataObject dataObject) {

        /*
         * The high-level algorithm is that we will have a Map of tracking ids to matching data elements. We will query
         * for each element and add any results to its entry in matchingRecords. When we are all done, we'll convert
         * matchingRecords to a form more digestible by the View. Doing it this way where we have a separate data query
         * for each data element could have us potentially bring a lot less data over the wire, because we only bring
         * back data which matches, instead of bringing back all data for a particular record when only one of its
         * fields matches.
         */
        final DataElementService dataElementService = etk.getDataElementService();

        final Map<Long, List<Map<String, Object>>> matchingRecords = new HashMap<>();

        /* Get all text/longText elements on this RDO */
        dataElementService.getDataElements(dataObject)
            .stream()
            .filter(dataElement -> selectedDataTypes.contains(dataElement.getDataType()))
            .filter(dataElement -> !dataElement.isBoundToLookup())
            .sorted(Comparator.comparing(DataElement::getName).thenComparing(DataElement::getBusinessKey))
            .forEach(textualDataElement -> {
                final String columnName = textualDataElement.getColumnName();
                final String elementName = textualDataElement.getName();

                /* Get all matching records for this data element */
                etk.createSQL(
                    String.format(Utility.isSqlServer(etk)
                        ? "SELECT ID, %s VALUE FROM %s WHERE %s LIKE '%%' + :searchText + '%%' ESCAPE :escapeChar"
                        : "SELECT ID, %s as \"VALUE\" FROM %s WHERE UPPER(%s) LIKE '%%' || UPPER(:searchText) || '%%' ESCAPE :escapeChar",
                        columnName,
                        dataObject.getTableName(),
                        columnName))
                    .setParameter("searchText", EscapeLike.escapeLike(etk, searchText))
                    .setParameter("escapeChar", EscapeLike.getEscapeCharString())
                    .fetchList()
                    .forEach(matchingValue ->
                /* Add the matching id/element information to our Map that we're building up */
                addMatchingItem(searchText,
                    matchingRecords,
                    ((Number) matchingValue.get("ID")).longValue(),
                    columnName,
                    elementName,
                    (String) matchingValue.get("VALUE")));
            });

        // Build the return value for this method
        final Map<String, Object> tableResults = new HashMap<>();
        tableResults.put("TABLE_NAME", dataObject.getTableName());
        tableResults.put("BUSINESS_KEY", dataObject.getBusinessKey());
        tableResults.put("LABEL", dataObject.getLabel());
        tableResults.put("records", convertMatchingRecordsMapToList(matchingRecords));

        return tableResults;
    }

    /**
     * This method adds a matching element/value to the map of matched objects. This method will add an id if it doesn't
     * exist in the map, otherwise it will update the id with the additional match.
     *
     * @param searchText
     *            The text which is being searched for
     * @param matchingItems
     *            The current map of ids to matched values
     * @param id
     *            The id of the item to be added
     * @param columnName
     *            The column of the element to add
     * @param elementName
     *            The name of the element to add
     * @param value
     *            The value to add
     */
    private static void addMatchingItem(
        final String searchText,
        final Map<Long, List<Map<String, Object>>> matchingItems,
        final Long id,
        final String columnName,
        final String elementName,
        final String value) {

        List<Map<String, Object>> matchedItem = matchingItems.get(id);

        if (matchedItem == null) {
            matchedItem = new ArrayList<>();
            matchingItems.put(id, matchedItem);
        }

        matchedItem.add(Utility.arrayToMap(String.class, Object.class, new Object[][] {
            { "COLUMN_NAME", columnName },
            { "ELEMENT_NAME", elementName },
            { "VALUE", findAllMatches(searchText, value) },
        }));
    }

    /**
     * This method will convert a Map of matching results to a List of Maps. This is because internally, the Map is
     * easier to construct originally, but the List is easier to work with in the View.
     *
     * @param matchingRecords
     *            Map of tracking ids to search results
     * @return List of search results
     */
    private static List<Map<String, Object>> convertMatchingRecordsMapToList(
        final Map<Long, List<Map<String, Object>>> matchingRecords) {
        return matchingRecords.entrySet()
            .stream()
            .map(matchingRecord -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
                { "ID", matchingRecord.getKey() },
                { "COLUMNS", matchingRecord.getValue() } }))
            .sorted(Comparator.comparing(theRecord -> (Long) theRecord.get("ID")))
            .collect(Collectors.toList());
    }

    /**
     * This method adds a text fragment to an existing list of fragments if the fragment is not empty.
     *
     * @param fragments
     *            The list of fragments to be added to
     * @param fragment
     *            The fragment which is to be added
     * @param isMatch
     *            Whether the fragment matches the search text
     */
    private static void addFragment(final List<Map<String, Object>> fragments, final String fragment,
        final boolean isMatch) {
        if (!fragment.isEmpty()) {
            fragments.add(Utility.arrayToMap(String.class, Object.class, new Object[][] {
                { "fragment", fragment },
                { "isMatch", isMatch },
            }));
        }
    }

    /**
     * This method takes a block of text and a keyword. It will find matches in the following way: If a line has the
     * keyword in it, we will return a list of fragments for that line, otherwise the line will thrown out. The
     * fragments that we split the lines into will have a flag indicating whether the fragment matches the keyword, or
     * does not match the keyword. This way the matching fragments can be highlighted in the View.
     *
     * @param keyword
     *            The word to be searched for
     * @param text
     *            the text to be searched over
     * @return A list of lines of fragments.
     */
    private static List<List<Map<String, Object>>> findAllMatches(final String keyword, final String text) {
        // We want to search for the text case-insensitive
        final Pattern pattern = Pattern.compile(Pattern.quote(keyword),
            Pattern.CASE_INSENSITIVE);

        // These will be the lines that contain a match
        final List<List<Map<String, Object>>> lineMatches = new ArrayList<>();

        final String[] lines = text.split("\r\n|\r|\n");
        Stream.of(lines).forEach(line -> {
            if (pattern.matcher(line).find()) {
                // The line contains a match

                // We are going to build a list of fragments
                final List<Map<String, Object>> fragments = new ArrayList<>();

                // We get a matcher
                final Matcher matcher = pattern.matcher(line);

                /* We store the index in the line where the matcher is going to start its next search */
                int lastIndex = 0;

                while (matcher.find(lastIndex)) {
                    // The remainder of the line still contains a match
                    // Get the indices of the matching fragment
                    final int startIndex = matcher.start();
                    final int endIndex = matcher.end();

                    // everything between lastIndex and the beginning of the match is an unmatching fragment
                    addFragment(fragments, line.substring(lastIndex, startIndex), false);
                    // everything between the startIndex and endIndex of the match is a matching fragment
                    addFragment(fragments, line.substring(startIndex, endIndex), true);
                    // indicate that we will continue our search at the end of the current match
                    lastIndex = endIndex;
                }

                // There are no more matches, so we add the remainder of the line as an unmatched fragment
                addFragment(fragments, line.substring(lastIndex), false);

                lineMatches.add(fragments);
            }
        });
        return lineMatches;
    }

    /**
     * Gets information about all the RDOs in the system.
     *
     * @param etk
     *            entellitrak execution context
     * @return A List of RDO configuration information
     */
    private static List<DataObject> getRDOs(final ExecutionContext etk) {
        final DataObjectService dataObjectService = etk.getDataObjectService();
        return dataObjectService.getDataObjectsByType(DataObjectType.REFERENCE)
            .stream()
            .sorted(Comparator.comparing(DataObject::getLabel)
                .thenComparing(DataObject::getTableName))
            .collect(Collectors.toList());
    }

    /**
     * Value Object for an item and whether it is selected.
     *
     * @author Zachary.Miller
     * @param <T>
     *            the type of the value which may be selected
     */
    private static class ItemSelected<T> {

        private final T item;
        private final boolean selected;

        /**
         * Simple constructor.
         *
         * @param theItem
         *            the item
         * @param isSelected
         *            whether the data object is selected
         */
        ItemSelected(final T theItem, final boolean isSelected) {
            item = theItem;
            selected = isSelected;
        }

        /**
         * The item.
         *
         * @return the item
         */
        public T getItem() {
            return item;
        }

        /**
         * Get whether the data object is selected.
         *
         * @return whether the data object is selected
         */
        public boolean isSelected() {
            return selected;
        }
    }
}
