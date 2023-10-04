package net.micropact.aea.core.utility;

import com.entellitrak.ExecutionContext;
import com.entellitrak.SystemPreferenceService;

import net.micropact.aea.core.enums.SystemPreference;

/**
 * Utility for dealing with system preferences.
 * <p>
 *  Primarily a way to get the values of the system preferences.
 *  When retrieving preferences this class will do as little work as possible to smooth over bad core
 *  naming conventions. So for instance when determining whether public resource caching is enabled,
 *  it would make more sense to have a method isPublicResourceCaching<em>Enabled</em>, but we will instead stick
 *  with core's name of get<em>Disable</em>PublicResourceCaching.
 * </p>
 *
 * @author Zachary.Miller
 */
public final class SystemPreferenceUtils {

    /**
     * Utility classes do not need public constructors.
     */
    private SystemPreferenceUtils() {}

    /**
     * Get whether public resource caching is <em>disabled</em>.
     *
     * @param etk entellitrak execution context
     * @return whether public resource caching is <em>disabled</em>
     */
    public static boolean getDisablePublicResourceCaching(final ExecutionContext etk) {
        final SystemPreferenceService systemPreferenceService = etk.getSystemPreferenceService();

        return Boolean.parseBoolean(systemPreferenceService.loadPreference(SystemPreference.DISABLE_PUBLIC_RESOURCE_CACHING.getName()));
    }

    /**
     * Get the duration of public resource caching in hours.
     *
     * @param etk entellitrak execution context
     * @return the duration of public resource caching in hours
     */
    public static long getHoursForPublicResourceCaching(final ExecutionContext etk) {
        final SystemPreferenceService systemPreferenceService = etk.getSystemPreferenceService();

        return Long.parseLong(systemPreferenceService.loadPreference(SystemPreference.HOURS_FOR_PUBLIC_RESOURCE_CACHING.getName()));
    }
}
