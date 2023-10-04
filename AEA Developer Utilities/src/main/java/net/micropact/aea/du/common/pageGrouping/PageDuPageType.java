package net.micropact.aea.du.common.pageGrouping;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.entellitrak.menu.MenuExecutionContext;
import com.entellitrak.menu.MenuItem;
import com.entellitrak.menu.PageMenuItem;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * @author Zachary.Miller
 *
 */
public final class PageDuPageType implements IDuPageType {

    private final String businessKey;

    /**
     * Simple constructor.
     *
     * @param theBusinessKey the page business key
     */
    public PageDuPageType(final String theBusinessKey) {
        businessKey = theBusinessKey;
    }

    @Override
    public MenuItem getMenuItem(final MenuExecutionContext etk, final DeveloperUtilityPage developerUtilityPage) {
        final PageMenuItem menuItem = etk.createPageMenuItem(String.format("net.micropact.aea.du.page.%s", businessKey), businessKey);
        menuItem.setLabel(developerUtilityPage.getName());

        return menuItem;
    }

    @Override
    public String getUrl() {
        try {
            return String.format("page.request.do?page=%s", URLEncoder.encode(businessKey, StandardCharsets.UTF_8.name()));
        } catch (final UnsupportedEncodingException e) {
            throw new GeneralRuntimeException(e);
        }
    }
}
