package net.micropact.aea.du.page.orphanedSharedObjectPermissionsClean;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.ContentType;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;

import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.du.utility.SharedObjectPermissionUtility;

/**
 * Controller code for a page which deletes orphaned shared object permissions.
 *
 * @author Zachary.Miller
 */
@HandlerScript(type = PageController.class)
public class OrphanedSharedObjectPermissionsCleanController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        response.setContentType(ContentType.JSON);

        PageUtility.validateCsrfToken(etk);

        SharedObjectPermissionUtility.deletedOrphanedSharedObjectPermissions(etk);

        response.put("out", "1");

        return response;
    }
}
