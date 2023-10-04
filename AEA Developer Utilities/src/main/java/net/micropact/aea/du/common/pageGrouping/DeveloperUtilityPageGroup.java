package net.micropact.aea.du.common.pageGrouping;

import java.util.List;

/**
 * An enum representing a group of developer utility pages.
 *
 * @author Zachary.Miller
 */
public enum DeveloperUtilityPageGroup {

    /** COMMON. */
    COMMON("Common", 1),
    /** EXPLORATION. */
    EXPLORATION("Exploration", 1),
    /** DIAGNOSTIC. */
    DIAGNOSTIC("Diagnostic", 2),
    /** TASKS. */
    TASKS("Tasks", 2),
    /** UNUSED_ITEMS. */
    UNUSED_ITEMS("Unused Items", 2),
    /** TRANSFER_OWNERSHIP. */
    TRANSFER_OWNERSHIP("Transfer Ownership", 3),
    /** CACHE. */
    CACHE("Cache", 3),
    /** LOOKUPS. */
    LOOKUPS("Lookups", 3),
    /** MAINTENANCE. */
    MAINTENANCE("Maintenance", 3);

    private final String name;

    private final int column;

    /**
     * Simple constructor.
     *
     * @param theName the name
     * @param theColumn the column
     */
    DeveloperUtilityPageGroup(final String theName, final int theColumn){
        name = theName;
        column = theColumn;
    }

    /**
     * Get the display name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the column.
     *
     * @return the column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Get the pages in the group.
     *
     * @return the pages
     */
    public List<DeveloperUtilityPage> getDeveloperUtilityPages() {
        return DeveloperUtilityPage.getDeveloperUtilityPagesInGroup(this);
    }
}