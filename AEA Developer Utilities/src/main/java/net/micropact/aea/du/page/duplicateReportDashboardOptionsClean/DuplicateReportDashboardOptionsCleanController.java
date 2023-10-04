package net.micropact.aea.du.page.duplicateReportDashboardOptionsClean;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.ContentType;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;

import net.micropact.aea.core.pageUtility.PageUtility;

/**
 * This serves as the Controller Code for a Page which removes duplicate Report Dashboard Options for users.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class DuplicateReportDashboardOptionsCleanController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        response.setContentType(ContentType.JSON);

        PageUtility.validateCsrfToken(etk);

        deleteDuplicateDashboardOptions(etk);

        response.put("out", "1");

        return response;
    }

    /**
     * Removes duplicate Report Dashboard Options from the database.
     *
     * @param etk entellitrak execution context
     */
    private static void deleteDuplicateDashboardOptions(final ExecutionContext etk) {
        etk.createSQL("DELETE FROM etk_report_dashboard_option WHERE report_dashboard_option_id IN ( SELECT report_dashboard_option_id FROM etk_report_dashboard_option rdoToDelete WHERE EXISTS ( SELECT * FROM etk_report_dashboard_option rdoToKeep WHERE rdoToKeep.saved_report_id = rdoToDelete.saved_report_id AND rdoToKeep.user_id = rdoToDelete.user_id AND rdoToKeep.report_dashboard_option_id > rdoToDelete.report_dashboard_option_id ) )")
            .execute();
    }
}
