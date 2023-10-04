package net.micropact.aea.du.utility.page;

import com.entellitrak.page.Breadcrumb;
import com.entellitrak.page.SimpleBreadcrumb;

/**
 * Utility class for common developer utilities functionality related to breadcrumb.
 *
 * @author Zachary.Miller
 */
public final class DuBreadcrumbUtility {

    /**
     * Utility classes do not need public constructors.
     */
    private DuBreadcrumbUtility() {
    }

    /**
     * Get the breadcrumb for Developer Utilities.
     * This is the breadcrumb used by the index pages, and should be the parent breadcrumb of each individual page.
     *
     * @return the breadcrumb
     */
    public static Breadcrumb getDeveloperUtilityBreadcrumb() {
        return new SimpleBreadcrumb("Developer Utility", "page.request.do?page=du.page.indexStandalone");
    }
}
