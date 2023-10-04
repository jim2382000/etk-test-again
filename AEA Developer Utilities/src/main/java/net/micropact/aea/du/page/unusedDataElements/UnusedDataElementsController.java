package net.micropact.aea.du.page.unusedDataElements;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.configuration.DataType;
import com.entellitrak.configuration.FormService;
import com.entellitrak.configuration.ViewService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.java.map.MapBuilder;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

/**
 * Controller code for a page which displays potentially unused data elements.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class UnusedDataElementsController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		setBreadcrumbAndTitle(response);

		response.put("unusedDataElements", new Gson().toJson(getUnusedDataElements(etk)));

		return response;
	}

	private static List<Map<String, Object>> getUnusedDataElements(final ExecutionContext etk) {
		final DataElementService dataElementService = etk.getDataElementService();
		final DataObjectService dataObjectService = etk.getDataObjectService();
		final FormService formService = etk.getFormService();
		final ViewService viewService = etk.getViewService();

		final Collection<DataObject> dataObjects = dataObjectService.getDataObjects();

		final Set<DataElement> dataElements = dataObjects.stream()
				.flatMap(dataObject -> dataElementService.getDataElements(dataObject).stream()
						.filter(dataElement -> !Objects.equals(DataType.STATE, dataElement.getDataType())))
				.collect(Collectors.toSet());

		final Set<DataElement> dataElementsOnForm = dataObjects.stream()
				.flatMap(dataObject -> formService.getForms(dataObject).stream())
				.flatMap(dataForm -> formService.getFormControls(dataForm).stream())
				.flatMap(formControl -> Optional.ofNullable(formService.getDataElement(formControl)).stream())
				.collect(Collectors.toSet());

		final Set<DataElement> dataElementsOnView = dataObjects.stream()
				.flatMap(dataObject -> viewService.getViews(dataObject).stream())
				.flatMap(dataView -> viewService.getViewElements(dataView).stream())
				.map(viewService::getDataElement)
				.collect(Collectors.toSet());

		return dataElements.stream()
				.filter(dataElement -> !dataElementsOnForm.contains(dataElement))
				.map(dataElement -> new MapBuilder<String, Object>()
						.put("businessKey", dataElement.getBusinessKey())
						.put("objectName", dataElement.getDataObject().getObjectName())
						.put("elementName", dataElement.getName())
						.put("onView", dataElementsOnView.contains(dataElement))
						.build())
				.sorted(Comparator.comparing((final Map<String, Object> map) -> (String) map.get("objectName"))
						.thenComparing(map -> (String) map.get("elementName")))
				.collect(Collectors.toList());
	}

	private static void setBreadcrumbAndTitle(final TextResponse response) {
		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Unused Data Elements",
								"page.request.do?page=du.page.unusedDataElements")));
	}
}
