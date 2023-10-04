package net.micropact.aea.utility;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class contains common functions related to the import/export utilities for various frameworks for moving
 * user-entered data between environments. The way that most of the import/exports work is that the export uses
 * {@link com.entellitrak.SQLFacade#fetchList()} and put that data straight into an XML. The imports then parse that XML
 * back out into Lists of Maps and perform their operations on those. The XML document will look like:
 *
 * <pre>
 * {@code
 *  <objects>
 *    <ETK_USER>
 *      <row>
 *        <USER_ID>1</USER_ID>
 *        <USERNAME>administrator</USERNAME>
 *      </row>
 *      <row>
 *        <USER_ID>10</USER_ID>
 *        <USERNAME>zmiller</USERNAME>
 *      </row>
 *    </ETK_USER>
 *    <T_CASE>
 *      <row>...</row>
 *      ...
 *    </T_CASE>
 *    ...
 *  </objects>
 *  }
 * </pre>
 *
 * @author zmiller
 */
public final class ImportExportUtility {

    /**
     * There is no reason to instantiate an {@link ImportExportUtility}.
     */
    private ImportExportUtility() {
    }

    /**
     * This will return the table as a list of maps where the keys are the column names.
     *
     * @param document
     *            an XML document
     * @param tableName
     *            the name of the table to search the XML document for
     * @return Elements representing the 'rows' of the table
     */
    public static List<Map<String, String>> getTable(final Document document, final String tableName) {
        final List<Map<String, String>> returnList = new ArrayList<>();
        final NodeList tableNodeList = document.getElementsByTagName(tableName);
        final Node tableNode = tableNodeList.item(0);
        final NodeList tableChildrenNodeList = tableNode.getChildNodes();

        for (int i = 0; i < tableChildrenNodeList.getLength(); i++) {
            final Map<String, String> row = new HashMap<>();
            final Node rowNode = tableChildrenNodeList.item(i);

            final NodeList columnNodeList = rowNode.getChildNodes();
            for (int j = 0; j < columnNodeList.getLength(); j++) {
                final Node columnNode = columnNodeList.item(j);
                final Node textNode = columnNode.getChildNodes().item(0);
                row.put(columnNode.getNodeName(), textNode == null ? null : textNode.getNodeValue());
            }

            returnList.add(row);
        }
        return returnList;
    }

    /**
     * This method searches objects for the particular map entries whose searchkey has the value searchValue. and then
     * gets the value for that map's valueKey
     *
     * @param <K>
     *            the type of the key
     * @param <V>
     *            the type of the value
     * @param objects
     *            The objects to search
     * @param searchKey
     *            The key for the Maps which should be used
     * @param searchValue
     *            The value of the key we are looking for
     * @param valueKey
     *            The key of the property we wish to extract the final value from
     * @return The value we are searching for. Returns null if no matching value was found.
     */
    public static <K, V> V lookupValueInListOfMaps(final List<Map<K, V>> objects,
        final K searchKey,
        final V searchValue,
        final K valueKey) {
        return objects.stream()
            .filter(o -> Objects.equals(o.get(searchKey), searchValue))
            .findAny()
            .map(o -> o.get(valueKey))
            .orElse(null);
    }

    /**
     * This method searches objects for the map where the value for the searchKey key is searchValue and returns that
     * map.
     *
     * @param <K>
     *            the type of the key
     * @param <V>
     *            the type of the value
     * @param objects
     *            The objects to search through
     * @param searchKey
     *            The key we wish to search on
     * @param searchValue
     *            The value of the key we wish to find
     * @return The object we were searchnig for. Returns null if no value was found.
     */
    public static <K, V> Map<K, V> lookupMapByKey(final List<Map<K, V>> objects,
        final K searchKey,
        final V searchValue) {
        return objects.stream()
            .filter(map -> Objects.equals(map.get(searchKey), searchValue))
            .findAny()
            .orElse(null);
    }

    /**
     * Will create a new node with a text-node inside and append it to another node.
     *
     * @param document
     *            The XML document that the tag will be a part of.
     * @param parentNode
     *            The node we will append the new node to.
     * @param tagName
     *            The name of the node we will create.
     * @param value
     *            The text content of the new node.
     */
    public static void addSimpleElement(final Document document,
        final Element parentNode,
        final String tagName,
        final Object value) {
        final Element element = document.createElement(tagName.toUpperCase());

        final String textValue;
        if (value == null) {
            textValue = "";
        } else if (value instanceof byte[]) {
            textValue = new String(Base64.encodeBase64((byte[]) value), StandardCharsets.UTF_8);
        } else {
            textValue = value.toString();
        }

        element.appendChild(document.createTextNode(textValue));
        parentNode.appendChild(element);
    }

    /**
     * Add a list of maps as a new element to the XML.
     *
     * @param document
     *            The XML document which the new elements will be added to
     * @param parent
     *            The parent node which the new nodes will be appended to
     * @param groupName
     *            In the sample XML at the top of this file, this would be the table name
     * @param rows
     *            The data that the new group should contain. The keys of the maps will be the tag names and the values
     *            will be the text content.
     */
    public static void addListToXml(final Document document,
        final Element parent,
        final String groupName,
        final List<Map<String, Object>> rows) {
        final Element group = document.createElement(groupName);
        parent.appendChild(group);

        rows.forEach(row -> {
            final Element element = document.createElement("row");
            group.appendChild(element);
            row.keySet().forEach(key -> addSimpleElement(document, element, key, row.get(key)));
        });
    }
}
