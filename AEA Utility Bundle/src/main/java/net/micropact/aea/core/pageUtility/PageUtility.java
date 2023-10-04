package net.micropact.aea.core.pageUtility;

import java.util.Objects;

import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.page.Page;
import com.entellitrak.page.Response;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.utility.SystemPreferenceUtils;

/**
 * Class for providing utility functionality for pages. Once we get enough functionality, it should make sense to split
 * this class into smaller pieces.
 *
 * @author zmiller
 */
public final class PageUtility {

    /**
     * Utility classes do not need constructors.
     */
    private PageUtility(){}

    /**
     * This method checks to see whether public resource caching has been enabled.
     * If it has, it will set headers on the Response so that the page response is cached by browsers.
     * The response can be cached by intermediaries.
     *
     * @param etk entellitrak execution context
     * @param response response to set the headers on
     * @see #setAEACacheHeadersPrivate(ExecutionContext, Response)
     */
    public static void setAEACacheHeaders(final ExecutionContext etk, final Response response) {
        final boolean isCachingEnabled = !SystemPreferenceUtils.getDisablePublicResourceCaching(etk);

        if(isCachingEnabled){
            final long cachingDurationHours = SystemPreferenceUtils.getHoursForPublicResourceCaching(etk);
            final long cachingDurationSeconds = cachingDurationHours * 60 * 60;

            response.setHeader("Cache-Control",
                    String.format("public, max-age=%s",
                            cachingDurationSeconds));
            response.setHeader("Pragma", "");
        }
    }

    /**
     * This method checks to see whether public resource caching has been enabled.
     * If it has, it will set headers on the Response so that the page response is cached by browsers.
     * The response cannot be cached by intermediaries.
     *
     * @param etk entellitrak execution context
     * @param response response to set the headers on
     * @see #setAEACacheHeaders(ExecutionContext, Response)
     */
    public static void setAEACacheHeadersPrivate(final ExecutionContext etk, final Response response) {
        final boolean isCachingEnabled = !SystemPreferenceUtils.getDisablePublicResourceCaching(etk);

        if(isCachingEnabled){
            final long cachingDurationHours = SystemPreferenceUtils.getHoursForPublicResourceCaching(etk);
            final long cachingDurationSeconds = cachingDurationHours * 60 * 60;

            response.setHeader("Cache-Control",
                    String.format("private, max-age=%s",
                            cachingDurationSeconds));
            response.setHeader("Pragma", "");
        }
    }

    /**
     * validates that a page execution context has the correct CSRF token.
     * The CSRF token must be named "csrfToken".
     * If it does not, an exception is thrown.
     *
     * @param etk entellitrak execution context
     */
    public static void validateCsrfToken(final PageExecutionContext etk) {
        final String validCsrfToken = etk.getCSRFToken();

        final String providedCsrfToken = etk.getParameters().getSingle("csrfToken");

        if(!Objects.equals(providedCsrfToken, validCsrfToken)) {
            throw new GeneralRuntimeException("A page was submitted with an invalid CSRF token");
        }
    }

    /**
     * Get the id of a page.
     *
     * @param etk entellitrak execution context
     * @param page the page
     * @return the page id
     */
    public static long getPageId(final ExecutionContext etk, final Page page) {
        try {
            return etk.createSQL("SELECT page_id FROM etk_page WHERE business_key = :businessKey")
                    .setParameter("businessKey", page.getBusinessKey())
                    .fetchLong();
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }
}
