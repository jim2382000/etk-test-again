package net.micropact.aea.core.utility;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.dynamic.DataObjectInstance;

/**
 * This utility class contains methods for working with core's Dynamic Object and Configuration APIs.
 *
 * @author Zachary.Miller
 */
public final class DynamicObjectConfigurationUtils {

    private static final String ROOT_PACKAGE = "com.entellitrak.dynamic";

    /**
     * Utility classes do not need public constructors.
     */
    private DynamicObjectConfigurationUtils(){}

    /**
     * This method retrieves the dynamic class for a data object. This is a common operation
     * because the Dynamic Object API requires the use of the Class, however generic code will only have the
     * business key.
     *
     * @param etk entellitrak execution context
     * @param dataObject the data object
     * @return The class
     * @throws ClassNotFoundException If the class could not be found
     */
    public static Class<? extends DataObjectInstance> getDynamicClass(final ExecutionContext etk, final DataObject dataObject)
            throws ClassNotFoundException {
        final DataObjectService dataObjectService = etk.getDataObjectService();
        @SuppressWarnings("unchecked")
        final Class<? extends DataObjectInstance> theClass = (Class<? extends DataObjectInstance>) Class.forName(String.format("%s.%s",
                ROOT_PACKAGE,
                dataObjectService.getDataObjectByBusinessKey(dataObject.getBusinessKey()).getObjectName()));
        return theClass;
    }
}
