package net.entellitrak.aea.gl.api.etk;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.group.Group;
import com.entellitrak.group.GroupService;
import com.entellitrak.platform.DatabasePlatform;
import com.entellitrak.user.User;

import net.entellitrak.aea.gl.api.etk.sql.QueryResultUtil;
import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * Utility class for dealing with entellitrak groups.
 *
 * @author Zachary.Miller
 */
public final class GroupUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private GroupUtil() {
    }

    /**
     * Determine whether the current user is in a group.
     *
     * @param etk entellitrak execution context
     * @param group the group
     * @return whether the user is a member of the group
     */
    public static boolean isCurrentUserInGroup(final ExecutionContext etk, final Group group) {
        return isUserInGroup(etk, etk.getCurrentUser(), group);
    }

    /**
     * Determine whether a user is a member of a particular group.
     *
     * @param etk entellitrak execution context
     * @param user the user
     * @param group the group
     * @return whether the user is in the group
     */
    public static boolean isUserInGroup(final ExecutionContext etk, final User user, final Group group) {
        try {
            return 1 == etk.createSQL(etk.getPlatformInfo().getDatabasePlatform() == DatabasePlatform.ORACLE
                    ? "SELECT CASE WHEN EXISTS (SELECT * FROM etk_group g JOIN etk_user_group_assoc userGroupAssoc ON userGroupAssoc.group_id = g.group_id WHERE g.business_key = :groupBusinessKey AND userGroupAssoc.user_id = :userId) THEN 1 ELSE 0 END FROM DUAL"
                            : "SELECT CASE WHEN EXISTS ( SELECT * FROM etk_group g JOIN etk_user_group_assoc userGroupAssoc ON userGroupAssoc.group_id = g.group_id WHERE g.business_key =:groupBusinessKey AND userGroupAssoc.user_id =:userId ) THEN 1 ELSE 0 END")
                    .setParameter("groupBusinessKey", group.getBusinessKey())
                    .setParameter("userId", user.getId())
                    .fetchLong();
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Get the groups that a user is a member of.
     *
     * @param etk entellitrak execution context
     * @param user the user
     * @return the groups the user is in
     */
    public static Collection<Group> getGroupsUserIsAMemberOf(final ExecutionContext etk, final User user){
        final GroupService groupService = etk.getGroupService();

        final long userId = user.getId();

        final List<String> groupBusinessKeys = QueryResultUtil.convertToListOfStrings(etk.createSQL("SELECT g.business_key FROM etk_user_group_assoc userGroupAssoc JOIN etk_group g ON g.group_id = userGroupAssoc.group_id WHERE userGroupAssoc.user_id = :userId")
                .setParameter("userId", userId)
                .fetchList());

        return groupBusinessKeys.stream()
                .map(groupService::getGroup)
                .collect(Collectors.toList());
    }
}
