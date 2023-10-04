package net.entellitrak.aea.gl.api.etk;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;
import com.entellitrak.OrganizationInfo;

import net.entellitrak.aea.gl.api.etk.sql.QueryParameterUtil;
import net.entellitrak.aea.gl.api.etk.sql.QueryResultUtil;

/**
 * Utility class for dealing with entellitrak hierarchy.
 *
 * @author Zachary.Miller
 */
public final class HierarchyUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private HierarchyUtil() {
    }

    /**
     * Get the etk_hierarchy ids corresponding to organization infos.
     *
     * @param etk entellitrak execution context
     * @param organizationInfos the organization infos
     * @return the hierarchy ids
     */
    public static Set<Long> getHierarchyIdsForOrganizationInfos(final ExecutionContext etk, final Collection<OrganizationInfo> organizationInfos) {
        final List<Long> nodeIds = organizationInfos.stream()
                .map(OrganizationInfo::getNodeId)
                .collect(Collectors.toList());

        return new HashSet<>(QueryResultUtil.convertToListOfLongs(etk.createSQL("SELECT HIERARCHY_ID FROM etk_hierarchy WHERE node_id IN (:nodeIds)")
                .setParameter("nodeIds", QueryParameterUtil.toNonEmptyParameterList(nodeIds))
                .fetchList()));
    }
}
