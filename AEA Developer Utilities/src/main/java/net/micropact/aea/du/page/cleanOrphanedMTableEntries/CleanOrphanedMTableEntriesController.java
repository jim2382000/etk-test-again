package net.micropact.aea.du.page.cleanOrphanedMTableEntries;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.utility.EtkDataUtils;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

/**
 * This serves as the controller code for a page which deletes records from entellitrak M_ tables which
 * are orphaned because their ID_OWNER record no longer exists.
 *
 * @author ahargrave 09/09/2016
 */
@HandlerScript(type = PageController.class)
public class CleanOrphanedMTableEntriesController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
                BreadcrumbUtility.addLastChildFluent(
                        DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                        new SimpleBreadcrumb("Clean Orphaned M Table Entries",
                                "page.request.do?page=du.page.cleanOrphanedMTableEntries")));

        final long totalCount = EtkDataUtils.getAllDataElements(etk)
                .filter(DataElement::isMultiValued)
                .mapToLong(dataElement -> {
                    final String objectTable = dataElement.getDataObject().getTableName();
                    final String mTable = dataElement.getTableName();
                    return etk.createSQL(String.format("DELETE FROM %s WHERE ID_OWNER NOT IN (SELECT ID FROM %s)",
                            mTable,
                            objectTable))
                            .execute();
                }).sum();

        response.put("count", new Gson().toJson(totalCount));
        return response;
    }
}
