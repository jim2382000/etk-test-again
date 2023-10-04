package net.entellitrak.aea.gl.api.etk;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.dynamic.DataObjectInstance;
import com.entellitrak.dynamic.DynamicObjectService;
import com.entellitrak.filter.Criteria;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * Utility class for dealing with {@link DataObjectInstance}.
 *
 * @author Zachary.Miller
 */
public final class DynamicObjectUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private DynamicObjectUtil() {
    }

    /**
     * Get the dynamic object class corresponding to a particular data object configuration.
     *
     * @param etk entellitrak execution context
     * @param dataObject the data object
     * @return the class
     */
    public static Class<? extends DataObjectInstance> getDynamicClassByDataObject(
            // Suppress Unused method parameters should be removed
            @SuppressWarnings("squid:S1172") final ExecutionContext etk,
            final DataObject dataObject){
        try {
            @SuppressWarnings("unchecked")
            final Class<? extends DataObjectInstance> theClass = (Class<? extends DataObjectInstance>) Class.forName(
                    String.format("com.entellitrak.dynamic.%s",
                            dataObject.getObjectName()));
            return theClass;
        } catch (final ClassNotFoundException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Get the parent dynamic object of a particular dynamic object.
     *
     * @param <T> the type of the parent object
     * @param etk entellitrak execution context
     * @param dataObjectInstance the data object instance
     * @param parentObjectClass the class of the parent object
     * @return the parent object
     */
    public static <T extends DataObjectInstance> T getParent(
            final ExecutionContext etk,
            final DataObjectInstance dataObjectInstance,
            final Class<T> parentObjectClass) {
        final DynamicObjectService dynamicObjectService = etk.getDynamicObjectService();

        final long parentId = dataObjectInstance.properties().getParentId();

        return dynamicObjectService.get(parentObjectClass, parentId);
    }

    /**
     * Get the children dynamic objects of a given dynamic object.
     *
     * @param <T> the type of the child
     * @param etk entellitrak execution context
     * @param parentObject the parent object
     * @param childClass the class of the child
     * @return the children
     */
    public static <T extends DataObjectInstance> Collection<T> getChildren(
            final ExecutionContext etk,
            final DataObjectInstance parentObject,
            final Class<T> childClass) {
        final DynamicObjectService dynamicObjectService = etk.getDynamicObjectService();

        final Criteria criteria = etk.createCriteria();
        criteria.add(criteria.ids(
                parentObject.configuration(),
                new HashSet<>(
                        Arrays.asList(parentObject.properties().getId().toString()))));

        return dynamicObjectService.get(childClass, criteria);
    }
}
