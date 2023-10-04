package net.micropact.aea.oAuth.page.getActiveWorkspaceName;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.Workspace;
import com.entellitrak.configuration.WorkspaceService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;

import net.micropact.aea.oAuth.util.EntellitrakOAuthPageUtility;

/**
 * Controller code for a page which gets the user's active workspace name.
 * This could be an endpoint, but it is complicated because endpoints always claim to
 * execute from the "system".
 *
 * @author Zachary.Miller
 */
@HandlerScript(type = PageController.class)
public class GetActiveWorkspaceNameController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final WorkspaceService workspaceService = etk.getWorkspaceService();
        final Workspace activeWorkspace = workspaceService.getActiveWorkspace();

        final TextResponse response = etk.createTextResponse();
        response.setContentType("text/plain");

        EntellitrakOAuthPageUtility.setAEACacheHeadersPrivate(etk, response);

        final String workspaceName = activeWorkspace.getName();

        response.put("out", workspaceName);

        return response;
    }
}
