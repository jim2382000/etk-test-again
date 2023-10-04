package net.micropact.aea.du.page.checkSimpleWorkflowCorruption;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.query.QueryUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/**
 * This serves as the Controller Code for a Page which displays any objects which appear to have a problem within
 * their Simple Workflow tables such as missing entries it ETK_WORKITEM.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class CheckSimpleWorkflowCorruptionController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final DataObjectService dataObjectService = etk.getDataObjectService();

        final TextResponse response = etk.createTextResponse();

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
                BreadcrumbUtility.addLastChildFluent(
                        DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                        new SimpleBreadcrumb("Check Simple Workflow Corruption",
                                "page.request.do?page=du.page.checkSimpleWorkflowCorruption")));

        final List<DataObject> btos = dataObjectService.getBaseTrackedObjects()
                .stream()
                .sorted(Comparator.comparing(DataObject::getBusinessKey))
                .collect(Collectors.toList());

        List<Map<String, Object>> objectsMissingWorkItems;
        List<Map<String, Object>> objectsMissingRuntimes;

        objectsMissingWorkItems = getMissingWorkItems(etk, btos);
        objectsMissingRuntimes = getMissingRuntimes(etk, btos);

        final Gson gson = new Gson();
        response.put("recordsWithoutWorkItems", gson.toJson(objectsMissingWorkItems));
        response.put("recordsWithoutRuntimes", gson.toJson(objectsMissingRuntimes));

        return response;
    }

    /**
     * This method retrieves all BTO records which are missing ETK_WORKITEM records.
     *
     * @param etk entellitrak execution context
     * @param btos A list of all BTO data objects in the system. This is only passed so that it only needs to be queried
     *          for once within this page.
     * @return A list of all objects which are missing ETK_WORKITEM records.
     */
    private static List<Map<String, Object>> getMissingWorkItems(final ExecutionContext etk,
            final List<DataObject> btos){
        return getMissingRecordsByQuery(etk, btos, "SELECT obj.id FROM %s obj WHERE NOT EXISTS(SELECT * FROM etk_workitem workItem WHERE workItem.workitem_id = obj.id_workflow) ORDER BY obj.id");
    }

    /**
     * Retrieves a list of all records which appear to have ETK_WORKITEM records, but not ETK_WORKFLOW_RUNTIME records.
     * Records which have ETK_WORKITEM records are excluded from this query because they will show up in a separate
     * list and it should be less confusing to have records not show in both places.
     *
     * @param etk entellitrak execution context
     * @param btos A list of all BTO data objects in the system.
     * @return A list of records which are missing ETK_WORKFLOW_RUNTIME records
     */
    private static List<Map<String, Object>> getMissingRuntimes(final ExecutionContext etk, final List<DataObject> btos){
        return getMissingRecordsByQuery(etk, btos, "SELECT obj.id FROM %s obj WHERE EXISTS(SELECT * FROM etk_workitem workItem WHERE workItem.workitem_id = obj.id_workflow) AND NOT EXISTS(SELECT * FROM etk_workflow_runtime runtime WHERE runtime.workitem_id = obj.id_workflow) ORDER BY obj.id");
    }

    /**
     * Returns a list of records which meet a particular query criteria.
     *
     * @param etk entellitrak execution context
     * @param btos maps representing BTO objects
     * @param query A query in the form of a "format String", which will take the BTO table name as a single parameter
     * @return a list of all records which match the query criteria
     */
    private static List<Map<String, Object>> getMissingRecordsByQuery(final ExecutionContext etk,
            final List<DataObject> btos,
            final String query){
        return btos
                .stream()
                .flatMap(bto -> {
                    final String tableName = bto.getTableName();
                    final String businessKey = bto.getBusinessKey();

                    return QueryUtility.mapsToLongs(etk.createSQL(String.format(query, tableName))
                            .fetchList())
                            .stream()
                            .map(objectId -> Utility.arrayToMap(String.class, Object.class, new Object[][]{
                                    {"businessKey", businessKey},
                                    {"trackingId", objectId}}));
                }).collect(Collectors.toList());
    }
}
