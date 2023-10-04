package net.micropact.aea.du.page.orphanedSharedObjectPermissions;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.utility.SharedObjectPermissionUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

/**
 * This serves as the Controller Code for a Page which displays orphaned shared object permissions.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class OrphanedSharedObjectPermissionsController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
                BreadcrumbUtility.addLastChildFluent(DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                        new SimpleBreadcrumb("Orphaned Shared Object Permissions",
                                "page.request.do?page=du.page.orphanedSharedObjectPermissions")));

        response.put("orphanedSharedObjectPermissionIds",
                new Gson().toJson(SharedObjectPermissionUtility.getOrphanedSharedObjectPermissionIds(etk)));
        response.put("csrfToken", new Gson().toJson(etk.getCSRFToken()));

        return response;
    }
}
