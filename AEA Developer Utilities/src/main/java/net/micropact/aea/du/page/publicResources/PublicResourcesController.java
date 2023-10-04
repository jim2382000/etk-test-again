package net.micropact.aea.du.page.publicResources;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.Script;
import com.entellitrak.configuration.Workspace;
import com.entellitrak.configuration.WorkspaceService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.ScriptObjectLanguageType;
import net.micropact.aea.utility.Utility;

/**
 * Controller code for a page which displays the public resource in the system.
 *
 * @author Zachary.Miller
 */
@HandlerScript(type = PageController.class)
public class PublicResourcesController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final WorkspaceService workspaceService = etk.getWorkspaceService();

        final TextResponse response = etk.createTextResponse();

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
                BreadcrumbUtility.addLastChildFluent(
                        DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                        new SimpleBreadcrumb("Public Resources",
                                "page.request.do?page=du.page.publicResources")));

        final Workspace activeWorkspace = workspaceService.getActiveWorkspace();

        final List<Map<String, Object>> scriptObjects = workspaceService.getScripts(activeWorkspace)
                .stream()
                .filter(Script::isPublicResource)
                .map(script -> {
                    final ScriptObjectLanguageType scriptObjectLanguageType = ScriptObjectLanguageType.getByLanguageType(script.getLanguageType());

                    return Utility.arrayToMap(String.class, Object.class, new Object[][] {
                        {"FULLY_QUALIFIED_SCRIPT_NAME", script.getFullyQualifiedName()},
                        {"scriptObjectLanguageType", scriptObjectLanguageType},
                        {"languageTypeDisplay", scriptObjectLanguageType.getName()}
                    });
                })
                .collect(Collectors.toList());

        response.put("scriptObjects", new Gson().toJson(scriptObjects));

        return response;
    }
}
