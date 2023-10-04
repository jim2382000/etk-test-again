package net.micropact.aea.du.page.cacheRemoveEntryAjax;

import java.io.Serializable;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.cache.Cache;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.ContentType;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.utility.Utility;

/**
 * This is the controller code for a page which removes a particular entry from the {@link com.entellitrak.cache.Cache}.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class CacheRemoveEntryAjaxController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final Parameters parameters = etk.getParameters();
        final Cache<String, Serializable> cache = etk.getSerializableCache();

        final TextResponse response = etk.createTextResponse();
        response.setContentType(ContentType.JSON);

        PageUtility.validateCsrfToken(etk);

        final String cacheKey = parameters.getSingle("cacheKey");
        cache.remove(cacheKey);

        response.put("out", new Gson().toJson(Utility.arrayToMap(String.class, Object.class, new Object[][]{
            {"status", "success"},
        })));

        return response;
    }
}
