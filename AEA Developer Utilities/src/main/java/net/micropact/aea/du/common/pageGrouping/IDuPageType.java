package net.micropact.aea.du.common.pageGrouping;

import com.entellitrak.menu.MenuExecutionContext;
import com.entellitrak.menu.MenuItem;

/**
 * An interface representing the links to developer utility pages.
 * The reason that this is needed is due to the fact that all developer utilities are pages
 * with the exception of the link to the javadoc. These two types of objects need completely
 * different menu items.
 *
 * @author Zachary.Miller
 */
public interface IDuPageType {

    /**
     * Get the menu item.
     *
     * @param etk entellitrak execution context
     * @param developerUtilityPage the developer utility page
     * @return the menu item
     */
    MenuItem getMenuItem(MenuExecutionContext etk, DeveloperUtilityPage developerUtilityPage);

    /**
     * Get the URL.
     *
     * @return the URL
     */
    String getUrl();
}
