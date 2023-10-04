package net.entellitrak.aea.gl.api.etk;

import java.util.Map;

import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.page.Page;
import com.entellitrak.platform.DatabasePlatform;
import com.entellitrak.user.User;

import net.entellitrak.aea.gl.api.etk.sql.OracleUtil;
import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.entellitrak.aea.gl.api.java.map.MapBuilder;

/**
 * Utility class for dealing with page dashboard options.
 *
 * @author Zachary.Miller
 */
public final class PageDashboardOptionUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private PageDashboardOptionUtil() {}

    /**
     * Sets the page dashboard options for a single user/page combination.
     * This method will work regardless of whether a record already exists in etk_page_dashboard_option.
     *
     * @param etk entellitrak execution context
     * @param user the user
     * @param page the page
     * @param listOnDashboard whether to list the page on the dashboard
     * @param displayOnDashboard whether to display the page on the dashboard
     */
    public static void setPageDashboardOption(final ExecutionContext etk, final User user, final Page page, final ListOnDashboardType listOnDashboard, final DisplayOnDashboardType displayOnDashboard) {
        final long pageDashboardOptionId = ensurePageDashboardOpitonExists(etk, user, page);
        setExistingPageDashboardOption(etk, pageDashboardOptionId, listOnDashboard, displayOnDashboard);
    }

    /**
     * Set the list/display values on an existing page dashboard option.
     *
     * @param etk entellitrak execution context
     * @param pageDashboardOptionId the page dashboard option id
     * @param listOnDashboard whether to list the page on the dashboard
     * @param displayOnDashboard whether to display the page on the dashboard
     */
    private static void setExistingPageDashboardOption(final ExecutionContext etk, final long pageDashboardOptionId, final ListOnDashboardType listOnDashboard, final DisplayOnDashboardType displayOnDashboard) {
        etk.createSQL("UPDATE etk_page_dashboard_option SET list_on_dashboard = :listOnDashboard, display_on_dashboard = :displayOnDashboard WHERE page_dashboard_option_id = :pageDashboardOptionId")
        .setParameter("pageDashboardOptionId", pageDashboardOptionId)
        .setParameter("listOnDashboard", listOnDashboard == ListOnDashboardType.LIST_ON_DASHBOARD ? 1 : 0)
        .setParameter("displayOnDashboard", displayOnDashboard == DisplayOnDashboardType.DISPLAY_ON_DASHBOARD ? 1 : 0)
        .execute();
    }

    /**
     * Ensures that a page dashboard option exists for a given user/page combination and gets the id.
     * Inserts a record if it does not exist.
     *
     * @param etk entellitrak execution context
     * @param user the user
     * @param page the page
     * @return the dashboard option id
     */
    private static long ensurePageDashboardOpitonExists(final ExecutionContext etk, final User user, final Page page) {
        final long returnValue;

        final Long existingPageDashboardOptionId = getExistingPageDashboardOptionId(etk, user, page);

        if(existingPageDashboardOptionId == null) {
            returnValue = insertPageDashboardOption(etk, user, page);
        }else {
            returnValue = existingPageDashboardOptionId;
        }

        return returnValue;
    }

    /**
     * Insert a new page dashboard option.
     * <strong>Does not check whether an entry already exists.</strong>
     *
     * @param etk entellitrak execution context
     * @param user the user
     * @param page the page
     * @return the new page dashboard option id
     */
    private static long insertPageDashboardOption(final ExecutionContext etk, final User user, final Page page) {
        final long returnValue;
        final Map<String, Object> queryParameters = new MapBuilder<String, Object>()
                .put("pageBusinessKey", page.getBusinessKey())
                .put("displayOnDashboard", 0)
                .put("listOnDashboard", 0)
                .put("userId", user.getId())
                .build();

        DatabasePlatform databasePlatform = etk.getPlatformInfo().getDatabasePlatform();
        switch (databasePlatform) {
        case SQL_SERVER:
            returnValue = etk.createSQL("INSERT INTO etk_page_dashboard_option(page_id, display_on_dashboard, list_on_dashboard, user_id, page_business_key) VALUES((SELECT page_id FROM etk_page WHERE business_key = :pageBusinessKey), :displayOnDashboard, :listOnDashboard, :userId, :pageBusinessKey)")
            .setParameter(queryParameters)
            .execute("page_dashboard_option_id");
            break;
        case ORACLE:
            returnValue = OracleUtil.getHibernateSequenceNextVal(etk);

            etk.createSQL("INSERT INTO etk_page_dashboard_option(page_dashboard_option_id, page_id, display_on_dashboard, list_on_dashboard, user_id, page_business_key) VALUES(:pageDashboardOptionId, (SELECT page_id FROM etk_page WHERE business_key = :pageBusinessKey), :displayOnDashboard, :listOnDashboard, :userId, :pageBusinessKey)")
            .setParameter(queryParameters)
            .setParameter("pageDashboardOptionId", returnValue)
            .execute();
            break;
        case POSTGRESQL:
            returnValue = etk.createSQL("INSERT INTO etk_page_dashboard_option(page_id, display_on_dashboard, list_on_dashboard, user_id, page_business_key) VALUES((SELECT page_id FROM etk_page WHERE business_key = :pageBusinessKey), :displayOnDashboard, :listOnDashboard, :userId, :pageBusinessKey) RETURNING page_dashboard_option_id")
            .setParameter(queryParameters)
            .execute("page_dashboard_option_id");
            break;
        default:
            throw new GeneralRuntimeException(String.format("Database platform not supported: %s",
                    databasePlatform));
        }

        return returnValue;
    }

    /**
     * Get an existing page dashboard option id for a given user/page combination or null if one does not exist.
     *
     * @param etk entellitrak execution context
     * @param user the user
     * @param page the page
     * @return the existing dashboard option id, or null if one does not exist
     */
    private static Long getExistingPageDashboardOptionId(final ExecutionContext etk, final User user, final Page page) {
        try {
            return etk.createSQL("SELECT page_dashboard_option_id FROM etk_page_dashboard_option WHERE user_id = :userId AND page_business_key = :pageBusinessKey")
                    .setParameter("userId", user.getId())
                    .setParameter("pageBusinessKey", page.getBusinessKey())
                    .returnEmptyResultSetAs(null)
                    .fetchLong();
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(String.format("Error getting dashboard option for user %s and page %s",
                    user.getAccountName(),
                    page.getBusinessKey()),
                    e);
        }
    }
}
