package net.entellitrak.aea.gl.api.etk;

import java.util.Collection;

import com.entellitrak.ExecutionContext;
import com.entellitrak.group.Group;
import com.entellitrak.group.GroupService;
import com.entellitrak.user.Role;
import com.entellitrak.user.User;

/**
 * Class containing utility functionality related to roles.
 *
 * @author Zachary.Miller
 */
public final class RoleUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private RoleUtil() {}

    /**
     * Determine whether the current user has a role.
     *
     * @param etk entellitrak execution context
     * @param role the role
     * @return whether the current user is in that role
     */
    public static boolean doesCurrentUserHaveRole(final ExecutionContext etk, final Role role) {
        final User currentUser = etk.getCurrentUser();

        return doesUserHaveRole(etk, currentUser, role);
    }

    /**
     * Determine whether a user has a role.
     * This checks all roles, not just the current or default.
     *
     * @param etk entellitrak execution context
     * @param user the user
     * @param role the role
     * @return whether the current user is in that role
     */
    public static boolean doesUserHaveRole(final ExecutionContext etk, final User user, final Role role) {
        final GroupService groupService = etk.getGroupService();

        /* We check for the user's roles first for efficiency since
         * at the time of the writing, the group checks have to query the DB. */
        if(user.getRoles().contains(role)) {
            return true;
        } else {
            final Collection<Group> groups = GroupUtil.getGroupsUserIsAMemberOf(etk, user);

            return groups.stream()
                    .anyMatch(group -> groupService.getRoles(group).contains(role));
        }
    }
}
