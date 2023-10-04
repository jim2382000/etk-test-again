package net.entellitrak.aea.du.service;

import com.entellitrak.menu.MenuItem;

/**
 * This interface describes functionality related to dealing with {@link MenuItem} related to
 * the developer utility.
 *
 * @author Zachary.Miller
 */
public interface IMenuItemService {

    /**
     * Get a {@link MenuItem} for all of the developer utilites.
     * This item is intended to be added to the main menu.
     *
     * @return the menu item
     */
    MenuItem getDeveloperUtilityMenuItem();
}
