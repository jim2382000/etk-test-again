package net.micropact.aea.du.page.lookupDefinitionUsageAjax;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.configuration.FormService;
import com.entellitrak.configuration.LookupDefinition;
import com.entellitrak.configuration.LookupDefinitionService;
import com.entellitrak.configuration.LookupSourceType;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.ContentType;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.java.map.MapBuilder;

/**
 * This page is used by the Lookup Definitions Usage page. It returns data indicating where various lookups are being
 * used.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class LookupDefinitionUsageAjaxController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk)
			throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		response.setContentType(ContentType.JSON);

		response.put("out", new Gson().toJson(getLookupDefinitionUsageData(etk).values()
				.stream()
				.sorted(Comparator.comparing(LookupUsageDto::getLookupName)
						.thenComparing(LookupUsageDto::getLookupBusinessKey))
				.map(lookupUsage -> new MapBuilder<String, Object>()
						.put("businessKey", lookupUsage.getLookupBusinessKey())
						.put("lookupName", lookupUsage.getLookupName())
						.put("cached", lookupUsage.getCached())
						.put("elementUsages", lookupUsage.getElementUsages()
								.stream()
								.sorted(Comparator.comparing(ElementUsageDto::getObjectName)
										.thenComparing(ElementUsageDto::getElementName)
										.thenComparing(ElementUsageDto::getDataElementBusinessKey))
								.collect(Collectors.toList()))
						.put("formUsages", lookupUsage.getFormUsages()
								.stream()
								.sorted(Comparator.comparing(FormUsageDto::getDataObjectName)
										.thenComparing(FormUsageDto::getFormName)
										.thenComparing(FormUsageDto::getFormControlName)
										.thenComparing(FormUsageDto::getFormControlBusinessKey))
								.collect(Collectors.toList()))
						.build())
				.collect(Collectors.toList())));

		return response;
	}

	private static Map<String, LookupUsageDto> getLookupDefinitionUsageData(final ExecutionContext etk) {
		final LookupDefinitionService lookupDefinitionService = etk.getLookupDefinitionService();
		final DataObjectService dataObjectService = etk.getDataObjectService();
		final DataElementService dataElementService = etk.getDataElementService();
		final FormService formService = etk.getFormService();

		/* For efficiency, we get a mapping from lookups to their data */
		final Map<String, LookupUsageDto> lookupBusinessKeyToLookupUsage = new HashMap<>();
		lookupDefinitionService.getLookupDefinitions()
		.forEach(lookupDefinition -> {
			final boolean cached = Objects.equals(LookupSourceType.DATA_OBJECT, lookupDefinition.getSourceType())
					&& lookupDefinitionService.getDataObjectLookupDefinitionByBusinessKey(lookupDefinition.getBusinessKey()).isCachingEnabled();

			lookupBusinessKeyToLookupUsage.put(lookupDefinition.getBusinessKey(),
					new LookupUsageDto(
							lookupDefinition.getBusinessKey(),
							lookupDefinition.getName(),
							cached,
							new ArrayList<>(),
							new ArrayList<>()));
		});

		/* Add information about data object usages */
		dataObjectService.getDataObjects()
		.stream()
		.flatMap(dataObject -> dataElementService.getDataElements(dataObject).stream())
		.filter(dataElement -> dataElement.getLookup() != null)
		.forEach(dataElement -> {
			final LookupDefinition lookupDefinition = dataElement.getLookup();
			final String lookupBusinessKey = lookupDefinition.getBusinessKey();
			lookupBusinessKeyToLookupUsage.get(lookupBusinessKey).getElementUsages()
			.add(new ElementUsageDto(dataElement.getBusinessKey(), dataElement.getDataObject().getObjectName(), dataElement.getName()));
		});

		/* Add information about form usages */
		dataObjectService.getDataObjects()
		.stream()
		.forEach(dataObject -> formService.getForms(dataObject).stream()
				.forEach(dataForm -> formService.getFormControls(dataForm).stream()
						.forEach(formControl -> {
							final LookupDefinition lookup = formService.getLookup(formControl);

							if(lookup != null) {
								lookupBusinessKeyToLookupUsage.get(lookup.getBusinessKey())
								.getFormUsages()
								.add(new FormUsageDto(
										dataObject.getObjectName(),
										formControl.getBusinessKey(),
										dataForm.getName(),
										formControl.getName()));
							}
						})));

		return lookupBusinessKeyToLookupUsage;
	}

	static class LookupUsageDto {

		private final String lookupBusinessKey;
		private final String lookupName;
		private final boolean cached;
		private final List<ElementUsageDto> elementUsages;
		private final List<FormUsageDto> formUsages;

		LookupUsageDto(final String theLookupBusinessKey, final String theLookupName, final boolean theCached, final List<ElementUsageDto> theElementUsages, final List<FormUsageDto> theFormUsages){
			lookupBusinessKey = theLookupBusinessKey;
			lookupName = theLookupName;
			cached = theCached;
			elementUsages = theElementUsages;
			formUsages = theFormUsages;
		}

		public String getLookupBusinessKey() {
			return lookupBusinessKey;
		}

		public String getLookupName() {
			return lookupName;
		}

		public boolean getCached() {
			return cached;
		}

		public List<ElementUsageDto> getElementUsages() {
			return elementUsages;
		}

		public List<FormUsageDto> getFormUsages() {
			return formUsages;
		}
	}

	static class ElementUsageDto {

		private final String dataElementBusinessKey;
		private final String objectName;
		private final String elementName;

		public ElementUsageDto(final String theDataElementBusinessKey, final String theObjectName, final String theElementName) {
			dataElementBusinessKey = theDataElementBusinessKey;
			objectName = theObjectName;
			elementName = theElementName;
		}

		public String getDataElementBusinessKey() {
			return dataElementBusinessKey;
		}

		public String getObjectName() {
			return objectName;
		}

		public String getElementName() {
			return elementName;
		}
	}

	static class FormUsageDto {

		private final String dataObjectName;
		private final String formControlBusinessKey;
		private final String formName;
		private final String formControlName;

		public FormUsageDto(final String theDataObjectName, final String theFormControlBusinessKey, final String theFormName, final String theFormControlName) {
			dataObjectName = theDataObjectName;
			formControlBusinessKey = theFormControlBusinessKey;
			formName = theFormName;
			formControlName = theFormControlName;
		}

		public String getDataObjectName() {
			return dataObjectName;
		}

		public String getFormControlBusinessKey() {
			return formControlBusinessKey;
		}

		public String getFormName() {
			return formName;
		}

		public String getFormControlName() {
			return formControlName;
		}
	}
}
