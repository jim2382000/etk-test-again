package net.micropact.aea.du.page.viewObjectDataAjax;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.ContentType;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.query.QueryUtility;
import net.micropact.aea.utility.IJson;
import net.micropact.aea.utility.JsonUtilities;
import net.micropact.aea.utility.Utility;

/**
 * This class serves as the controller code for a JSON page which can fetch information about a BTO and all of its
 * descendants.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class ViewObjectDataAjaxController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final Parameters parameters = etk.getParameters();
        final DataObjectService dataObjectService = etk.getDataObjectService();

        final TextResponse response = etk.createTextResponse();
        response.setContentType(ContentType.JSON);

        final String objectBusinessKey = parameters.getSingle("dataObjectKey");
        final long trackingId = Long.parseLong(parameters.getSingle("trackingId"));

        final DataObject dataObject = dataObjectService.getDataObjectByBusinessKey(objectBusinessKey);

        response.put("out", JsonUtilities.encode(fetchBTO(etk, dataObject, trackingId)));

        return response;
    }

    /**
     * Retrieves information about a single BTO and all of its descendants.
     *
     * @param etk entellitrak execution context
     * @param dataObject the data object
     * @param trackingId trackingId of the object
     * @return A representation of the object and its descendants.
     */
    private static List<Map<String, Object>> fetchBTO(
            final ExecutionContext etk,
            final DataObject dataObject,
            final long trackingId) {
        final List<Map<String, Object>> objectList = new ArrayList<>();

        final List<TreeNode> objectNodes = new ArrayList<>();
        objectNodes.add(fetchTreeNode(etk, dataObject, trackingId));

        objectList.add(Utility.arrayToMap(String.class, Object.class, new Object[][]{
            {"businessKey", dataObject.getBusinessKey()},
            {"name", dataObject.getName()},
            {"objects", objectNodes},
        }));

        return objectList;
    }

    /**
     * Gets information about a particular data object and all of its descendants.
     *
     * @param etk entellitrak execution context
     * @param dataObject the data object
     * @param trackingId trackingId of the object
     * @return An object containing information about the object and all of its descendants.
     */
    private static TreeNode fetchTreeNode(
            final ExecutionContext etk,
            final DataObject dataObject,
            final long trackingId) {
        return new TreeNode(
                dataObject,
                trackingId,
                fetchElements(etk, dataObject, trackingId),
                fetchChildren(etk, dataObject, trackingId));
    }

    /**
     * Gets the elements and their data for a particular data object.
     *
     * @param etk entellitrak execution context
     * @param dataObject the data object
     * @param trackingId trackingId of the object
     * @return An object representing the elements and their data
     */
    private static List<Map<String, Object>> fetchElements(
            final ExecutionContext etk,
            final DataObject dataObject,
            final long trackingId) {
        final DataElementService dataElementService = etk.getDataElementService();

        return dataElementService.getDataElements(dataObject)
                .stream()
                .sorted(Comparator.comparing(DataElement::getName).thenComparing(DataElement::getBusinessKey))
                .map(dataElement -> Utility.arrayToMap(String.class, Object.class, new Object[][]{
                    {"elementBusinessKey", dataElement.getBusinessKey()},
                    {"name", dataElement.getName()},
                    {"value", getDataElementValue(etk, dataElement, trackingId)},
                }))
                .collect(Collectors.toList());
    }

    /**
     * This method gets all of the descendants and their data for a particular parent object.
     *
     * @param etk entellitrak execution context
     * @param dataObject the data object
     * @param trackingId trackingId of the object
     * @return An object representing all of the descendants of an object and their data
     */
    private static List<Map<String, Object>> fetchChildren(
            final ExecutionContext etk,
            final DataObject dataObject,
            final long trackingId) {
        final DataObjectService dataObjectService = etk.getDataObjectService();

        return dataObjectService.getChildren(dataObject)
                .stream()
                .map(childObject -> {
                    final List<TreeNode> childNodes = QueryUtility.mapsToLongs(etk.createSQL(String.format("SELECT ID FROM %s WHERE id_parent = :trackingId",
                            childObject.getTableName()))
                            .setParameter("trackingId", trackingId)
                            .fetchList())
                            .stream()
                            .map(childTrackingId -> fetchTreeNode(etk, childObject, childTrackingId))
                            .collect(Collectors.toList());
                    return Utility.arrayToMap(String.class, Object.class, new Object[][]{
                        {"businessKey", childObject.getBusinessKey()},
                        {"name", childObject.getName()},
                        {"objects", childNodes},
                    });
                })
                .collect(Collectors.toList());
    }

    /**
     * This method gets the value of a particular element for a particular object.
     *
     * @param etk entellitrak execution context
     * @param dataElement data element the data is to be retrieved for
     * @param trackingId trackingId of the object
     * @return The value of the element
     */
    private static Object getDataElementValue(
            final ExecutionContext etk,
            final DataElement dataElement,
            final long trackingId) {
        try {
            final Object returnValue;
            if(dataElement.isMultiValued().booleanValue()){
                returnValue = QueryUtility.toSimpleList(etk.createSQL(String.format("SELECT %s FROM %s WHERE id_owner = :idOwner ORDER BY  list_order, id",
                        dataElement.getColumnName(),
                        dataElement.getTableName()))
                        .setParameter("idOwner", trackingId)
                        .fetchList());
            }else{
                returnValue = etk.createSQL(String.format("SELECT %s FROM %s WHERE id = :trackingId",
                        dataElement.getColumnName(),
                        dataElement.getDataObject().getTableName()))
                        .setParameter("trackingId", trackingId)
                        .fetchObject();
            }
            return returnValue;
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * This class represents a data object's data and the data of all of its descendants.
     *
     * @author zachary.miller
     */
    static class TreeNode implements IJson{
        private final DataObject dataObject;
        private final long trackingId;
        private final List<Map<String, Object>> elements;
        private final List<Map<String, Object>> children;

        /**
         * Constructs a new TreeNode.
         *
         * @param theDataObject the data object
         * @param theTrackingId trackingId of the object
         * @param theElements the elements of this object
         * @param theChildren object representing the descendants of the object.
         */
        TreeNode(final DataObject theDataObject,
                final long theTrackingId,
                final List<Map<String, Object>> theElements,
                final List<Map<String, Object>> theChildren) {
            dataObject = theDataObject;
            trackingId = theTrackingId;
            elements = theElements;
            children = theChildren;
        }

        @Override
        public String encode() {
            return JsonUtilities.encode(Utility.arrayToMap(String.class, Object.class, new Object[][]{
                {"dataObjectKey", dataObject.getBusinessKey()},
                {"name", dataObject.getName()},
                {"trackingId", trackingId},
                {"elements", elements},
                {"children", children},
            }));
        }
    }
}
