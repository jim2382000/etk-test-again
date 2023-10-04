package net.micropact.aea.du.page.duplicateViewFilters;

import java.util.List;
import java.util.Map;
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
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/**
 * This serves as the Controller Code for a Page which displays duplicate View Filters.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class DuplicateViewFiltersController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
                BreadcrumbUtility.addLastChildFluent(
                        DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                        new SimpleBreadcrumb("Duplicate View Filters",
                                "page.request.do?page=du.page.duplicateViewFilters")));

        final List<Map<String, Object>> duplicateRecords = etk.createSQL("SELECT u.username USERNAME, viewFilter.data_view_key DATA_VIEW, COUNT(*) COUNT FROM etk_view_filter viewFilter LEFT JOIN etk_user u ON u.user_id = viewFilter.user_id GROUP BY u.username, viewFilter.data_view_key HAVING COUNT(*) > 1 ORDER BY USERNAME, DATA_VIEW")
        .fetchList()
        .stream()
        .map(duplicateRecord -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
        	{"username", duplicateRecord.get("username")},
        	{"dataView", duplicateRecord.get("data_view")},
        	{"count", duplicateRecord.get("count")}
        }))
        .collect(Collectors.toList());

        response.put("duplicateRecords", new Gson().toJson(duplicateRecords));

        response.put("csrfToken", new Gson().toJson(etk.getCSRFToken()));

        return response;
    }
}
