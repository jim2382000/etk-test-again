package net.entellitrak.aea.gl.api.etk;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;

import com.entellitrak.ExecutionContext;
import com.entellitrak.OrganizationInfo;
import com.entellitrak.OrganizationTree;
import com.entellitrak.RoleService;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.dynamic.DataObjectInstance;
import com.entellitrak.permission.DataPermissionType;
import com.entellitrak.permission.DataPermissions;
import com.entellitrak.permission.PermissionAccessLevelType;
import com.entellitrak.user.User;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * Utility class for dealing with hierarchy.
 *
 * @author Zachary.Miller
 */
public final class OrganizationInfoUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private OrganizationInfoUtil() {
    }

    /**
     * Get the organization infos that the current user has access to for a given object for a given data permission.
     *
     * @param etk entellitrak execution context
     * @param dynamicClass the dynamic class
     * @param dataPermissionType the data permission type
     * @return the organization infos that the current user has access to in that role for that object
     */
    public static Set<OrganizationInfo> getOrganizationInfosAccesibleByCurrentUserRoleForObject(
            final ExecutionContext etk,
            final Class<? extends DataObjectInstance> dynamicClass,
            final DataPermissionType dataPermissionType) {
        final DataObject dataObject = DataObjectUtil.getDataObjectByDynamicClass(etk, dynamicClass);
        final RoleService roleService = etk.getRoleService();
        final User currentUser = etk.getCurrentUser();
        final OrganizationTree organizationTree = etk.getOrganizationTree();

        final DataPermissions dataPermissions = roleService.getDataObjectPermissions(currentUser.getRole(), dataObject);

        final PermissionAccessLevelType accessLevel = dataPermissions.getAccessLevel(dataPermissionType);

        final Set<OrganizationInfo> accessibleOrgs;

        final SortedMap<Long, OrganizationInfo> orgTree = organizationTree.orgTree();

        final long hierarchyId = currentUser.getHierarchy().getId();

        switch(accessLevel) {
            case NONE:
                accessibleOrgs = Collections.emptySet();
                break;
            case USER:
                accessibleOrgs = Collections.emptySet();
                break;
            case ORGANIZATIONAL_UNIT:
                accessibleOrgs = new HashSet<>(Arrays.asList(organizationTree.getOrganizationInfoByHierarchyId(hierarchyId)));
                break;
            case CHILD_ORGANIZATION_UNITS:
                final OrganizationInfo orgUnit = organizationTree.getOrganizationInfoByHierarchyId(hierarchyId);
                accessibleOrgs = getOrganizationInfoSubTree(etk, orgUnit);
                break;
            case GLOBAL:
                accessibleOrgs = new HashSet<>(orgTree.values());
                break;
            default:
                throw new GeneralRuntimeException(String.format("Do not know how to handle access level: %s", accessLevel));
        }

        return accessibleOrgs;
    }

    /**
     * Get a subtree (including the current node) of a given {@link OrganizationInfo}.
     *
     * @param etk entellitrak execution context
     * @param organizationInfo the organization info
     * @return the subtree
     */
    public static Set<OrganizationInfo> getOrganizationInfoSubTree(final ExecutionContext etk, final OrganizationInfo organizationInfo) {
        final OrganizationTree organizationTree = etk.getOrganizationTree();

        final SortedMap<Long, OrganizationInfo> orgTree = organizationTree.getOrgTree();

        return new HashSet<>(orgTree.subMap(organizationInfo.getNodeId(), organizationInfo.getMaxChildId() + 1)
                .values());
    }
}
