package net.micropact.aea.du.page.systemPreferencesTool;

import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.SystemPreferenceService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.ContentType;
import com.entellitrak.page.FileResponse;
import com.entellitrak.page.FileStream;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.entellitrak.aea.du.DuServiceFactory;
import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.enums.SystemPreference;
import net.micropact.aea.core.ioUtility.IOUtility;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.du.service.SystemPreferenceMigrationService;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.du.utility.systemPreference.SystemPreferenceValue;
import net.micropact.aea.du.utility.systemPreference.SystemPreferencesDTO;
import net.micropact.aea.utility.Utility;


/**
 * This class serves as the controller code for a page which provides tools for handling entellitrak system preferences.
 * This page can display, import, export, and set recommended production values for system preferences.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class SystemPreferencesToolController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		try {
			final Parameters parameters = etk.getParameters();

			/* We will handle each of the actions separately. They will each produce their own Response. While this
			 * is a little annoying since the TextResponses all set similar response variables, the export actually
			 * has to produce a FileResponse. */
			final String action = Optional.ofNullable(parameters.getSingle("action"))
					.orElse("initial");

			switch(action) {
			case "initial":
				return doInitial(etk);
			case "export":
				return doExport(etk);
			case "import":
				return doImport(etk);
			case "setProductionValues":
				return doSetProductionValues(etk);
			default:
				throw new GeneralRuntimeException(String.format("Action not recognized: %s",
						action));
			}
		} catch (final Exception e) {
			throw new ApplicationException(e);
		}
	}

	/**
	 * Set the breadcrumb for the response.
	 *
	 * @param response the response
	 */
	private static void setBreadcrumb(final TextResponse response) {
		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("System Preferences Tool",
								"page.request.do?page=du.page.systemPreferencesTool")));
	}

	/**
	 * This method adds the default parameters that all of the text responses should contain.
	 *
	 * @param etk entellitrak execution context
	 * @param response Response to add the default parameters to
	 */
	private static void addDefaultTextResponseParameters(final PageExecutionContext etk, final TextResponse response){
		final Gson gson = new GsonBuilder().serializeNulls().create();

		response.put("allPreferences", gson.toJson(getEditablePreferences(etk)));
		response.put("productionPreferences", gson.toJson(getProductionPreferences()));
		response.put("csrfToken", new Gson().toJson(etk.getCSRFToken()));
	}

	/**
	 * Handles the import action. (uploading a file).
	 *
	 * @param etk entellitrak execution context
	 * @return The response to be returned by the controller
	 * @throws Exception If there was an underlying exception.
	 */
	private static Response doImport(final PageExecutionContext etk) throws Exception {
		final Parameters parameters = etk.getParameters();

		final TextResponse response = etk.createTextResponse();

		setBreadcrumb(response);

		PageUtility.validateCsrfToken(etk);

		final FileStream fileStream = parameters.getFile("importFile");
		if(fileStream != null){
			InputStream inputStream = null;
			try{
				inputStream = fileStream.getInputStream();
				DuServiceFactory.getSystemPreferenceMigrationService(etk).importFromStream(inputStream);
			}finally{
				IOUtility.closeQuietly(inputStream);
			}
		}
		addDefaultTextResponseParameters(etk, response);
		return response;
	}

	/**
	 * This method handles the export action (producing the XML file).
	 *
	 * @param etk entellitrak execution context
	 * @return The response to be returned by the controller
	 * @throws Exception If there was an underlying exception.
	 */
	private static Response doExport(final PageExecutionContext etk) throws Exception {
		final Parameters parameters = etk.getParameters();

		final List<String> preferences = Optional.ofNullable(parameters.getField("preferences"))
				.orElse(Collections.emptyList());

		final FileResponse response = etk.createFileResponse("systemPreferenceExport.json",
				DuServiceFactory.getSystemPreferenceMigrationService(etk).exportToStream(new HashSet<>(preferences)));
		response.setContentType(ContentType.JSON);
		return response;
	}

	/**
	 * Handles the initial page load.
	 *
	 * @param etk entellitrak execution context
	 * @return The response to be returned by the page controller
	 */
	private static Response doInitial(final PageExecutionContext etk) {
		final TextResponse response = etk.createTextResponse();
		setBreadcrumb(response);
		addDefaultTextResponseParameters(etk, response);
		return response;
	}

	/**
	 * Handles the action of setting the production values to their default values.
	 *
	 * @param etk entellitrak execution context
	 * @return The response to be returned by the page controller
	 */
	private static Response doSetProductionValues(final PageExecutionContext etk) {
		final Parameters parameters = etk.getParameters();
		final TextResponse response = etk.createTextResponse();

		setBreadcrumb(response);

		PageUtility.validateCsrfToken(etk);

		final List<SystemPreferenceValue> preferenceValues =
				getProductionPreferenceValues(parameters.getField("preferences"));
		final SystemPreferencesDTO systemPreferencesDTO = new SystemPreferencesDTO(preferenceValues);

		SystemPreferenceMigrationService.updatePreferenceValues(etk, systemPreferencesDTO);

		addDefaultTextResponseParameters(etk, response);

		return response;
	}

	/**
	 * This method gets a list of the default production values for a list of system preference names.
	 *
	 * @param preferenceNames names of the preferences whose values should be gotten
	 * @return The preferences and their corresponding default production value
	 */
	private static List<SystemPreferenceValue> getProductionPreferenceValues(final List<String> preferenceNames){
		return Optional.ofNullable(preferenceNames)
				.orElse(Collections.emptyList())
				.stream()
				.map(preferenceName
						-> new SystemPreferenceValue(
								preferenceName,
								SystemPreference.getPreferenceByName(preferenceName).getDefaultProductionValue().orElse(null)))
				.collect(Collectors.toList());
	}

	/**
	 * Gets a list of the preferences which are currently configured in the system along with their values.
	 *
	 * @param etk entellitrak execution context
	 * @return A generic representation of the preferences in the system, their current value and whether they
	 *      are exportable by default.
	 */
	private static List<Map<String, Object>> getEditablePreferences(final ExecutionContext etk) {
		final SystemPreferenceService systemPreferenceService = etk.getSystemPreferenceService();

		return systemPreferenceService.getAllPreferences().stream()
				.filter(systemPreference -> !systemPreference.isReadOnly())
				.sorted(Comparator.comparing(com.entellitrak.SystemPreference::getName))
				.map(systemPreference -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
					{"name", systemPreference.getName()},
					{"currentValue", systemPreference.getValue()},
					{"isExportableByDefault", SystemPreference.getPreferenceByCorePreference(systemPreference).isExportable()}
				}))
				.collect(Collectors.toList());
	}

	/**
	 * This method gets a list of all system preferences which have a recommended value in production.
	 *
	 * @return a list of production system preferences
	 */
	private static List<Map<String, Object>> getProductionPreferences() {
		return SystemPreference.getProductionSystemPreferences().stream()
				.map(preference -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
					{"name", preference.getName()},
		            {"defaultProductionValue", preference.getDefaultProductionValue().orElse(null)},
		            {"isExportableByDefault", preference.isExportable()},
				}))
				.collect(Collectors.toList());
	}
}
