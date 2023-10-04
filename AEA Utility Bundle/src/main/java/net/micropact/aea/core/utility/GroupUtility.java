package net.micropact.aea.core.utility;

import java.util.Collection;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;
import com.entellitrak.group.Group;
import com.entellitrak.group.GroupService;
import com.entellitrak.user.User;
import com.entellitrak.user.UserType;

/**
 * Utility for functionality related to entellitrak groups.
 *
 * @author Zachary.Miller
 */
public final class GroupUtility {

    /**
     * Utility classes do not need public constructors.
     */
    private GroupUtility() {}

    /**
     * Get the active users which are members of a particular group.
     *
     * @param etk entellitrak execution context
     * @param group the group
     * @return the users
     */
    public static Collection<User> getActiveUsersInGroup(final ExecutionContext etk, final Group group){
        final GroupService groupService = etk.getGroupService();

        return groupService.getUsers(group)
                .stream()
                .filter(user -> UserType.ACTIVE == user.getAuthentication().getTypeOfUser())
                .collect(Collectors.toList());
    }
}
