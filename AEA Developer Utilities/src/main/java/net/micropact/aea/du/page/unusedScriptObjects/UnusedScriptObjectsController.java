package net.micropact.aea.du.page.unusedScriptObjects;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.LanguageType;
import com.entellitrak.configuration.Script;
import com.entellitrak.configuration.Workspace;
import com.entellitrak.configuration.WorkspaceService;
import com.entellitrak.endpoints.EndpointHandler;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.menu.AppLauncherController;
import com.entellitrak.menu.MenuController;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.page.scriptObjectUsage.ScriptObjectUsageController;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

/**
 * Generates a list of Script Objects which do not appear to be being used in entellitrak. For instance, javascript
 * files which are not selected as form event listeners or form element listeners.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class UnusedScriptObjectsController implements PageController {

	// TODO: Remove this list once core provides a way to check their use via public API
	private static final Set<String> WHITE_LISTED_HANDLER_TYPES = Stream.of(
			AppLauncherController.class,
			MenuController.class
			)
			.map(Class::getName)
			.collect(Collectors.toSet());

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final WorkspaceService workspaceService = etk.getWorkspaceService();

		final TextResponse response = etk.createTextResponse();

		setBreadcrumbAndTitle(response);

		final Workspace workspace = workspaceService.getSystemWorkspace();

		final Set<String> usedScriptObjects = getUsedScriptObjects(etk);

		final List<Map<String, Object>> unusedScriptObjects = workspaceService.getScripts(workspace)
				.stream()
				.filter(scriptObject -> !isUsed(scriptObject, usedScriptObjects))
				.map((final Script script) -> Map.<String, Object>of(
						"language", script.getLanguageType(),
						"handler", script.getHandlerType().getClassName(),
						"fullyQualifiedName", script.getFullyQualifiedName()))
				.sorted(Comparator.comparing((final Map<String, Object> map) -> (LanguageType) map.get("language"))
						.thenComparing(map -> (String) map.get("handler"))
						.thenComparing(map -> (String) map.get("fullyQualifiedName")))
				.collect(Collectors.toList());

		response.put("unusedScriptObjects", new Gson().toJson(unusedScriptObjects));

		return response;
	}

	private static Set<String> getUsedScriptObjects(final ExecutionContext etk) {
		return ScriptObjectUsageController.getAllReferences(etk)
				.stream()
				.map(reference -> reference.getScript().getFullyQualifiedName())
				.collect(Collectors.toSet());
	}

	private static boolean isUsed(final Script script, final Set<String> usedScriptObjects) {
		if(usedScriptObjects.contains(script.getFullyQualifiedName())) {
			return true;
		}

		if(Objects.equals(LanguageType.CSS, script.getLanguageType())) {
			return true;
		}

		if(script.isPublicResource()) {
			return true;
		}

		if(Set.of(LanguageType.BEANSHELL, LanguageType.JAVA).contains(script.getLanguageType())
				&& script.getHandlerType().getClassName().isEmpty()) {
			return true;
		}

		if(Objects.equals(EndpointHandler.class.getName(),
				script.getHandlerType().getClassName())) {
			return true;
		}

		if(WHITE_LISTED_HANDLER_TYPES.contains(script.getHandlerType().getClassName())) {
			return true;
		}

		return false;
	}

	private static void setBreadcrumbAndTitle(final TextResponse response) {
		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Unused Script Objects",
								"page.request.do?page=du.page.unusedScriptObjects")));
	}
}
