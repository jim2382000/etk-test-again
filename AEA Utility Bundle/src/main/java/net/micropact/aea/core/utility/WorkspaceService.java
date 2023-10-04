package net.micropact.aea.core.utility;

import java.util.Date;

import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.user.User;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.wrappedAPIs.WorkspaceServiceWrapper;
import net.micropact.aea.utility.Utility;

/**
 * This class is currently not used by components, but is being kept around
 * to make it easier for existing entellitrak sites to upgrade. They will
 * not need to manually delete this file.
 * <p>
 *  This class contains methods for dealing with the core classes related to
 *  Script Object Workspaces.
 * </p>
 *
 * @author Zachary.Miller
 */
public final class WorkspaceService {

    /**
     * Utility classes do not need public constructors.
     */
    private WorkspaceService() {
    }

    /**
     * Publish bundle workspace changes.
     * This should be called after changes are made to the system repository.
     *
     * @param etk entellitrak execution context
     */
    public static void publishWorkspaceChanges(final ExecutionContext etk) {
        try {
            final User currentUser = etk.getCurrentUser();

            etk.createSQL("UPDATE etk_workspace SET workspace_revision = workspace_revision + 1, last_updated_by = :last_updated_by, last_updated_on = :last_updated_on WHERE workspace_id = :workspaceId")
            .setParameter("last_updated_by", currentUser.getAccountName())
            .setParameter("last_updated_on", new Date())
            .setParameter("workspaceId", Utility.getSystemRepositoryWorkspaceId(etk))
            .execute();

            WorkspaceServiceWrapper.publishWorkspaceChanges();
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }

    }
}
