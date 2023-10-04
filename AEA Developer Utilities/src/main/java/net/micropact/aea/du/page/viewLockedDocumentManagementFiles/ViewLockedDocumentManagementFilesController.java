/**
 *
 * ViewLockedDocumentManagementFilesController
 *
 * zmiller 02/12/2016
 **/

package net.micropact.aea.du.page.viewLockedDocumentManagementFiles;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

/**
 * This is the controller code for a page which shows all Document Management files which are currently locked.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class ViewLockedDocumentManagementFilesController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
            BreadcrumbUtility.addLastChildFluent(
                DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                new SimpleBreadcrumb("Locked Document Management Files",
                    "page.request.do?page=du.page.viewLockedDocumentManagementFiles")));

        response.put("lockedFiles", new Gson().toJson(etk.createSQL("SELECT  "
            + "f.file_name \"FILENAME\",  "
            + "dataObject.business_key \"OBJECTKEY\",  "
            + "dataObject.name  \"OBJECTNAME\",  "
            + "f.reference_id  \"TRACKINGID\",  "
            + "dmResource.locked_on  \"LOCKEDON\",  "
            + "u.user_id  \"USERID\",  "
            + "u.username  \"USERNAME\"  "
            + "FROM  etk_file  f  JOIN  etk_dm_resource  dmResource  ON  dmResource.resource_id  =  f.etk_dm_resource_id  JOIN  etk_user  u  ON  u.user_id  =  dmResource.locked_by_user_id  JOIN  etk_data_object  dataObject  ON  dataObject.table_name  =  f.object_type  WHERE  dmResource.is_locked  =  1  AND  dataObject.tracking_config_id  =  (SELECT  MAX  (tracking_config_id)  FROM  etk_tracking_config_archive  )  ORDER  BY  5,  7,  6,  3,  2,  4,  1")
            .fetchList()));

        return response;
    }
}
