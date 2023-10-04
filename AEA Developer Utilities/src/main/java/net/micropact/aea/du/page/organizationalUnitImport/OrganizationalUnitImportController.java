package net.micropact.aea.du.page.organizationalUnitImport;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.FileStream;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.entellitrak.aea.du.DuServiceFactory;
import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.du.page.organizationalUnitExport.OrganizationalUnitExportController;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/**
 * This page ingests a JSON representation of user-entered information regarding the Organizational Unit.
 *
 * @author zmiller
 * @see OrganizationalUnitExportController
 */
@HandlerScript(type = PageController.class)
public class OrganizationalUnitImportController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk)
        throws ApplicationException {
        final Parameters parameters = etk.getParameters();

        final boolean update = "1".equals(parameters.getSingle("update"));
        Boolean importCompleted = false;
        List<Map<String, Object>> undeletedNodes = null;

        final TextResponse response = etk.createTextResponse();

        setBreadcrumb(response);

        final List<String> errors = new ArrayList<>();

        if (update) {
            PageUtility.validateCsrfToken(etk);

            final FileStream fileParameter = parameters.getFile("importFile");

            if (fileParameter == null) {
                errors.add("You must upload a file");
            } else {
                try (InputStream fileStream = fileParameter.getInputStream()) {
                    undeletedNodes = DuServiceFactory.getOrganizationalHierarchyMigrationService(etk)
                        .importFromStream(fileStream)
                        .getUnmatchedNodes()
                        .stream()
                        .map(orgUnit -> {
                            try {
                                final String nodeCode = orgUnit.getCode();
                                final Long hierarchyId = etk
                                    .createSQL("SELECT HIERARCHY_ID FROM etk_hierarchy WHERE code = :nodeCode")
                                    .setParameter("nodeCode", nodeCode)
                                    .fetchLong();

                                return Utility.arrayToMap(String.class, Object.class, new Object[][] {
                                    { "HIERARCHY_ID", hierarchyId },
                                    { "CODE", nodeCode },
                                    { "NAME", orgUnit.getName() },
                                });
                            } catch (final IncorrectResultSizeDataAccessException e) {
                                throw new GeneralRuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList());
                    importCompleted = true;
                } catch (final Exception e) {
                    throw new GeneralRuntimeException(e);
                }
            }
        }

        final Gson gson = new Gson();
        response.put("errors", gson.toJson(errors));
        response.put("undeletedNodes", gson.toJson(undeletedNodes));
        response.put("importCompleted", gson.toJson(importCompleted));
        response.put("csrfToken", gson.toJson(etk.getCSRFToken()));

        return response;
    }

    /**
     * Set the breadcrumb for the response.
     *
     * @param response
     *            the response
     */
    private static void setBreadcrumb(final TextResponse response) {
        BreadcrumbUtility.setBreadcrumbAndTitle(response,
            BreadcrumbUtility.addLastChildFluent(
                DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                new SimpleBreadcrumb(
                    "Hierarchy Import/Export",
                    "page.request.do?page=du.page.organizationalUnitImport")));
    }
}
