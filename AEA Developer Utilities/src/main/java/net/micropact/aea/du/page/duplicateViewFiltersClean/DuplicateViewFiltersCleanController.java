package net.micropact.aea.du.page.duplicateViewFiltersClean;

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
 * This serves as the Controller Code for a Page which removes duplicate View Filters for users.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class DuplicateViewFiltersCleanController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        response.setContentType(ContentType.JSON);

        PageUtility.validateCsrfToken(etk);

        deleteDuplicateViewFilters(etk);

        response.put("out", "1");

        return response;
    }

    /**
     * Removes duplicate View Filters from the database.
     *
     * @param etk entellitrak execution context
     */
    private static void deleteDuplicateViewFilters(final ExecutionContext etk) {
        etk.createSQL("DELETE FROM etk_view_filter WHERE filter_id IN ( SELECT filter_id FROM etk_view_filter viewFilterToDelete WHERE EXISTS ( SELECT * FROM etk_view_filter viewFilterToKeep WHERE viewFilterToKeep.data_view_key = viewFilterToDelete.data_view_key AND viewFilterToKeep.user_id = viewFilterToDelete.user_id AND viewFilterToKeep.filter_id > viewFilterToDelete.filter_id))")
            .execute();
    }
}
