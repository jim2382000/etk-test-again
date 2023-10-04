package net.micropact.aea.du.utility.orgUnit;

import java.util.Collection;

import com.entellitrak.OrganizationInfo;

import net.entellitrak.aea.du.service.organizationalHierarchy.IOrganizationalHierarchyImportResult;

/**
 * Implementation of {@link IOrganizationalHierarchyImportResult}.
 *
 * @author Zachary.Miller
 */
public class OrganizationalHierarchyImportResult implements IOrganizationalHierarchyImportResult {

    private final Collection<OrganizationInfo> unmatchedNodes;

    /**
     * Simple constructor.
     *
     * @param theUnmatchedNodes the code of the unmatched organizational hierarchy nodes
     */
    public OrganizationalHierarchyImportResult(final Collection<OrganizationInfo> theUnmatchedNodes) {
        unmatchedNodes = theUnmatchedNodes;
    }

    @Override
    public Collection<OrganizationInfo> getUnmatchedNodes() {
        return unmatchedNodes;
    }
}
