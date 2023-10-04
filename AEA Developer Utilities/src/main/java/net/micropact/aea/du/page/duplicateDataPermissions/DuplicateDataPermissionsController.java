package net.micropact.aea.du.page.duplicateDataPermissions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import net.micropact.aea.utility.Utility;

@HandlerScript(type = PageController.class)
public class DuplicateDataPermissionsController implements PageController{

	@Override
	public Response execute(PageExecutionContext etk) throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Duplicate Data Permissions",
								"page.request.do?page=du.page.duplicateDataPermissions")));

        final List<Map<String, Object>> duplicateRecords = etk.createSQL("select data_object_type, data_element_type, role_id, count(*) from etk_data_permission group by data_object_type, data_element_type, role_id having count(*) > 1 order by data_object_type, data_element_type ")
                .fetchList()
                .stream()
                .map(duplicateRecord -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
                	{"data_object_type", duplicateRecord.get("data_object_type")},
                	{"data_element_type", duplicateRecord.get("data_element_type")},
                	{"role_id", duplicateRecord.get("role_id")},
                	{"count", duplicateRecord.get("count")}
                }))
                .collect(Collectors.toList());
        response.put("duplicateRecords", new Gson().toJson(duplicateRecords));

        response.put("csrfToken", new Gson().toJson(etk.getCSRFToken()));

        return response;
	}

}
