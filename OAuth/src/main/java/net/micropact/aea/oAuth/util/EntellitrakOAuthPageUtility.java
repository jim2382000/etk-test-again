package net.micropact.aea.oAuth.util;

import java.util.Objects;

import com.entellitrak.ExecutionContext;
import com.entellitrak.SystemPreferenceService;
import com.entellitrak.SystemPreferences;
import com.entellitrak.page.Response;

public class EntellitrakOAuthPageUtility {

	private EntellitrakOAuthPageUtility() {}

	/**
     * This method checks to see whether public resource caching has been enabled.
     * If it has, it will set headers on the Response so that the page response is cached by browsers.
     * The response cannot be cached by intermediaries.
     *
     * @param etk entellitrak execution context
     * @param response response to set the headers on
     */
    public static void setAEACacheHeadersPrivate(final ExecutionContext etk, final Response response) {
    	final SystemPreferenceService systemPreferenceService = etk.getSystemPreferenceService();

    	final boolean isCachingEnabled = !Objects.equals("true", systemPreferenceService.loadPreference(SystemPreferences.DISABLE_PUBLIC_RESOURCE_CACHING));

        if(isCachingEnabled){
        	final long cachingDurationHours = Long.parseLong(systemPreferenceService.loadPreference(SystemPreferences.HOURS_FOR_PUBLIC_RESOURCE_CACHING));
        	final long cachingDurationSeconds = cachingDurationHours * 60 * 60;

            response.setHeader("Cache-Control",
                    String.format("private, max-age=%s",
                            cachingDurationSeconds));
            response.setHeader("Pragma", "");
        }
    }
}
