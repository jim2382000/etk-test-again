package net.micropact.aea.du.utility;

import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.Script;
import com.entellitrak.configuration.Workspace;
import com.entellitrak.configuration.WorkspaceService;

/**
 * Utility class for generating default code templates.
 *
 * @author Zachary.Miller
 */
public final class DefaultScriptCodeGeneratorUtility {

    /**
     * Utility classes do not need public constructors.
     */
    private DefaultScriptCodeGeneratorUtility() {}

    /**
     * Get the default code to use for new page controller script objects.
     *
     * @param etk entellitrak execution context
     * @param packagePath the package path
     * @param className the class name
     * @return the code
     */
    public static String getDefaultPageControllerCode(
            final ExecutionContext etk,
            final String packagePath,
            final String className) {
        final WorkspaceService workspaceService = etk.getWorkspaceService();

        final Workspace workspace = workspaceService.getActiveWorkspace();

        final Script script = workspaceService.getScriptByFullyQualifiedName(workspace, "net.micropact.aea.du.utility.defaultCodeTemplates.DefaultPageController");

        final String template = workspaceService.getCode(workspace, script);

        return String.format(template, packagePath, className);
    }

    /**
     * Get the default code to use for new HTML script objects.
     *
     * @param etk entellitrak execution context
     * @return the code
     */
    public static String getDefaultHtmlCode(final ExecutionContext etk) {
        final WorkspaceService workspaceService = etk.getWorkspaceService();

        final Workspace workspace = workspaceService.getActiveWorkspace();

        final Script script = workspaceService.getScriptByFullyQualifiedName(workspace, "net.micropact.aea.du.utility.defaultCodeTemplates.DefaultHtml");

        return workspaceService.getCode(workspace, script);
    }

    /**
     * Get the default code to use for new javascript script objects.
     *
     * @param etk entellitrak execution context
     * @return the code
     */
    public static String getDefaultJavascriptCode(final ExecutionContext etk) {
        final WorkspaceService workspaceService = etk.getWorkspaceService();

        final Workspace workspace = workspaceService.getActiveWorkspace();

        final Script script = workspaceService.getScriptByFullyQualifiedName(workspace, "net.micropact.aea.du.utility.defaultCodeTemplates.DefaultJavascript");

        return workspaceService.getCode(workspace, script);
    }
}
