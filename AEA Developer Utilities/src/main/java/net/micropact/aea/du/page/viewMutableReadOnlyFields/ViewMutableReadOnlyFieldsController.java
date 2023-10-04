package net.micropact.aea.du.page.viewMutableReadOnlyFields;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.configuration.FormControl;
import com.entellitrak.configuration.FormService;
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
 * This class serves as the controller code for a class which displays all fields which are both mutable and read-only.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class ViewMutableReadOnlyFieldsController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		setBreadcrumbAndTitle(response);

		response.put("fields", new Gson().toJson(getMutableReadOnlyFields(etk)));

		return response;
	}

	private static List<Map<String, Object>> getMutableReadOnlyFields(final ExecutionContext etk) {
		final FormService formService = etk.getFormService();
		final DataObjectService dataObjectService = etk.getDataObjectService();

		return dataObjectService.getDataObjects()
				.stream()
				.flatMap(dataObject -> formService.getForms(dataObject)
						.stream()
						.flatMap(form -> formService.getFormControls(form).stream()
								.filter(FormControl::isMutableReadOnly)
								.map(formControl -> new MapBuilder<String, Object>()
										.put("objectName", dataObject.getName())
										.put("formName", form.getName())
										.put("formControlName", formControl.getName())
										.build())))
				.sorted(Comparator.comparing((final Map<String, Object> map) -> (String) map.get("objectName"))
						.thenComparing(map -> (String) map.get("formName"))
						.thenComparing(map -> (String) map.get("formControlName")))
				.collect(Collectors.toList());
	}

	private static void setBreadcrumbAndTitle(final TextResponse response) {
		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Mutable Read Only Fields",
								"page.request.do?page=du.page.viewMutableReadOnlyFields")));
	}
}
