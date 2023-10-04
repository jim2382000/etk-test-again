package net.micropact.aea.du.page.viewJavaSystemProperties;

import java.util.Comparator;
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
public class ViewJavaSystemPropertiesController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("View Java System Properties",
								"page.request.do?page=du.page.viewjavasystemproperties")));

		final List<Map<String, Object>> properties = System.getProperties().entrySet()
				.stream()
				.sorted(Comparator.comparing(entry -> (String) entry.getKey()))
				.map(entry -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
					{"key", entry.getKey()},
					{"value", entry.getValue()}
				}))
				.collect(Collectors.toList());

		response.put("systemProperties", new Gson().toJson(properties));

		return response;
	}
}
