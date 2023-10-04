package net.micropact.aea.du.page.viewObjectData;

import java.util.Comparator;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/**
 * This class is the controller code for a page which can be used to view the data for a BTO and all of its descendants.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class ViewObjectDataController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final DataObjectService dataObjectService = etk.getDataObjectService();

		final TextResponse response = etk.createTextResponse();

		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("View Object Data",
								"page.request.do?page=du.page.viewObjectData")));

		response.put("dataObjects", new Gson().toJson(
				dataObjectService.getBaseTrackedObjects()
				.stream()
				.sorted(Comparator.comparing(DataObject::getLabel).thenComparing(DataObject::getBusinessKey))
				.map(dataObject -> Utility.arrayToMap(String.class, Object.class, new Object[][]{
					{"LABEL", dataObject.getLabel()},
					{"BUSINESS_KEY", dataObject.getBusinessKey()},
				}))
				.collect(Collectors.toList())));

		return response;
	}
}
