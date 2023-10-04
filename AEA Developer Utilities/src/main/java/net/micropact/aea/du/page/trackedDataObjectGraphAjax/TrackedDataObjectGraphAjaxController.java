package net.micropact.aea.du.page.trackedDataObjectGraphAjax;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.configuration.DataObjectType;
import com.entellitrak.configuration.NavigationService;
import com.entellitrak.configuration.ServiceBundleService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.ContentType;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.java.map.MapBuilder;

/**
 * This page is used by the Tracked Data Object graph page. It purpose is to return meta-data about the data objects in
 * the system.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class TrackedDataObjectGraphAjaxController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final DataObjectService dataObjectService = etk.getDataObjectService();
		final ServiceBundleService serviceBundleService = etk.getServiceBundleService();

		final TextResponse response = etk.createTextResponse();

		response.setContentType(ContentType.JSON);

		response.put("out", new Gson().toJson(dataObjectService.getDataObjectsByType(DataObjectType.TRACKED)
				.stream()
				.sorted(Comparator.comparing((final DataObject dataObject) -> TrackedDataObjectGraphAjaxController.getDepth(etk, dataObject))
						.thenComparing(Comparator.comparing(dataObject -> TrackedDataObjectGraphAjaxController.getListOrder(etk, dataObject)))
						.thenComparing(Comparator.comparing(DataObject::getDefaultLabel)))
				.map(dataObject -> new MapBuilder<>()
						.put("BUSINESS_KEY", dataObject.getBusinessKey())
						.put("PARENT_OBJECT_BUSINESS_KEY",
								Optional.ofNullable(dataObjectService.getParent(dataObject))
								.map(DataObject::getBusinessKey)
								.orElse(null))
						.put("LABEL", dataObject.getDefaultLabel())
						.put("TABLE_NAME", dataObject.getTableName())
						.put("NAME", dataObject.getName())
						.put("OBJECT_NAME", dataObject.getObjectName())
						.put("BUNDLE_NAME", serviceBundleService.getServiceBundle(dataObject).getName())
						.build())
				.collect(Collectors.toList())));

		return response;
	}

	private static long getListOrder(final ExecutionContext etk, final DataObject dataObject) {
		final DataObjectService dataObjectService = etk.getDataObjectService();
		final NavigationService navigationService = etk.getNavigationService();

		final DataObject parent = dataObjectService.getParent(dataObject);

		if(parent == null) {
			return 0;
		} else {
			return navigationService.getOrderedChildObjects(parent).indexOf(parent);
		}
	}

	private static List<DataObject> getAncestors(final ExecutionContext etk, final DataObject dataObject) {
		final DataObjectService dataObjectService = etk.getDataObjectService();

		final List<DataObject> returnList = new ArrayList<>();

		DataObject currentObject = dataObject;

		while(currentObject != null) {
			returnList.add(dataObject);
			currentObject = dataObjectService.getParent(currentObject);
		}

		return returnList;
	}

	private static long getDepth(final ExecutionContext etk, final DataObject dataObject) {
		return getAncestors(etk, dataObject).size();
	}
}
