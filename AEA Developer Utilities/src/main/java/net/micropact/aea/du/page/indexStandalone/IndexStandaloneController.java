package net.micropact.aea.du.page.indexStandalone;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

/**
 * Controller code for the Developer Utility Index Standalone page.
 *
 * @author Zachary.Miller
 */
@HandlerScript(type = PageController.class)
public class IndexStandaloneController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        PageUtility.setAEACacheHeaders(etk, response);

        BreadcrumbUtility.setBreadcrumbAndTitle(response, DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb());

        return response;
    }
}
