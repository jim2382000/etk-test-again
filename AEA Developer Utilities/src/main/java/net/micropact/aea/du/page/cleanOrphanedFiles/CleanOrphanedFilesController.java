package net.micropact.aea.du.page.cleanOrphanedFiles;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.du.utility.FileUtility;
import net.micropact.aea.du.utility.OrphanedEtkFileUtility;
import net.micropact.aea.du.utility.OrphanedFileCColumnUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/**
 * Controller code for a page which deletes files from etk_file which are not referenced anymore.
 *
 * @author zmiller
 * @see FileUtility
 */
@HandlerScript(type = PageController.class)
public class CleanOrphanedFilesController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        setBreadcrumbAndTitle(etk, response);

        PageUtility.validateCsrfToken(etk);

        response.put("fileSummary", new Gson().toJson(
                Utility.arrayToMap(String.class, Object.class, new Object[][]{
                    {"orphanedFiles", OrphanedEtkFileUtility.cleanOrphanedFiles(etk)},
                    {"orphanedRecords", OrphanedFileCColumnUtility.cleanOrphanedCColumns(etk).stream()
                	.map(orphanedCColumn -> Utility.arrayToMap(String.class, Object.class, new Object[][]{
                            {"dataObjectName", orphanedCColumn.getDataElement().getDataObject().getName()},
                            {"dataElementName", orphanedCColumn.getDataElement().getName()},
                            {"dataObjectBusinessKey", orphanedCColumn.getDataElement().getDataObject().getBusinessKey()},
                            {"trackingId", orphanedCColumn.getTrackingId()},
                        }))
                	.collect(Collectors.toList())},
                })));

        return response;
    }

    /**
     * Set the breadcrumb and title for the response.
     *
     * @param etk entellitrak execution context
     * @param response the response
     */
    private static void setBreadcrumbAndTitle(final PageExecutionContext etk, final TextResponse response) {
        try {
            final String url = String.format("page.request.do?page=du.page.cleanOrphanedFiles&csrfToken=%s",
                    URLEncoder.encode(etk.getCSRFToken(), StandardCharsets.UTF_8.name()));

            BreadcrumbUtility.setBreadcrumbAndTitle(response,
                    BreadcrumbUtility.addLastChildFluent(
                            DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                            new SimpleBreadcrumb("Clean Orphaned Files",
                                    url)));
        } catch (final UnsupportedEncodingException e) {
            throw new GeneralRuntimeException(e);
        }
    }
}
