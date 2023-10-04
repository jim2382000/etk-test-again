package net.entellitrak.aea.du.service.organizationalHierarchy;

import java.util.Collection;

import com.entellitrak.OrganizationInfo;

/**
 * This interface represents the result of performing an organizational hierarchy import.
 *
 * @author Zachary.Miller
 */
public interface IOrganizationalHierarchyImportResult {

    /**
     * Get the hierarchy nodes which existed in the destination site, but did not exist in the export file.
     *
     * @return the code of the unmatched nodes
     */
    Collection<OrganizationInfo> getUnmatchedNodes();
}
