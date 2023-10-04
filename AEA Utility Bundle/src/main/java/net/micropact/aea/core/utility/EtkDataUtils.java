package net.micropact.aea.core.utility;

import java.util.stream.Stream;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;

/**
 * Utility class for methods dealing with entellitrak data (medata APIs).
 *
 * @author Zachary.Miller
 */
public final class EtkDataUtils {

    /**
     * Utility classes do not need constructors.
     */
    private EtkDataUtils(){}

    /**
     * Get the actual table name which the data element stores its data in.
     * In the case of multi-valued elements, returns the M_ table.
     *
     * @param dataElement the data element
     * @return the table name
     */
    public static String getActualTableName(final DataElement dataElement){
        return dataElement.isMultiValued().booleanValue()
                ? dataElement.getTableName()
                : dataElement.getDataObject().getTableName();
    }

    /**
     * Get all data elements in the system.
     *
     * @param etk entellitrak execution context
     * @return the data elements
     */
    public static Stream<DataElement> getAllDataElements(final ExecutionContext etk){
        final DataObjectService dataObjectService = etk.getDataObjectService();
        final DataElementService dataElementService = etk.getDataElementService();

        return dataObjectService.getDataObjects()
                .stream()
                .flatMap(dataObject -> dataElementService.getDataElements(dataObject).stream());
    }

    /**
     * Determine whether a given data object is a root object (has no parents).
     *
     * @param etk entellitrak execution context
     * @param dataObject the data object
     * @return whether the data object is a root
     */
    public static boolean isRootDataObject(final ExecutionContext etk, final DataObject dataObject){
        final DataObjectService dataObjectService = etk.getDataObjectService();
        return dataObjectService.getParent(dataObject) == null;
    }

    /**
     * Get the root object for a particular data object. If the object passed in is the root, the data object will be
     * returned.
     *
     * @param etk entellitrak execution context
     * @param dataObject the data object
     * @return the base object
     */
    public static DataObject getRootObject(final ExecutionContext etk, final DataObject dataObject){
        final DataObjectService dataObjectService = etk.getDataObjectService();

        DataObject current = dataObject;

        while(!isRootDataObject(etk, current)){
            current = dataObjectService.getParent(current);
        }

        return current;
    }
}
