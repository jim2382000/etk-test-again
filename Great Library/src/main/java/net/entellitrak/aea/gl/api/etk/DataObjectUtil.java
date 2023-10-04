package net.entellitrak.aea.gl.api.etk;

import java.util.Objects;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.dynamic.DataObjectInstance;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * Utility functionality related to {@link DataObject}.
 *
 * @author Zachary.Miller
 */
public final class DataObjectUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private DataObjectUtil() {
    }

    /**
     * Find a {@link DataObject} corresponding to a given {@link DataObjectInstance} class.
     *
     * @param etk entellitrak execution context
     * @param dynamicClass the dynamic class
     * @return the data object
     */
    public static DataObject getDataObjectByDynamicClass(final ExecutionContext etk, final Class<? extends DataObjectInstance> dynamicClass) {
        final DataObjectService dataObjectService = etk.getDataObjectService();

        final String objectName = dynamicClass.getSimpleName();

        return dataObjectService
                .getDataObjects()
                .stream()
                .filter(dataObject -> Objects.equals(dataObject.getObjectName(), objectName))
                .findAny()
                .orElseThrow(() -> new GeneralRuntimeException(String.format("Could not find data object corresponding to class %s", dynamicClass)));
    }

    /**
     * Find a {@link DataObject} by its table name.
     * Should only be used in situations where a business key or dynamic class
     * cannot be used, such as looking up based on ETK_FILE.OBJECT_TYPE.
     *
     * @param etk entellitrak execution context
     * @param tableName the table name
     * @return the data object
     */
    public static DataObject getDataObjectByTableName(final ExecutionContext etk, final String tableName) {
    	final DataObjectService dataObjectService = etk.getDataObjectService();

        return dataObjectService
                .getDataObjects()
                .stream()
                .filter(dataObject -> Objects.equals(dataObject.getTableName(), tableName))
                .findAny()
                .orElse(null);
    }

    /**
     * Determine whether a given data object is a base/root object (has no parents).
     *
     * @param etk entellitrak execution context
     * @param dataObject the data object
     * @return whether the data object is a base
     */
    public static boolean isBaseDataObject(final ExecutionContext etk, final DataObject dataObject){
        final DataObjectService dataObjectService = etk.getDataObjectService();

        return dataObjectService.getParent(dataObject) == null;
    }

    /**
     * Get the base/root object for a particular data object. If the object passed in is the base, the data object will be
     * returned.
     *
     * @param etk entellitrak execution context
     * @param dataObject the data object
     * @return the base object
     */
    public static DataObject getBaseObject(final ExecutionContext etk, final DataObject dataObject){
        final DataObjectService dataObjectService = etk.getDataObjectService();

        DataObject current = dataObject;

        while(!isBaseDataObject(etk, current)){
            current = dataObjectService.getParent(current);
        }

        return current;
    }
}
