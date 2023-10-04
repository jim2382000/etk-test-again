/**
 *
 * LogViewerFileList
 *
 * zmiller 06/05/2015
 **/

package net.micropact.aea.du.page.logViewerFileList;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.ContentType;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.micropact.aea.du.utility.LogUtility;
import net.micropact.aea.utility.Utility;

/**
 * Page controller code which returns information about the files in the container log directory.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class LogViewerFileListController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();
        response.setContentType(ContentType.JSON);

        response.put("out", new Gson().toJson(
                Utility.arrayToMap(String.class, Object.class, new Object[][]{{
                        "files", LogUtility.getLogFileInfos(etk),
                    }})));

        return response;
    }
}
