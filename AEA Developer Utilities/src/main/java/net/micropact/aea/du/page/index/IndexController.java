package net.micropact.aea.du.page.index;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;

import net.micropact.aea.core.utility.StringEscapeUtils;
import net.micropact.aea.du.common.pageGrouping.DeveloperUtilityPageGroup;

/**
 * Controller code used by the Developer Utilities Index page to be embedded within other pages.
 * Contains links to all of the other developer utilities.
 *
 * @author Zachary.Miller
 */
@HandlerScript(type = PageController.class)
public class IndexController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        response.put("esc", StringEscapeUtils.class);
        response.put("developerUtilityPageGroups", DeveloperUtilityPageGroup.values());

        return response;
    }
}
