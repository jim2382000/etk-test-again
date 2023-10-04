package net.micropact.aea.du.page.viewOrphanedFiles;

import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.utility.OrphanedEtkFileUtility;
import net.micropact.aea.du.utility.OrphanedFileCColumnUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/**
 * Controller code for a page which allows viewing orphaned files.
 *
 * @author Zachary.Miller
 */
@HandlerScript(type = PageController.class)
public class ViewOrphanedFilesController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
            BreadcrumbUtility.addLastChildFluent(
                DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                new SimpleBreadcrumb("View Orphaned Files",
                    "page.request.do?page=du.page.viewOrphanedFiles")));

        final Gson gson = new Gson();

        response.put("fileSummary", gson.toJson(
            Utility.arrayToMap(String.class, Object.class, new Object[][] {
                { "orphanedFiles", OrphanedEtkFileUtility.findOrphanedFiles(etk) },
                { "orphanedRecords", OrphanedFileCColumnUtility.findOrphanedCColumns(etk)
                	.stream()
                	.map(orphanedCColumn -> Utility.arrayToMap(String.class, Object.class, new Object[][]{
                            {"dataObjectName", orphanedCColumn.getDataElement().getDataObject().getName()},
                            {"dataElementName", orphanedCColumn.getDataElement().getName()},
                            {"dataObjectBusinessKey", orphanedCColumn.getDataElement().getDataObject().getBusinessKey()},
                            {"trackingId", orphanedCColumn.getTrackingId()},
                        }))
                	.collect(Collectors.toList())},
            })));

		response.put("csrfToken", gson.toJson(etk.getCSRFToken()));

        return response;
    }
}
