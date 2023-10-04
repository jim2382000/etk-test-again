package net.entellitrak.aea.gl.api.etk;

import java.util.Objects;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.configuration.DataObject;

/**
 * Utility functionality related to {@link DataElement}.
 *
 * @author Zachary.Miller
 */
public final class DataElementUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private DataElementUtil() {
    }

    /**
     * Finds a data element belonging to a particular data object, and having a particular property name.
     *
     * @param etk entellitrak execution context
     * @param dataObject the data object
     * @param elementPropertyName the element property name
     * @return the element, or null if it does not exist
     */
    public static DataElement getDataElementByPropertyName(final ExecutionContext etk, final DataObject dataObject, final String elementPropertyName) {
        final DataElementService dataElementService = etk.getDataElementService();

        return dataElementService.getDataElements(dataObject)
                .stream()
                .filter(dataElement -> Objects.equals(elementPropertyName, dataElement.getPropertyName()))
                .findAny()
                .orElse(null);
    }
}
