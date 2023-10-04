package net.entellitrak.aea.gl.api.etk;

import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * Utility class for dealing with etk_tracking_config.
 *
 * @author Zachary.Miller
 */
public final class TrackingConfigUtil {

    /**
     * Utility classes do not need public constructors.
     */
    private TrackingConfigUtil() {
    }

    /**
     * Get the tracking configuration id of the configuration which will be
     * deployed next time apply changes is done.
     * This is the configuration which is visible in the Configuration tab.
     *
     * @param etk
     *            entellitrak execution context
     * @return The next tracking config id
     */
    public static long getTrackingConfigIdNext(final ExecutionContext etk) {
        try {
            return etk.createSQL("SELECT tracking_config_id FROM etk_tracking_config WHERE config_version = (SELECT MAX(config_version) FROM etk_tracking_config)")
                    .fetchLong();
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }
}
