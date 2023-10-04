package net.micropact.aea.du.utility;

import java.util.Map;
import java.util.Optional;

import com.entellitrak.ExecutionContext;
import com.entellitrak.RoleService;
import com.entellitrak.page.Page;
import com.entellitrak.user.Role;

import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.utility.Utility;

/**
 * Utility class for dealing with Page Permissions.
 *
 * @author Zachary.Miller
 */
public final class PagePermissionUtility {

    /**
     * Utility classes do not need public constructors.
     */
    private PagePermissionUtility() {}

    /**
     * Insert a new page permission (and shared object permission) giving all users execute.
     * <strong>Does not error checking or updating of an existing record.</strong>
     *
     * @param etk entellitrak execution context
     * @param page the page
     */
    public static void createAllUsersPagePermission(final ExecutionContext etk, final Page page) {
        final long pagePermissionId = SharedObjectPermissionUtility.insertSharedObjectPermission(etk, false, true, false, null, null, true);

        final Map<String, Object> parameters = Utility.arrayToMap(String.class, Object.class, new Object[][] {
            {"page_permission_id", pagePermissionId},
            {"page_id", PageUtility.getPageId(etk, page)},
        });

        etk.createSQL("INSERT INTO etk_page_permission(page_permission_id, page_id) VALUES(:page_permission_id, :page_id)")
            .setParameter(parameters)
            .execute();
    }

    /**
     * Create a page permission for the administrator role (if it exists).
     * <strong>Does not check to see if there is an existing record.</strong>
     *
     * @param etk entellitrak execution context
     * @param page the page
     */
    public static void createAdministratorPagePermission(final ExecutionContext etk, final Page page) {
        final RoleService roleService = etk.getRoleService();

        final Role administratorRole = roleService.getRoleByBusinessKey("role.administration");

        Optional.ofNullable(administratorRole).ifPresent(role -> {
            final long administratorRoleId = administratorRole.getId();

            final long pagePermissionId = SharedObjectPermissionUtility.insertSharedObjectPermission(etk, true, true, true, null, administratorRoleId, false);

            final Map<String, Object> parameters = Utility.arrayToMap(String.class, Object.class, new Object[][] {
                {"page_permission_id", pagePermissionId},
                {"page_id", PageUtility.getPageId(etk, page)},
            });

            etk.createSQL("INSERT INTO etk_page_permission(page_permission_id, page_id) VALUES(:page_permission_id, :page_id)")
                .setParameter(parameters)
                .execute();
        });
    }
}
