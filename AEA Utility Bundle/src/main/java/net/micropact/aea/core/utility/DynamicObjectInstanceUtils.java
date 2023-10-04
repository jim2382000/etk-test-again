package net.micropact.aea.core.utility;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.dynamic.DataObjectInstance;
import com.entellitrak.dynamic.DynamicObjectService;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * Utility class for dealing with dynamic object instances.
 *
 * @author Zachary.Miller
 */
public final class DynamicObjectInstanceUtils {

    /**
     * Utility classes do not need public constructors.
     */
    private DynamicObjectInstanceUtils() {
    }

    /**
     * Get the parent data object instance for a particular data object instance.
     *
     * @param etk entellitrak execution context
     * @param dataObjectInstance the data object instance
     * @return the parent data object instance
     */
    public static DataObjectInstance getParentDataObjectInstance(final ExecutionContext etk, final DataObjectInstance dataObjectInstance) {
        try {
            final DynamicObjectService dynamicObjectService = etk.getDynamicObjectService();
            final DataObjectService dataObjectService = etk.getDataObjectService();

            final long parentId = dataObjectInstance.properties().getParentId();

            final DataObject objectConfiguration = dataObjectInstance.configuration();
            final DataObject parentConfiguration = dataObjectService.getParent(objectConfiguration);

            return dynamicObjectService.get(DynamicObjectConfigurationUtils.getDynamicClass(etk, parentConfiguration),
                    parentId);
        } catch (final ClassNotFoundException e) {
            throw new GeneralRuntimeException(e);
        }
    }
}
