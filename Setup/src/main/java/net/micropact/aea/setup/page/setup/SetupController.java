package net.micropact.aea.setup.page.setup;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.velocity.tools.generic.EscapeTool;

import com.entellitrak.ApplicationException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;

import net.entellitrak.aea.setup.core.ISetupRequest;
import net.entellitrak.aea.setup.core.ISetupResponse;
import net.entellitrak.aea.setup.core.ISetupService;

@HandlerScript(type = PageController.class)
public class SetupController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {

		final TextResponse response = etk.createTextResponse();

		setBreadcrumbAndTitle(etk, response);

		final ISetupResponse setupResponse = ISetupService.setup(ISetupRequest.builder(etk).build());

		response.put("esc", new EscapeTool());
		response.put("setupResponse", setupResponse);

		return response;
	}

	private static void setBreadcrumbAndTitle(final PageExecutionContext etk, final TextResponse response) {
		final Parameters parameters = etk.getParameters();

		response.setTitle("Setup - Setup");
		response.setBreadcrumb(new SimpleBreadcrumb("Setup - Setup", String.format("page.request.do?page=%s",
				URLEncoder.encode(parameters.getSingle("page"), StandardCharsets.UTF_8))));
	}
}
