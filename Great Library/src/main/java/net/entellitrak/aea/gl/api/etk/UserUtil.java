package net.entellitrak.aea.gl.api.etk;

import java.util.Collection;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;
import com.entellitrak.UserService;
import com.entellitrak.group.Group;
import com.entellitrak.group.GroupService;
import com.entellitrak.user.Role;
import com.entellitrak.user.User;

import net.entellitrak.aea.gl.api.etk.sql.QueryResultUtil;

/**
 * Utility for dealing with entellitrak {@link User}.
 *
 * @author Zachary.Miller
 */
public final class UserUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private UserUtil() {
    }

    /**
     * Get the users which are members of a particular group.
     *
     * @param etk entellitrak execution context
     * @param group the group
     * @return the users
     *
     * @deprecated use {@link GroupService#getUsers(Group)} instead
     */
    @Deprecated(forRemoval = true)
    public static Collection<User> getUsersInGroup(final ExecutionContext etk, final Group group){
        return etk.getGroupService().getUsers(group);
    }

    /**
     * Get all users which have a particular role.
     *
     * @param etk entellitrak execution context
     * @param role the role
     * @return the users with that role
     */
    public static Collection<User> getUsersWithRole(final ExecutionContext etk, final Role role){
        final UserService userService = etk.getUserService();

        return QueryResultUtil.convertToListOfLongs(etk.createSQL("SELECT u.user_id FROM etk_subject_role subjectRole JOIN etk_user u ON u.user_id = subjectRole.subject_id WHERE subjectRole.role_id = :roleId UNION SELECT userGroupAssoc.user_id FROM etk_user_group_assoc userGroupAssoc JOIN etk_subject_role subjectRole ON subjectRole.subject_id = userGroupAssoc.group_id WHERE subjectRole.role_id = :roleId")
                .setParameter("roleId", role.getId())
                .fetchList())
            .stream()
            .map(userService::getUser)
            .collect(Collectors.toList());
    }
}
