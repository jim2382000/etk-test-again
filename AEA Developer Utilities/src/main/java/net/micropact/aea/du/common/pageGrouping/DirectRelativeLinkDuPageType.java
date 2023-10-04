package net.micropact.aea.du.common.pageGrouping;

import com.entellitrak.menu.Link;
import com.entellitrak.menu.LinkMenuItem;
import com.entellitrak.menu.MenuExecutionContext;
import com.entellitrak.menu.MenuItem;

/**
 * Developer Utility Page representing a link to a resource internal to the site (such as the javadoc).
 *
 * @author Zachary.Miller
 */
public final class DirectRelativeLinkDuPageType implements IDuPageType {

    private final String url;

    /**
     * Simple constructor.
     *
     * @param theUrl the relative URL <strong>without a leading /</strong>
     */
    public DirectRelativeLinkDuPageType(final String theUrl) {
        url = theUrl;
    }

    @Override
    public MenuItem getMenuItem(final MenuExecutionContext etk, final DeveloperUtilityPage developerUtilityPage) {
        final LinkMenuItem menuItem = new LinkMenuItem(String.format("net.micropact.aea.du.link.%s", url), Link.relative(url));
        menuItem.setLabel(developerUtilityPage.getName());

        return menuItem;
    }

    @Override
    public String getUrl() {
        return url;
    }
}
