package net.entellitrak.aea.gl.api.etk;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.entellitrak.ExecutionContext;

/**
 * Utility class for dealing with entellitrak Endpoints.
 *
 * @author Zachary.Miller
 */
public final class EndpointUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private EndpointUtil() {}

    /**
     * Sets headers related to caching on a response builder.
     * Whether to cache, and the duration to cache are determined by the public resource caching system preferences.
     *
     * @param etk entellitrak execution context
     * @param responseBuilder the response builder
     * @return the response builder
     */
    public static ResponseBuilder setPublicResourceCache(final ExecutionContext etk, final ResponseBuilder responseBuilder) {
        final boolean isCachingEnabled = !SystemPreferenceUtil.getDisablePublicResourceCaching(etk);

        if(isCachingEnabled){
            final long cachingDurationHours = SystemPreferenceUtil.getHoursForPublicResourceCaching(etk);
            final long cachingDurationSeconds = cachingDurationHours * 60 * 60;

            final LocalDateTime now = LocalDateTime.now();
            final LocalDateTime cacheExpirationDateTime = now.plusSeconds(cachingDurationSeconds);
            final ZonedDateTime cacheExpirationZonedDateTime = cacheExpirationDateTime.atZone(ZoneId.systemDefault());

            final Date expiresDate = Date.from(cacheExpirationZonedDateTime.toInstant());

            final CacheControl cacheControl = new CacheControl();
            cacheControl.setMaxAge((int) cachingDurationSeconds);

            responseBuilder
                .cacheControl(cacheControl)
                .expires(expiresDate)
                .header("Pragma", "");
        }

        return responseBuilder;
    }
}
