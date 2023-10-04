package net.micropact.aea.du.page.viewhttpheaders;

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
public class ViewHttpHeadersController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("View HTTP Headers",
								"page.request.do?page=du.page.viewHttpHeaders")));

		final List<Map<String, String>> httpHeaders = etk.getHeaderNames()
			.stream()
			.sorted()
			.map(headerName ->
				Utility.arrayToMap(String.class, String.class, new String[][] {
					{"key", headerName},
					{"value", etk.getHeader(headerName)}
				}))
			.collect(Collectors.toList());

		response.put("httpHeaders", new Gson().toJson(httpHeaders));

		return response;
	}
}
