package net.micropact.aea.core.wrappedAPIs;

import com.micropact.entellitrak.config.SpringGlobalContext;
import com.micropact.entellitrak.workspace.service.WorkspaceIndexService;
import com.micropact.entellitrak.workspace.service.WorkspaceService;

/**
 * This class contains methods for dealing with the core classes related to
 * Script Object Workspaces.
 *
 * @author Zachary.Miller
 */
public final class WorkspaceServiceWrapper {

    /**
     * Utility classes do not need public constructors.
     */
    private WorkspaceServiceWrapper() {
    }

    /**
     * Call appropriate methods to indicate to core that the system repository has been modified without their knowledge.
     */
    public static void publishWorkspaceChanges() {
        final WorkspaceService workspaceService = getWorkspaceService();
        workspaceService.publishWorkspaceChanged(true);
        getWorkspaceIndexService().invalidateIndex(workspaceService.getSystemWorkspace());
    }

    /**
     * Acquire a WorkspaceService instance.
     * @return the core workspace service
     */
    public static WorkspaceService getWorkspaceService() {
        return SpringGlobalContext.getBean(WorkspaceService.class);
    }

    /**
     * Acquire a WorkspaceIndexService instance.
     * @return the core workspace index service
     */
    public static WorkspaceIndexService getWorkspaceIndexService() {
        return SpringGlobalContext.getBean(WorkspaceIndexService.class);
    }
}
