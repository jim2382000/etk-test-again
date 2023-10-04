package net.micropact.aea.du.service;

import com.entellitrak.menu.MenuExecutionContext;
import com.entellitrak.menu.MenuItem;
import com.entellitrak.menu.PageMenuItem;

import net.entellitrak.aea.du.service.IMenuItemService;
import net.micropact.aea.du.common.pageGrouping.DeveloperUtilityPage;
import net.micropact.aea.du.common.pageGrouping.DeveloperUtilityPageGroup;

/**
 * Private implementation of the public {@link IMenuItemService}.
 *
 * @author Zachary.Miller
 */
public final class MenuItemService implements IMenuItemService {

    private final MenuExecutionContext etk;

    /**
     * Simple constructor.
     *
     * @param theEtk entellitrak execution context
     */
    public MenuItemService(final MenuExecutionContext theEtk) {
        etk = theEtk;
    }

    @Override
    public MenuItem getDeveloperUtilityMenuItem() {
        final MenuItem developerUtilityMenuItem = etk.createPageMenuItem("net.micropact.aea.du", "du.page.indexStandalone");
        developerUtilityMenuItem.setLabel("Developer Utility");
        developerUtilityMenuItem.setIconCode("MdWrench");

        for(final DeveloperUtilityPageGroup pageGroup : DeveloperUtilityPageGroup.values()) {
            final PageMenuItem groupMenuItem = etk.createPageMenuItem(String.format("net.micropact.aea.du.group.%s",
                    pageGroup.getName()),
                    "du.page.indexStandalone");
            groupMenuItem.setLabel(pageGroup.getName());
            groupMenuItem.setIconCode("MdChevronDown");
            groupMenuItem.setProperty("column", String.valueOf(pageGroup.getColumn()));

            for(final DeveloperUtilityPage page : pageGroup.getDeveloperUtilityPages()) {
                groupMenuItem.add(page.getMenuItem(etk));
            }

            developerUtilityMenuItem.add(groupMenuItem);
        }

        return developerUtilityMenuItem;
    }
}
