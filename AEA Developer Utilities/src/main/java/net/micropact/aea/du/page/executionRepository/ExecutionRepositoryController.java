package net.micropact.aea.du.page.executionRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.WorkspaceService;
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
 * This is for a {@link PageController} which displays information about users who are not using the System Repository
 * as their Execution Repository.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class ExecutionRepositoryController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        try {
        	final WorkspaceService workspaceService = etk.getWorkspaceService();

        	final TextResponse response = etk.createTextResponse();

            BreadcrumbUtility.setBreadcrumbAndTitle(response,
                BreadcrumbUtility.addLastChildFluent(
                    DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                    new SimpleBreadcrumb("Execution Repository",
                        "page.request.do?page=du.page.executionRepository")));

			final String systemWorkspaceName = workspaceService.getSystemWorkspace().getName();

            final List<Map<String, Object>> users = etk.createSQL("SELECT u.user_id user_id, u.username username, u.type_of_user type_of_user, workspace.workspace_revision workspace_revision FROM etk_user u JOIN etk_workspace workspace ON workspace.user_id = u.user_id JOIN etk_development_preferences developmentPreferences ON developmentPreferences.development_preferences_id = u.development_preferences_id WHERE developmentPreferences.current_workspace != :systemWorkspaceName ORDER BY username, user_id")
            .setParameter("systemWorkspaceName", systemWorkspaceName)
            .fetchList()
            .stream()
            .map(userInfo -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
				{"user_id", userInfo.get("user_id")},
				{"username", userInfo.get("username")},
				{"type_of_user", userInfo.get("type_of_user")},
				{"workspace_revision", userInfo.get("workspace_revision")},

			}))
            .collect(Collectors.toList());

            final String systemWorkspaceRevision = etk.createSQL("SELECT workspace_revision FROM etk_workspace WHERE user_id IS NULL")
            		.fetchString();

            final Gson gson = new Gson();
            response.put("users", gson.toJson(users));
            response.put("systemWorkspaceRevision", gson.toJson(systemWorkspaceRevision));

            return response;
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new ApplicationException(e);
        }
    }
}
