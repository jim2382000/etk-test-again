package net.micropact.aea.du.page.logViewer;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

/**
 * Controller code for the Log Viewer.
 *
 * @author Zachary.Miller
 */
@HandlerScript(type = PageController.class)
public class LogViewerController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        PageUtility.setAEACacheHeaders(etk, response);

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
                BreadcrumbUtility.addLastChildFluent(
                        DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                        new SimpleBreadcrumb("Log Viewer",
                                "page.request.do?page=du.page.logViewer")));

        return response;
    }
}
