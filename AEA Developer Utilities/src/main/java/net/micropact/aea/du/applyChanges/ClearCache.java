package net.micropact.aea.du.applyChanges;

import java.io.Serializable;

import com.entellitrak.ApplicationException;
import com.entellitrak.cache.Cache;
import com.entellitrak.cache.service.DataCacheService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.system.ApplyChangesEventHandler;
import com.entellitrak.system.ApplyChangesExecutionContext;

/**
 * This Apply Changes handler clears the caches.
 * In a high concurrency system <strong>you still may need to clear the cache manually</strong>.
 * This is the case if your cache contains information dependent on the tracking configuration because a user could load
 * data into the cache after it has been cleared, but before the Apply Changes transaction has committed.
 *
 * @author zmiller
 */
@HandlerScript(type = ApplyChangesEventHandler.class)
public class ClearCache implements ApplyChangesEventHandler {

    @Override
    public void execute(final ApplyChangesExecutionContext etk) throws ApplicationException {
    	// Suppress deprecation warning. We clear any cache we can.
        @SuppressWarnings("deprecation")
		final Cache<String, Object> cache = etk.getCache();
        final Cache<String, Serializable> serializableCache = etk.getSerializableCache();
        final DataCacheService dataCacheService = etk.getDataCacheService();

        cache.clearCache();
        serializableCache.clearCache();
        dataCacheService.clearDataCaches();
    }
}
