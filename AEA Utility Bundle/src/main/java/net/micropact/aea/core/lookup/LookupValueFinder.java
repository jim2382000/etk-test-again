/**
 *
 * LookupValueFinder
 *
 * alee 08/18/2014
 **/

package net.micropact.aea.core.lookup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.legacy.util.StringUtility;
import com.entellitrak.localization.Localizations;
import com.entellitrak.lookup.For;
import com.entellitrak.lookup.Lookup;
import com.entellitrak.lookup.LookupResult;
import com.entellitrak.lookup.LookupService;
import com.micropact.entellitrak.cfg.model.DataObject;
import com.micropact.entellitrak.cfg.model.DataType;
import com.micropact.entellitrak.cfg.model.LookupDefinition;
import com.micropact.entellitrak.config.SpringGlobalContext;
import com.micropact.entellitrak.data.service.DataService;
import com.micropact.entellitrak.model.TrackedDataElement;
import com.micropact.entellitrak.model.TrackedDataObject;
import com.micropact.entellitrak.system.UserContainer;
import com.micropact.entellitrak.web.RequestContextHolder;
import com.micropact.entellitrak.workflow.service.WorkflowContext;
import com.micropact.internal.page.PageExecutionContextImpl;

import net.micropact.aea.core.dataTypePlugin.DataTypePluginClassUtility;
import net.micropact.aea.utility.DataElementType;
import net.micropact.aea.utility.SystemObjectDisplayFormat;
import net.micropact.aea.utility.SystemObjectType;
import net.micropact.aea.utility.Utility;
import net.micropact.aea.utility.lookup.LookupDataUtility;

public class LookupValueFinder {

	private static final String TIME_FORMAT = "MM/dd/yyyy hh:mm a";

	private final UserContainer userContainer;
	private final PageExecutionContextImpl etk;
	private final com.micropact.entellitrak.cfg.model.TrackingConfig trackingConfig;
	private final com.micropact.entellitrak.data.service.DataService dataService;

	public LookupValueFinder(final ExecutionContext etkTmp) {
		this.etk = (PageExecutionContextImpl) etkTmp;
		this.userContainer = RequestContextHolder.getUserContainer();
		this.trackingConfig = SpringGlobalContext.getBean(WorkflowContext.class).getTrackingConfig();
		this.dataService = SpringGlobalContext.getBean(DataService.class);
	}

	private String peformSelectReplacements(final String aString) {
		final boolean isSqlServer = Utility.isSqlServer(etk);

		if (isSqlServer) {
			if (aString == null) {
				return null;
			}

			final Pattern p = Pattern.compile("(select)(\\s)+(?!top)", Pattern.CASE_INSENSITIVE);
			final Matcher m = p.matcher(aString);

			return m.replaceAll("SELECT TOP 2147483647 ");
		} else {
			return aString;
		}
	}

	/**
	 *
	 * Convert null strings to empty strings.
	 *
	 * @param input
	 *            object to coerce to a string
	 * @return a non-null string
	 **/
	private static String fixNull(final Object input) {
		return input == null ? "" : input + "";
	}

	/**
	 * Returns a String display value for the provided lookup dataElementKey for a specific data element (using the
	 * trackingID to define that specific element).
	 *
	 * @param dataElement
	 *            The data element
	 * @param trackingId
	 *            The ID of the BTO/CTO.
	 * @return String with the data element's value for that BTO/CTO.
	 *
	 * @throws ApplicationException
	 *             If a problem is encountered
	 */
	public List<String> getLookupValueByBusinessKey(final DataElement dataElement, final String trackingId)
			throws ApplicationException {

		if (dataElement != null) {
			final DataObject dataObject = trackingConfig
					.getDataObjectByBusinessKey(dataElement.getDataObject().getBusinessKey());
			final TrackedDataObject tdo = dataService
					.getTrackedDataObject(dataObject, Long.valueOf(trackingId), userContainer);

			final TrackedDataElement tde = Optional.ofNullable(tdo.getElements())
					.orElse(Collections.emptyList())
					.stream()
					.filter(privateElement -> privateElement.getBusinessKey().equals(dataElement.getBusinessKey()))
					.findAny()
					.orElse(null);

			return getLookupValue(tde);
		}

		return new ArrayList<>();
	}

	/**
	 * Get the display value of the current tracked data element.
	 *
	 * @param tde
	 *            The tracked data element to load the value for
	 * @return A list of strings containing the display value. Usually this will only contain 0 or 1 elements, however
	 *         in the case of multiselects may contain more.
	 * @throws ApplicationException
	 *             If a problem is encountered
	 **/
	private List<String> getLookupValue(final TrackedDataElement tde) throws ApplicationException {
		final DataElementService dataElementService = etk.getDataElementService();

		final List<String> returnValList = new ArrayList<>();

		// Fatal Error, return.
		if (tde == null || tde.getDataElement() == null) {
			return returnValList;
		}

		final StringBuilder val = new StringBuilder("");

		if (tde.getDataElement().isBoundToLookup()) {

			final StringBuilder lookupSql = new StringBuilder("");

			if (tde.getDataElement().getLookupDefinition().isListBasedScriptLookup()) {
				final List<String> stringValues;

				if (tde.isMultiValued()) {
					final List<String> rawValues = tde.getValues();
					if (rawValues == null) {
						stringValues = Collections.emptyList();
					} else {
						stringValues = rawValues;
					}

				} else {
					final String rawValue = tde.getValue();
					if (Utility.isBlank(rawValue)) {
						stringValues = Collections.emptyList();
					} else {
						stringValues = Arrays.asList(rawValue);
					}
				}

				final DataElement dataElement = dataElementService.getDataElementByBusinessKey(tde.getBusinessKey());

				final Class<?> valueClass = DataElementType.getDataElementType(dataElement.getDataType())
						.getUnderlyingClass();

				List<?> values = null;

				if (valueClass.equals(Integer.class)) {
					values = stringValues
							.stream()
							.filter(StringUtility::isNotBlank)
							.map(Integer::valueOf)
							.collect(Collectors.toList());
				} else if (valueClass.equals(Long.class)) {
					values = stringValues
							.stream()
							.filter(StringUtility::isNotBlank)
							.map(Long::valueOf)
							.collect(Collectors.toList());
				} else {
					values = stringValues
							.stream()
							.filter(StringUtility::isNotBlank)
							.collect(Collectors.toList());
				}

				return getListBasedScriptLookup(etk, dataElement, values);
			} else if (tde.getDataElement().getLookupDefinition().isDataObjectBased()) {

				// Initialize local variables
				final LookupDefinition ld = tde.getDataElement().getLookupDefinition();
				final String valColumn = ld.getValueElement() != null ? ld.getValueElement().getColumnName() : "ID";
				final String dispColumn = ld.getDisplayElement() != null ? ld.getDisplayElement().getColumnName()
						: "ID";

				lookupSql.append("select DISPLAY from (select ");
				lookupSql.append(valColumn);
				lookupSql.append(" as VALUE, ");
				lookupSql.append(dispColumn);
				lookupSql.append(" as DISPLAY from ");
				lookupSql.append(ld.getDataObject().getTableName());
			} else if (tde.getDataElement().getLookupDefinition().isSystemObjectBased()) {
				final int objectType = tde.getDataElement().getLookupDefinition().getSystemObjectTypeId().intValue();
				final int formatId = tde.getDataElement().getLookupDefinition().getSystemObjectDisplayFormatId()
						.intValue();

				lookupSql.append("select DISPLAY from (");
				lookupSql.append(LookupDataUtility.getSystemObjectQuery(etk,
						SystemObjectType.getById(objectType),
						SystemObjectDisplayFormat.getById(formatId)));

			} else {
				lookupSql.append("select DISPLAY from (");
				lookupSql.append(dataService.getLookupDataViewSql(tde.getDataElement(), userContainer));
			}

			if ("".equals(lookupSql.toString())) {
				return returnValList;
			}

			DataType lookupValueType = null;

			if (tde.getDataElement().getLookupDefinition()
					.isSystemObjectBased()) {
				lookupValueType = DataType.LONG;
			} else if (tde.getDataElement().getLookupDefinition().isDataObjectBased()) {
				if (tde.getDataElement().getLookupDefinition().getValueElement() == null) {
					lookupValueType = DataType.LONG;
				} else {
					lookupValueType = tde.getDataElement().getLookupDefinition().getValueElement().getDataType();
				}
			} else {
				lookupValueType = tde.getDataElement().getLookupDefinition()
						.getValueReturnType();
			}

			if (lookupValueType == null) {
				etk.getLogger().error(
						String.format("Lookup with business key %s does not have a return type set, setting to TEXT.",
								tde.getDataElement().getLookupDefinition().getBusinessKey()));

				lookupValueType = DataType.TEXT;
			}

			List<Map<String, Object>> lookupReturn = null;

			// Ensure the query executed correctly.
			try {
				if (tde.getDataElement().isMultiValued()) {
					if (tde.getValues() == null || tde.getValues().isEmpty()) {
						return returnValList;
					} else {
						final Map<String, Object> paramMap = new HashMap<>();
						final ArrayList<Object> typedValueArray = new ArrayList<>();

						for (final String aValue : tde.getValues()) {
							typedValueArray
							.add(LookupDataUtility.convertStringToTypedObject(etk, aValue, lookupValueType));
						}

						lookupSql.append(") var_rep_inside_query where ");
						Utility.addLargeInClause("VALUE", lookupSql, paramMap, typedValueArray);

						lookupReturn = etk.createSQL(peformSelectReplacements(lookupSql.toString()))
								.setParameter(paramMap)
								.fetchList();
					}
				} else {
					if (tde.getValue() == null) {
						return returnValList;
					} else {
						lookupSql.append(") var_rep_inside_query where VALUE = :aValue");
						lookupReturn = etk.createSQL(peformSelectReplacements(lookupSql.toString()))
								.setParameter("aValue",
										LookupDataUtility.convertStringToTypedObject(etk, tde.getValue(), lookupValueType))
								.fetchList();
					}
				}
			} catch (final Exception e) {
				etk.getLogger().error(String.format("Error executing query %s", lookupSql.toString()), e);
			}

			// If the query failed, do not perform this.
			if (lookupReturn != null) {
				for (final Map<String, Object> lookupReturnVal : lookupReturn) {

					if (lookupReturnVal.get("DISPLAY") == null) {
						continue;
					}

					returnValList.add(lookupReturnVal.get("DISPLAY") + "");
				}
			}

			return returnValList;
		} else if (tde.getDataElement().getDataType().isYesNo()) {
			if ("1".equals(tde.getValue())) {
				val.append("Yes");
			} else if ("0".equals(tde.getValue())) {
				val.append("No");
			} else {
				val.append(fixNull(tde.getValue()));
			}
		} else if (tde.getDataElement().getDataType().isFile()) {
			if (tde.getFileElement() != null) {
				val.append(fixNull(tde.getFileElement().getName()));
			}
		} else if (tde.getDataElement().getDataType().isPassword()) {
			if (tde.getPasswordElement() != null) {
				val.append(fixNull(tde.getPasswordElement().getUserLabel()));
			}
		} else if (tde.getDataElement().getDataType().isTimestamp()) {
			try {
				if (etk.getCurrentUser().getTimeZonePreference() != null) {
					final SimpleDateFormat timeFormatter = new SimpleDateFormat(TIME_FORMAT);

					val.append(
							Localizations.toLocalTimestamp(
									etk.getCurrentUser().getTimeZonePreference(), timeFormatter.parse(tde.getValue())));
				} else {
					val.append(fixNull(tde.getValue()));
				}
			} catch (final Exception e) {
				etk.getLogger().debug("Problem handling timestamp", e);
				val.append(fixNull(tde.getValue()));
			}
		} else if (tde.getDataElement().getDataType().isPluginBased()) {
			val.append(DataTypePluginClassUtility.getDataTypePluginDisplayFromStringValue(etk,
					tde.getDataElement().getBusinessKey(), tde.getValue()));
		} else {
			val.append(fixNull(tde.getValue()));
		}

		returnValList.add(val.toString());

		return returnValList;
	}

	public static List<String> getListBasedScriptLookup(final ExecutionContext etk, final DataElement dataElement,
			final List<?> values) {
		final LookupService lookupService = etk.getLookupService();

		final com.entellitrak.configuration.LookupDefinition lookupDefinition = dataElement.getLookup();

		final String lookupDefinitionBusinessKey = lookupDefinition.getBusinessKey();

		final Lookup lookup = lookupService.getLookup(lookupDefinitionBusinessKey);

		final String dataElementPropertyName = dataElement.getPropertyName();

		final List<LookupResult> selectedElements = lookup
				.setCurrentElement(dataElementPropertyName)
				.set(dataElementPropertyName, values)
				.execute(For.SINGLE);

		return selectedElements
				.stream()
				.map(LookupResult::getDisplay)
				.collect(Collectors.toList());
	}
}
