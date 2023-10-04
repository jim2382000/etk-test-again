/**
 *
 * Cache clearing utility controller.
 *
 * alee 11/03/2014
 **/

package net.micropact.aea.du.page.clearCache;

import java.io.Serializable;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.cache.Cache;
import com.entellitrak.cache.service.DataCacheService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

/**
 * Short helper utility to immediately call etk.getCache().clearCache().
 *
 * @author aclee
 *
 */
@HandlerScript(type = PageController.class)
public class ClearCacheController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final Cache<String, Object> cache = etk.getCache();
        final Cache<String, Serializable> serializableCache = etk.getSerializableCache();
        final DataCacheService dataCacheService = etk.getDataCacheService();

        final TextResponse response = etk.createTextResponse();

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
                BreadcrumbUtility.addLastChildFluent(
                        DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                        new SimpleBreadcrumb("Clear Cache",
                                "page.request.do?page=du.page.clearCache")));

        cache.clearCache();
        serializableCache.clearCache();
        dataCacheService.clearDataCaches();

        return response;
    }
}
