package net.micropact.aea.du.utility;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;

import net.micropact.aea.core.query.Coersion;
import net.micropact.aea.core.query.DatabaseSequence;
import net.micropact.aea.core.query.QueryUtility;
import net.micropact.aea.utility.Utility;

/**
 * Utility class for working with shared object permissions.
 *
 * @author Zachary.Miller
 */
public final class SharedObjectPermissionUtility {

    /**
     * Utility classes do not need public constructors.
     */
    private SharedObjectPermissionUtility() {
    }

    /**
     * Get the shared object permission ids which appear to be orphaned. Currently this is shared object permissions
     * which are not associated with a specific permission such as page permission.
     *
     * @param etk
     *            entellitrak execution context
     * @return the shared object permission ids
     */
    public static List<Long> getOrphanedSharedObjectPermissionIds(final ExecutionContext etk) {
        return QueryUtility.mapsToLongs(etk.createSQL(
            "SELECT shared_object_permission_id FROM etk_shared_object_permission WHERE shared_object_permission_id NOT IN(SELECT page_permission_id FROM etk_page_permission UNION SELECT report_permission_id FROM etk_report_permission UNION SELECT search_permission_id FROM etk_search_permission)")
            .fetchList())
            .stream()
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * Delete the orphaned shared object permissions.
     *
     * @param etk
     *            entellitrak execution context
     */
    public static void deletedOrphanedSharedObjectPermissions(final ExecutionContext etk) {
        getOrphanedSharedObjectPermissionIds(etk)
            .forEach(sharedObjectPermissionId -> deleteSharedObjectPermissionById(etk, sharedObjectPermissionId));
    }

    /**
     * Delete a shared object permission id. Does not delete the child permission such as page permission.
     *
     * @param etk
     *            entellitrak execution context
     * @param sharedObjectPermissionId
     *            the shared object permission id
     */
    private static void deleteSharedObjectPermissionById(final ExecutionContext etk,
        final long sharedObjectPermissionId) {
        etk.createSQL(
            "DELETE FROM etk_shared_object_permission WHERE shared_object_permission_id = :sharedObjectPermissionId")
            .setParameter("sharedObjectPermissionId", sharedObjectPermissionId)
            .execute();
    }

    /**
     * Insert a record into etk_shared_object_permission. <strong>Does no error checking or updating of existing
     * rows.</strong>
     *
     * @param etk
     *            entellitrak execution context
     * @param isEdit
     *            whether the permission is edit
     * @param isExecute
     *            whether the permission is execute
     * @param isDisplay
     *            whether the permission is display
     * @param subjectId
     *            the subject id
     * @param roleId
     *            the role id
     * @param isAllUsers
     *            whether the permission is all users
     * @return the shared object permission id
     */
    public static long insertSharedObjectPermission(final ExecutionContext etk,
        final boolean isEdit,
        final boolean isExecute,
        final boolean isDisplay,
        final Long subjectId,
        final Long roleId,
        final boolean isAllUsers) {
        final long sharedObjectPermissionId;

        final Map<String, Object> insertQueryParameters = Utility.arrayToMap(String.class, Object.class,
            new Object[][] {
                { "is_edit", Coersion.toLong(isEdit) },
                { "is_execute", Coersion.toLong(isExecute) },
                { "is_display", Coersion.toLong(isDisplay) },
                { "subject_id", subjectId },
                { "role_id", roleId },
                { "is_all_users", Coersion.toLong(isAllUsers) },
            });

        if (Utility.isSqlServer(etk)) {
            sharedObjectPermissionId = etk.createSQL(
                "INSERT INTO etk_shared_object_permission(is_edit, is_execute, is_display, subject_id, role_id, is_all_users) VALUES (:is_edit, :is_execute, :is_display, :subject_id, :role_id, :is_all_users)")
                .setParameter(insertQueryParameters)
                .execute("shared_object_permission_id");
        } else if (Utility.isPostgreSQL(etk)) {
            sharedObjectPermissionId = etk.createSQL(
                "INSERT INTO etk_shared_object_permission(is_edit, is_execute, is_display, subject_id, role_id, is_all_users) VALUES (:is_edit, :is_execute, :is_display, :subject_id, :role_id, :is_all_users) returning shared_object_permission_id")
                .setParameter(insertQueryParameters)
                .execute("shared_object_permission_id");
        } else {
            sharedObjectPermissionId = DatabaseSequence.HIBERNATE_SEQUENCE.getNextVal(etk);

            etk.createSQL(
                "INSERT INTO etk_shared_object_permission(shared_object_permission_id, is_edit, is_execute, is_display, subject_id, role_id, is_all_users) VALUES (:shared_object_permission_id, :is_edit, :is_execute, :is_display, :subject_id, :role_id, :is_all_users)")
                .setParameter(insertQueryParameters)
                .setParameter("shared_object_permission_id", sharedObjectPermissionId)
                .execute();
        }

        return sharedObjectPermissionId;
    }
}
