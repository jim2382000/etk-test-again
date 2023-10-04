package net.micropact.aea.core.page.defaultControllers;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;

import net.micropact.aea.core.utility.StringEscapeUtils;

/**
 * A default page controller which passes a StringEscaper (as esc) and CSRF token (as csrfToken).
 *
 * @author Zachary.Miller
 */
@HandlerScript(type = PageController.class)
public class DefaultHtmlControllerNonCached implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        response.put("esc", StringEscapeUtils.class);
        response.put("csrfToken", etk.getCSRFToken());

        return response;
    }
}
