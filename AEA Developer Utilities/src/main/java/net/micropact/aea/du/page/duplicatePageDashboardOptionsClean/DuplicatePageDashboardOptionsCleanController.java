package net.micropact.aea.du.page.duplicatePageDashboardOptionsClean;

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
 * This serves as the Controller Code for a Page which removes duplicate Page Dashboard Options for users.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class DuplicatePageDashboardOptionsCleanController implements PageController {

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
     * Removes duplicate Page Dashboard Options from the database.
     *
     * @param etk entellitrak execution context
     */
    private static void deleteDuplicateDashboardOptions(final ExecutionContext etk) {
        etk.createSQL("DELETE FROM etk_page_dashboard_option WHERE page_dashboard_option_id IN (SELECT page_dashboard_option_id FROM etk_page_dashboard_option pdoToDelete WHERE EXISTS (SELECT * FROM etk_page_dashboard_option pdoToKeep WHERE pdotokeep.page_business_key = pdotodelete.page_business_key AND pdoToKeep.user_id = pdoToDelete.user_id AND pdoToKeep.page_dashboard_option_id > pdoToDelete.page_dashboard_option_id))")
            .execute();
    }
}
