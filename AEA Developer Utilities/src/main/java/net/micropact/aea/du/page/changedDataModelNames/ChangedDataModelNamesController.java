package net.micropact.aea.du.page.changedDataModelNames;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.configuration.DataType;
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
 * This serves as the Controller Code for a Page which displays Data Objects and Data Elements which appear to have had
 * their names changed after being initially created.
 *
 * @author zachary.miller
 */
@HandlerScript(type = PageController.class)
public class ChangedDataModelNamesController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Changed Data Model Names",
								"page.request.do?page=du.page.changedDataModelNames")));

		final Gson gson = new Gson();
		response.put("changedObjects", gson.toJson(getChangedDataObjects(etk)));
		response.put("changedElements", gson.toJson(getChangedDataElements(etk)));

		return response;
	}

	/**
	 * Retrieves a list of data elements which appear to have had their name changed after creation.
	 *
	 * @param etk entellitrak execution context
	 * @return The list of Data Elements
	 */
	private static List<Map<String, Object>> getChangedDataElements(final ExecutionContext etk) {
		final DataObjectService dataObjectService = etk.getDataObjectService();
		final DataElementService dataElementService = etk.getDataElementService();

		return dataObjectService.getDataObjects()
				.stream()
				.flatMap(dataObject -> dataElementService.getDataElements(dataObject).stream()
						.filter(dataElement -> !Objects.equals(DataType.STATE, dataElement.getDataType()))
						.sorted(Comparator.comparing(DataElement::getName)))
				.flatMap(dataElement -> {
					final String eleName = dataElement.getName();
					final String propertyName = dataElement.getPropertyName();
					final String businessKey = dataElement.getBusinessKey();

					final boolean nameMatchesPropertyName = doesNameMatchPropertyName(eleName, propertyName);
					final boolean nameMatchesBusinessKey = doesNameMatchElementBusinessKey(etk, eleName, businessKey);

					if (!(nameMatchesPropertyName && nameMatchesBusinessKey)) {
						return Stream.of(Utility.arrayToMap(String.class, Object.class, new Object[][] {
							{ "OBJECT_NAME", dataElement.getDataObject().getName() },
							{ "ELE_NAME", eleName },
							{ "PROPERTY_NAME", propertyName },
							{ "BUSINESS_KEY", dataElement.getBusinessKey() },
							{ "nameMatchesPropertyName", nameMatchesPropertyName },
							{ "nameMatchesBusinessKey", nameMatchesBusinessKey },
						}));
					} else {
						return Stream.empty();
					}
				}).collect(Collectors.toList());
	}

	private static boolean doesNameMatchElementBusinessKey(final ExecutionContext etk, final String eleName,
			final String businessKey) {
		final String businessKeyTail;

		try {
			businessKeyTail = businessKey.substring(businessKey.lastIndexOf('.') + 1);
		} catch (final RuntimeException e) {
			etk.getLogger().debug(String.format("Business Key did not follow expected format: %s", businessKey), e);
			return false;
		}

		final String expectedPropertyName = getValidPropertyName(eleName);

		return Objects.equals(businessKeyTail, expectedPropertyName);
	}

	/**
	 * This method determines whether elementName is the internal name that entellitrak would have assigned to a Data
	 * Element whose Name was eleName.
	 *
	 * @param eleName ETK_DATA_ELEMENT.NAME
	 * @param propertyName ETK_DATA_ELEMENT.element_name
	 * @return whether the names appear to match
	 */
	private static boolean doesNameMatchPropertyName(final String eleName, final String propertyName) {
		return propertyName.equals(getValidPropertyName(eleName));
	}

	/**
	 * Retrieves a list of Data Objects which appear to have had their name changed after creation.
	 *
	 * @param etk entellitrak execution context
	 * @return The list of Data Objects
	 */
	private static List<Map<String, Object>> getChangedDataObjects(final ExecutionContext etk) {
		final DataObjectService dataObjectService = etk.getDataObjectService();

		return dataObjectService.getDataObjects().stream()
				.sorted(Comparator.comparing(DataObject::getName))
				.flatMap(dataObject -> {
					final String name = dataObject.getName();
					final String label = dataObject.getDefaultLabel();
					final String objectName = dataObject.getObjectName();
					final String businessKey = dataObject.getBusinessKey();

					final boolean nameMatchesLabel = doesNameMatchesLabel(name, label);
					final boolean nameMatchesObjectName = doesNameMatchObjectName(name, objectName);
					final boolean nameMatchesBusinessKey = doesNameMatchObjectBusinessKey(name, businessKey);

					if (!(nameMatchesLabel && nameMatchesObjectName && nameMatchesBusinessKey)) {
						return Stream.of(Utility.arrayToMap(String.class, Object.class, new Object[][] {
							{ "NAME", name },
							{ "LABEL", label },
							{ "OBJECT_NAME", objectName },
							{ "BUSINESS_KEY", businessKey },
							{ "nameMatchesLabel", nameMatchesLabel },
							{ "nameMatchesObjectName", nameMatchesObjectName },
							{ "nameMatchesBusinessKey", nameMatchesBusinessKey },
						}));
					} else {
						return Stream.of();
					}
				})
				.collect(Collectors.toList());
	}

	private static boolean doesNameMatchObjectBusinessKey(final String name, final String businessKey) {
		final String expectedObjectName = getValidObjectName(name);

		/*
		 * Suppress the NullPointerException. It should never actually be null. We don't want to change the core method.
		 */
		@SuppressWarnings("squid:S2259")
		final String uncapitalizedObjectName = String.format("%s%s",
				Character.toLowerCase(expectedObjectName.charAt(0)),
				expectedObjectName.substring(1));

		final String expectedBusinessKey = String.format("object.%s",
				uncapitalizedObjectName);

		return Objects.equals(businessKey, expectedBusinessKey);
	}

	/**
	 * This method determines whether objectName is the name that entellitrak would have created for a Data Object which
	 * was created with the Name name.
	 *
	 * @param name ETK_DATA_OBJECT.NAME
	 * @param objectName ETK_DATA_OBJECT.OBJECT_NAME
	 * @return whether the names appear to match
	 */
	private static boolean doesNameMatchObjectName(final String name, final String objectName) {
		return Objects.equals(objectName, getValidObjectName(name));
	}

	/**
	 * This method determines whether label is what entellitrak would have provided as the default label for an object
	 * with Name name.
	 *
	 * @param name ETK_DATA_OBJECT.NAME
	 * @param label ETK_DATA_OBJECT.LABEL
	 * @return whether the values match
	 */
	private static boolean doesNameMatchesLabel(final String name, final String label) {
		return Objects.equals(name, label);
	}

	/**
	 * <strong> This method is copied directly from
	 * {@link com.micropact.entellitrak.cfg.database.NamingUtil#getValidObjectName(String)} </strong>
	 *
	 * <p>
	 * Generates a valid object name based on the passed name.
	 * </p>
	 *
	 * @param name an entelliTrak object name
	 * @return a valid, camel case object name
	 */
	private static String getValidObjectName(final String name) {
		if (name == null) {
			return null;
		}

		/* Suppress warning over use of StringBuffer since we copied the core method. */
		@SuppressWarnings("squid:S1149")
		final StringBuffer rc = new StringBuffer();
		final String[] words = name.trim().split("\\W+");

		for (final String word : words) {
			rc.append(word.substring(0, 1).toUpperCase())
			.append(word.substring(1).toLowerCase());
		}

		return rc.toString();
	}

	/**
	 * <strong> This method is copied directly from
	 * {@link com.micropact.entellitrak.cfg.database.NamingUtil#getValidPropertyName(String)} </strong>
	 *
	 * <p>
	 * Generates a valid property name based on the passed name.
	 * </p>
	 *
	 * @param name an entelliTrak property name
	 * @return a valid, camel case property name
	 */
	private static String getValidPropertyName(final String name) {

		if (Utility.isBlank(name)) {
			return name;
		}

		/* Suppress warning over use of StringBuffer since we copied the core method. */
		@SuppressWarnings("squid:S1149")
		final StringBuffer rc = new StringBuffer();
		final String[] words = name.trim().split("\\W+");

		for (int i = 0; i < words.length; i++) {
			if (i == 0) {
				rc.append(words[i].substring(0, 1).toLowerCase());
			} else {
				rc.append(words[i].substring(0, 1).toUpperCase());
			}
			rc.append(words[i].substring(1).toLowerCase());
		}

		return rc.toString();
	}
}
