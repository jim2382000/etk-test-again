package net.micropact.aea.du.page.duplicatePageDashboardOptions;

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
 * This serves as the Controller Code for a Page which displays duplicate Page Dashboard Options.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class DuplicatePageDashboardOptionsController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
                BreadcrumbUtility.addLastChildFluent(
                        DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                        new SimpleBreadcrumb("Duplicate Page Dashboard Options",
                                "page.request.do?page=du.page.duplicatePageDashboardOptions")));

        final List<Map<String, Object>> duplicateRecords = etk.createSQL("SELECT u.username USERNAME, page.business_key, COUNT(*) COUNT FROM etk_page_dashboard_option dashboardOption LEFT JOIN etk_user u ON u.user_id = dashboardOption.user_id LEFT JOIN etk_page page ON page.business_key = dashboardoption.page_business_key GROUP BY USERNAME, BUSINESS_KEY HAVING COUNT(*) > 1 ORDER BY COUNT, USERNAME, BUSINESS_KEY")
                .fetchList()
                .stream()
                .map(duplicateRecord -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
                	{"username", duplicateRecord.get("username")},
                	{"page", duplicateRecord.get("business_key")},
                	{"count", duplicateRecord.get("count")}
                }))
                .collect(Collectors.toList());

		response.put("duplicateRecords", new Gson().toJson(duplicateRecords));

        response.put("csrfToken", new Gson().toJson(etk.getCSRFToken()));

        return response;
    }
}
