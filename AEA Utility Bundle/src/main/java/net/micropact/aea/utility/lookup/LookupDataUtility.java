/**
 *
 * Utility methods used by Live Search.
 *
 * administrator 09/15/2014
 **/

package net.micropact.aea.utility.lookup;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.SortDirection;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataObjectLookupDefinition;
import com.entellitrak.configuration.FormControl;
import com.entellitrak.configuration.LookupDefinition;
import com.entellitrak.configuration.LookupSourceType;
import com.entellitrak.configuration.ScriptLookupDefinition;
import com.entellitrak.configuration.SqlScriptLookupDefinition;
import com.entellitrak.configuration.SystemObjectLookupDefinition;
import com.entellitrak.configuration.Workspace;
import com.entellitrak.legacy.util.DateUtility;
import com.entellitrak.legacy.util.StringUtility;
import com.entellitrak.lookup.LookupExecutionContext;
import com.entellitrak.lookup.LookupHandler;
import com.micropact.entellitrak.cfg.model.DataType;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.utility.StringEscapeUtils;
import net.micropact.aea.utility.DataElementType;
import net.micropact.aea.utility.DataObjectType;
import net.micropact.aea.utility.SystemObjectDisplayFormat;
import net.micropact.aea.utility.SystemObjectType;
import net.micropact.aea.utility.Utility;

/**
 * Main utility class for the Live Search Component.
 *
 * @author aclee
 *
 */
public class LookupDataUtility {
	private ExecutionContext etk = null;

	// Thread local variable containing this thread's previous lookup ID value.
	private static final ThreadLocal<String> previousLookupBk = ThreadLocal.withInitial(() -> "");

	// Thread local variable containing this thread's recursive depth counter.
	private static final ThreadLocal<Integer> recursionCounter = ThreadLocal.withInitial(() -> 1);

	/**
	 * Main constructor.
	 *
	 * @param thePageContext
	 *            The PageExecutionContext.
	 */
	public LookupDataUtility(final ExecutionContext thePageContext) {
		this.etk = thePageContext;
	}

	/**
	 * Get a null-safe column name.
	 *
	 * @param dataElement the data element
	 * @return null-safe column name.
	 */
	private static String getColName(final DataElement dataElement) {
		return dataElement == null ? "ID" : dataElement.getColumnName();
	}

	/**
	 * Replace select XYZ within a query with SELECT TOP.
	 *
	 * @param aString
	 *            A Query
	 * @return The modified query.
	 */
	private static String peformSelectReplacements(final String aString) {
		if (aString == null) {
			return null;
		}

		final Pattern p = Pattern.compile("(select)(\\s)+(?!top)", Pattern.CASE_INSENSITIVE);
		final Matcher m = p.matcher(aString);
		return m.replaceAll("SELECT TOP 2147483647 ");
	}

	/**
	 * Returns the search result table header.
	 *
	 * @param userHeaderParams
	 *            Table Column Names
	 * @return HTML table header.
	 */
	public static String writeTableHeader(final List<String> userHeaderParams) {
		final StringBuilder sb = new StringBuilder();

		sb.append("<thead><tr>");

		if (userHeaderParams != null) {
			for (final String userHeaderParam : userHeaderParams) {
				final String[] columnVars = userHeaderParam.split(":");

				if (columnVars != null && columnVars.length == 2) {
					sb.append("<th scope=\"col\" >");
					sb.append(getStringValue(columnVars[1]));
					sb.append("</th>");
				} else {
					throw new IllegalArgumentException("Error writing table header: \""
							+ userHeaderParam + "\" column header is malformed.");
				}
			}
		}

		sb.append("</tr></thead>");

		return sb.toString();
	}

	/**
	 * Converts a list of table rows headers to case insenstive.
	 *
	 * @param tableRow
	 *            A list of table rows.
	 * @return A case-insensitive list of table rows.
	 */
	private static Map<String, String> getCaseSensitiveHeaders(final Map<String, Object> tableRow) {

		final Map<String, String> caseSensitiveHeaders = new HashMap<>();

		if (tableRow == null) {
			return caseSensitiveHeaders;
		}

		for (final String aKey : tableRow.keySet()) {
			caseSensitiveHeaders.put(aKey.toLowerCase(), aKey);
		}

		return caseSensitiveHeaders;
	}

	/**
	 * Returns an escaped HTMP string.
	 *
	 * @param anObject
	 *            An Object.
	 * @return The escaped string.
	 */
	private static String getStringValue(final Object anObject) {
		if (anObject == null) {
			return "";
		} else {
			return StringEscapeUtils.escapeHtml(anObject.toString());
		}
	}

	/**
	 * Writes the result body for the search result.
	 *
	 * @param userHeaderParams
	 *            User header row names.
	 * @param tableRows
	 *            Table row data.
	 * @param dataElementId
	 *            The data element ID for the row in question.
	 * @return String HTML result table.
	 */
	public static String writeTableBody(final List<String> userHeaderParams,
			final List<Map<String, Object>> tableRows, final String dataElementId) {
		final StringBuilder sb = new StringBuilder();

		sb.append("<tbody>");

		Map<String, String> csh = null;

		for (final Map<String, Object> tableRow : tableRows) {

			if (csh == null) {
				csh = getCaseSensitiveHeaders(tableRow);
			}

			sb.append("<tr id=\"TR_");
			sb.append(dataElementId);
			sb.append("_");
			sb.append(getStringValue(tableRow.get(csh.get("value"))));
			sb.append("\" name=\"TR_");
			sb.append(dataElementId);
			sb.append("_");
			sb.append(getStringValue(tableRow.get(csh.get("display"))));
			sb.append("\" style=\"cursor:pointer;\">");

			if (userHeaderParams != null) {
				for (final String userHeaderParam : userHeaderParams) {
					final String[] columnVars = userHeaderParam.split(":");

					if (columnVars != null && columnVars.length == 2) {
						sb.append("<td><a class=\"searchTableClickableLink\" href=\"javascript:void(0)\" ");
						sb.append("title=\"Select search result value ");
						sb.append(getStringValue(tableRow.get(csh.get(columnVars[0].toLowerCase()))));
						sb.append("\" >");
						sb.append(getStringValue(tableRow.get(csh.get(columnVars[0].toLowerCase()))));
						sb.append("</a></td>");
					} else {
						throw new IllegalArgumentException("Error writing live search table row: \""
								+ userHeaderParam + "\" column header is malformed.");
					}
				}
			} else {
				throw new IllegalArgumentException("Error writing live search table rows: table headers are required.");
			}

			sb.append("</tr>");
		}

		sb.append("</tbody>");

		return sb.toString();
	}

	/**
	 * Gets the lookup query for a data object / data element id.
	 *
	 * @param dataFormKey
	 *            The data form key.
	 * @param dataElementName
	 *            The data element name.
	 * @param aLookupExecutionContext
	 *            context to pass to the lookup handler
	 * @return The SQL query for the data object with the given ID.
	 * @throws InstantiationException
	 *             Instantiation Exception.
	 * @throws IllegalAccessException
	 *             Illegal Access Exception.
	 * @throws ClassNotFoundException
	 *             Class Not Found Exception.
	 * @throws ApplicationException
	 *             Application Exception.
	 */
	public String getLookupQuery(final String dataFormKey,
			final String dataElementName,
			final LookupExecutionContext aLookupExecutionContext)
					throws InstantiationException,
					IllegalAccessException,
					ClassNotFoundException,
					ApplicationException {
		return getLookupQuery(dataFormKey, dataElementName, null, aLookupExecutionContext, true);
	}

	/**
	 * Gets the lookup query for a data element by business key. If multiple, pulls the first result. Returns data
	 * object queries with no start/stop date filtering on the query.
	 *
	 * @param dataElementBusinessKey
	 *            The data element business key.
	 * @param aLookupExecutionContext
	 *            context to pass to the lookup handler
	 * @return The SQL query for the data object with the given ID.
	 * @throws InstantiationException
	 *             Instantiation Exception.
	 * @throws IllegalAccessException
	 *             Illegal Access Exception.
	 * @throws ClassNotFoundException
	 *             Class Not Found Exception.
	 * @throws ApplicationException
	 *             Application Exception.
	 */
	public String getLookupQuery(final String dataElementBusinessKey,
			final LookupExecutionContext aLookupExecutionContext)
					throws InstantiationException,
					IllegalAccessException,
					ClassNotFoundException,
					ApplicationException {
		return getLookupQuery(null, null, dataElementBusinessKey, aLookupExecutionContext, false);
	}

	/**
	 * Gets the lookup query for a data object / data element id or by a data element by business key.
	 *
	 * @param dataFormBusinessKey
	 *            The data form business key.
	 * @param dataElementName
	 *            The data element name.
	 * @param dataElementBusinessKey
	 *            The data element business key.
	 * @param aLookupExecutionContext
	 *            context to pass to the lookup handler
	 * @param filterStartStopDate
	 *            whether to add stop/start date filtering to data object lookup queries.
	 *
	 * @return The SQL query for the data object with the given ID.
	 * @throws InstantiationException
	 *             Instantiation Exception.
	 * @throws IllegalAccessException
	 *             Illegal Access Exception.
	 * @throws ClassNotFoundException
	 *             Class Not Found Exception.
	 * @throws ApplicationException
	 *             Application Exception.
	 */
	private String getLookupQuery(final String dataFormBusinessKey,
			final String dataElementName,
			final String dataElementBusinessKey,
			final LookupExecutionContext aLookupExecutionContext,
			final boolean filterStartStopDate)
					throws InstantiationException,
					IllegalAccessException,
					ClassNotFoundException,
					ApplicationException {
		try {
			final boolean isSqlServer = Utility.isSqlServer(etk);

			final LookupDefinition lookupDefinition = getLookupDefinition(dataFormBusinessKey, dataElementName,
					dataElementBusinessKey);

			String returnString = null;
			final Workspace activeWorkspace = etk.getWorkspaceService().getActiveWorkspace();

			if (lookupDefinition.getSourceType() == LookupSourceType.DATA_OBJECT) {
				returnString = buildDataObjectLookupQuery(filterStartStopDate, lookupDefinition);

			} else if (lookupDefinition.getSourceType() == LookupSourceType.SQL_QUERY) {
				final SqlScriptLookupDefinition ssld = etk.getLookupDefinitionService()
						.getSqlScriptLookupDefinitionByBusinessKey(lookupDefinition.getBusinessKey());

				returnString = etk.getWorkspaceService().getCode(activeWorkspace, ssld.getScript());
			} else if (lookupDefinition.getSourceType() == LookupSourceType.SCRIPT) {
				final ScriptLookupDefinition sld = etk.getLookupDefinitionService()
						.getScriptLookupDefinitionByBusinessKey(lookupDefinition.getBusinessKey());

				final String javaScriptId = sld.getScript().getFullyQualifiedName();

				LookupHandler luHandler;
				luHandler = (LookupHandler) Class.forName(javaScriptId).getDeclaredConstructor().newInstance();

				returnString = luHandler.execute(aLookupExecutionContext);
			} else if (lookupDefinition.getSourceType() == LookupSourceType.SYSTEM_OBJECT) {
				final SystemObjectLookupDefinition sold = etk.getLookupDefinitionService()
						.getSystemObjectLookupDefinitionByBusinessKey(lookupDefinition.getBusinessKey());

				returnString = LookupDataUtility.getSystemObjectQuery(etk,
						SystemObjectType.getByLookupSystemObjectType(sold.getSystemObjectType()),
						SystemObjectDisplayFormat.getBySystemObjectDisplayFormat(sold.getSystemObjectDisplayFormat()));
			}

			if (isSqlServer) {
				return peformSelectReplacements(returnString);
			} else {
				return returnString;
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException
				| ClassNotFoundException e) {
			throw new GeneralRuntimeException(e);
		}
	}

	private String buildDataObjectLookupQuery(final boolean filterStartStopDate,
			final LookupDefinition lookupDefinition) {
		final DataObjectLookupDefinition dold = etk.getLookupDefinitionService()
				.getDataObjectLookupDefinitionByBusinessKey(lookupDefinition.getBusinessKey());

		final StringBuilder queryBuilder = new StringBuilder();

		queryBuilder.append("SELECT ");
		queryBuilder.append(getColName(dold.getDisplayElement()));
		queryBuilder.append(" AS DISPLAY, ");
		queryBuilder.append(getColName(dold.getValueElement()));
		queryBuilder.append(" AS VALUE FROM ");
		queryBuilder.append(dold.getDataObject().getTableName());
		queryBuilder.append(" WHERE 1=1 ");

		// For audit log we do not want to add this filtering. Form
		// filtering should do this automatically,
		// and we need to catch "from" values that are in the past.
		if (filterStartStopDate) {
			buildBeginningEndDateClause(dold, queryBuilder);
		}

		if (dold.getFilterScript() != null) {
			buildUserSqlFilterClause(dold, queryBuilder);
		}

		if (dold.getOrderByElement() != null) {
			buildOrderByClause(dold, queryBuilder);
		}

		return queryBuilder.toString();
	}

	private void buildBeginningEndDateClause(final DataObjectLookupDefinition dold, final StringBuilder queryBuilder) {
		if (Utility.isSqlServer(etk)) {
			if (dold.getStartDateElement() != null) {
				queryBuilder.append(" and (datediff (day, dbo.ETKF_getServerTime(), isNull(");
				queryBuilder.append(dold.getStartDateElement().getColumnName());
				queryBuilder.append(", dbo.ETKF_getServerTime())) <= 0) ");
			}

			if (dold.getEndDateElement() != null) {
				queryBuilder.append(" and (datediff (day, dbo.ETKF_getServerTime(), isNull(");
				queryBuilder.append(dold.getEndDateElement().getColumnName());
				queryBuilder.append(", dbo.ETKF_getServerTime() + 1)) > 0) ");
			}
		} else if (Utility.isPostgreSQL(etk)) {
			if (dold.getStartDateElement() != null) {
				queryBuilder.append(" AND ((date_trunc('day', ");
				queryBuilder.append(dold.getStartDateElement().getColumnName());
				queryBuilder.append(") <= date_trunc('day', ETKF_GETSERVERTIME())) OR ");
				queryBuilder.append(dold.getStartDateElement().getColumnName());
				queryBuilder.append(" IS NULL) ");
			}

			if (dold.getEndDateElement() != null) {
				queryBuilder.append(" AND ((date_trunc('day', ");
				queryBuilder.append(dold.getEndDateElement().getColumnName());
				queryBuilder.append(") > date_trunc('day', ETKF_GETSERVERTIME())) OR ");
				queryBuilder.append(dold.getEndDateElement().getColumnName());
				queryBuilder.append(" IS NULL) ");
			}
		} else {
			if (dold.getStartDateElement() != null) {
				queryBuilder.append(" AND ((trunc(");
				queryBuilder.append(dold.getStartDateElement().getColumnName());
				queryBuilder.append(") <= trunc(ETKF_GETSERVERTIME())) OR ");
				queryBuilder.append(dold.getStartDateElement().getColumnName());
				queryBuilder.append(" IS NULL) ");
			}

			if (dold.getEndDateElement() != null) {
				queryBuilder.append(" AND ((trunc(");
				queryBuilder.append(dold.getEndDateElement().getColumnName());
				queryBuilder.append(") > trunc(ETKF_GETSERVERTIME())) OR ");
				queryBuilder.append(dold.getEndDateElement().getColumnName());
				queryBuilder.append(" IS NULL) ");
			}
		}
	}

	private static void buildOrderByClause(final DataObjectLookupDefinition dold, final StringBuilder queryBuilder) {
		queryBuilder.append(" ORDER BY ");
		queryBuilder.append(dold.getOrderByElement().getColumnName());

		if (SortDirection.DESCENDING == dold.getOrderBy()) {
			queryBuilder.append(" DESC ");
		} else if (SortDirection.ASCENDING == dold.getOrderBy()) {
			queryBuilder.append(" ASC ");
		}
	}

	private void buildUserSqlFilterClause(final DataObjectLookupDefinition dold, final StringBuilder queryBuilder) {
		final String userSQL = etk.getWorkspaceService().getCode(
				etk.getWorkspaceService().getActiveWorkspace(), dold.getFilterScript());

		if (userSQL != null && StringUtility.isNotBlank(userSQL)) {
			queryBuilder.append(" AND ");
			queryBuilder.append(userSQL);
		}
	}

	private LookupDefinition getLookupDefinition(final String dataFormBusinessKey, final String dataElementName,
			final String dataElementBusinessKey) throws ApplicationException {
		LookupDefinition lookupDefinition = null;

		if (dataFormBusinessKey == null && dataElementName == null) {
			lookupDefinition = etk.getDataElementService()
					.getDataElementByBusinessKey(dataElementBusinessKey)
					.getLookup();
		} else {
			final Optional<FormControl> formControl = etk.getFormService()
					.getFormControls(etk.getFormService().getForm(dataFormBusinessKey))
					.stream()
					.filter(f -> f.getName().equalsIgnoreCase(dataElementName))
					.findAny();

			DataElement dataElement = null;

			if (formControl.isPresent()) {
				dataElement = etk.getFormService().getDataElement(formControl.get());
			}

			if (dataElement != null) {
				lookupDefinition = dataElement.getLookup();
			} else if (formControl.isPresent()) {
				lookupDefinition = etk.getFormService().getLookup(formControl.get());
			}
		}

		if (lookupDefinition == null) {
			throw new ApplicationException("Critical error with getLookupQuery. Could not find lookup definition. " +
					"dataFormBusinessKey = \"" + dataFormBusinessKey +
					"\", dataElementName = \"" + dataElementName +
					"\", dataElementBusinessKey = \"" + dataElementBusinessKey + "\"");
		}
		return lookupDefinition;
	}

	public static String getSystemObjectQuery(final ExecutionContext etk,
			final SystemObjectType theSystemObjectType,
			final SystemObjectDisplayFormat theSystemObjectDisplayFormat)
					throws ApplicationException {
		if (SystemObjectType.USER == theSystemObjectType) {
			if (SystemObjectDisplayFormat.ACCOUNT_NAME == theSystemObjectDisplayFormat) {
				return " select username as DISPLAY, user_id as VALUE from etk_user order by lower(username) ";
			} else if (SystemObjectDisplayFormat.LASTNAME_FIRSTNAME_MI == theSystemObjectDisplayFormat) {
				if (Utility.isSqlServer(etk)) {
					return " select p.last_name + ', ' + p.first_name + "
							+ " case when p.middle_name is null then '' else ' ' + SUBSTRING (p.middle_name, 1, 1) end as DISPLAY, "
							+ " user_id as VALUE "
							+ " from etk_user u "
							+ " join etk_person p on p.person_id = u.person_id "
							+ " order by lower(p.last_name) ";
				} else {
					return " select p.last_name || ', ' || p.first_name ||  "
							+ " case when p.middle_name is null then '' else ' ' || SUBSTR(p.middle_name, 1, 1) end as DISPLAY, user_id as VALUE "
							+ " from etk_user u "
							+ " join etk_person p on p.person_id = u.person_id "
							+ " order by lower(p.last_name) ";
				}
			} else {
				throw new ApplicationException("Unknown SYSTEM_OBJECT_DISPLAY_FORMAT encountered with value = " +
						theSystemObjectDisplayFormat);
			}
		} else {
			throw new ApplicationException("Unknown SYSTEM_OBJECT_TYPE encountered with value = " +
					theSystemObjectType);
		}
	}

	private static Integer intValue(final Object aNumber) {
		if (aNumber == null) {
			return null;
		} else if (aNumber instanceof Number) {
			return ((Number) aNumber).intValue();
		}

		return null;
	}

	private static Long longValue(final Object aNumber) {
		if (aNumber == null) {
			return null;
		} else if (aNumber instanceof Number) {
			return ((Number) aNumber).longValue();
		}

		return null;
	}

	private static Boolean booleanValue(final Object aBoolean) {
		if (aBoolean == null) {
			return null;
		} else if (aBoolean instanceof Number) {
			return ((Number) aBoolean).intValue() == 1;
		}

		return null;
	}

	public static Number getEtkDataObjectIdByBusinessKey(final ExecutionContext theEtk, final String businessKey) {
		try {
			return (Number) theEtk.createSQL("select DATA_OBJECT_ID from etk_data_object "
					+ "where BUSINESS_KEY = :business_key AND TRACKING_CONFIG_ID = "
					+ "(select max(tracking_config_id) from ETK_TRACKING_CONFIG_ARCHIVE)")
					.returnEmptyResultSetAs(null)
					.setParameter("business_key", businessKey)
					.fetchObject();
		} catch (final IncorrectResultSizeDataAccessException e) {
			theEtk.getLogger().error("Error retrieving data object with business key " + businessKey, e);
			return -1L;
		}
	}

	public static Number getEtkLookupDefinitionByBusinessKey(final ExecutionContext theEtk, final String businessKey) {
		try {
			return (Number) theEtk.createSQL("select LOOKUP_DEFINITION_ID from etk_lookup_definition "
					+ "where BUSINESS_KEY = :business_key AND TRACKING_CONFIG_ID = "
					+ "(select max(tracking_config_id) from ETK_TRACKING_CONFIG_ARCHIVE)")
					.returnEmptyResultSetAs(null)
					.setParameter("business_key", businessKey)
					.fetchObject();
		} catch (final IncorrectResultSizeDataAccessException e) {
			theEtk.getLogger().error("Error retrieving lookup with business key " + businessKey, e);
			return -1L;
		}
	}

	public static AeaEtkDataObject getEtkDataObjectById(final ExecutionContext theEtk, final Number anEtkDataObjectId,
			final boolean loadElements) {
		Map<String, Object> etkDataObjectInfo = null;

		if (anEtkDataObjectId == null) {
			return null;
		}

		try {
			// APINOW
			etkDataObjectInfo = theEtk.createSQL("select * from etk_data_object "
					+ "where DATA_OBJECT_ID = :dataObjectId ")
					.returnEmptyResultSetAs(null)
					.setParameter("dataObjectId", anEtkDataObjectId)
					.fetchMap();
		} catch (final Exception e) {
			theEtk.getLogger().error("Could not retrieve ETK_DATA_OBJECT with DATA_OBJECT_ID = " + anEtkDataObjectId, e);
			return null;
		}

		if (etkDataObjectInfo == null) {
			theEtk.getLogger().error("No ETK_DATA_OBJECT with DATA_OBJECT_ID = " + anEtkDataObjectId + " found.");
			return null;
		}

		return getEtkDataObject(theEtk, etkDataObjectInfo, loadElements);
	}

	public static AeaEtkDataObject getEtkDataObjectByTableName(final ExecutionContext theEtk, final String aTableName) {
		Map<String, Object> etkDataObjectInfo = null;

		if (aTableName == null) {
			return null;
		}

		try {
			// APINOW
			etkDataObjectInfo = theEtk.createSQL("select * from etk_data_object "
					+ "where table_name = :tableName "
					+ "and tracking_config_id = "
					+ "(select max(tracking_config_id) from etk_tracking_config_archive) ")
					.returnEmptyResultSetAs(null)
					.setParameter("tableName", aTableName)
					.fetchMap();
		} catch (final Exception e) {
			theEtk.getLogger().error("Could not retrieve ETK_DATA_OBJECT with TABLE_NAME = " + aTableName, e);
			return null;
		}

		if (etkDataObjectInfo == null) {
			theEtk.getLogger().error("No ETK_DATA_OBJECT with TABLE_NAME = " + aTableName + " found.");
			return null;
		}

		return getEtkDataObject(theEtk, etkDataObjectInfo, true);
	}

	public static AeaEtkDataElement getEtkDataElementById(final ExecutionContext theEtk,
			final AeaEtkDataObject theEtkDataObject,
			final Number etkDataElementId) {
		String etkDataElementBusinessKey;

		if (etkDataElementId == null) {
			return null;
		}

		try {
			// APINOW
			etkDataElementBusinessKey = theEtk.createSQL("select business_key from etk_data_element "
					+ "where DATA_ELEMENT_ID = :dataElementId")
					.returnEmptyResultSetAs(null)
					.setParameter("dataElementId", etkDataElementId)
					.fetchString();
		} catch (final Exception e) {
			theEtk.getLogger().error("Could not retrieve ETK_DATA_ELEMENT with DATA_ELEMENT_ID = " + etkDataElementId, e);
			return null;
		}

		if (etkDataElementBusinessKey == null) {
			theEtk.getLogger().error("No ETK_DATA_ELEMENT with DATA_ELEMENT_ID = " + etkDataElementId + " found.");
			return null;
		}

		return getEtkDataElement(theEtk, theEtkDataObject,
				theEtk.getDataElementService().getDataElementByBusinessKey(etkDataElementBusinessKey));
	}

	public static AeaEtkLookupDefinition getEtkLookupDefinitionById(final ExecutionContext theEtk,
			final Number etkLookupDefinitionId) {
		Map<String, Object> etkLookupDefinitionInfo = null;

		if (etkLookupDefinitionId == null) {
			return null;
		}

		try {
			// APINOW
			etkLookupDefinitionInfo = theEtk.createSQL("select * from ETK_LOOKUP_DEFINITION "
					+ "where LOOKUP_DEFINITION_ID = :lookupDefinitionId ")
					.returnEmptyResultSetAs(null)
					.setParameter("lookupDefinitionId", etkLookupDefinitionId)
					.fetchMap();
		} catch (final Exception e) {
			theEtk.getLogger().error(
					"Could not retrieve ETK_LOOKUP_DEFINITION with LOOKUP_DEFINITION_ID = " + etkLookupDefinitionId, e);
			return null;
		}

		if (etkLookupDefinitionInfo == null) {
			theEtk.getLogger().error(
					"No ETK_LOOKUP_DEFINITION with LOOKUP_DEFINITION_ID = " + etkLookupDefinitionId + " found.");
			return null;
		}

		return getEtkLookupDefinition(theEtk, etkLookupDefinitionInfo);
	}

	private static void populateEtkDataElements(final ExecutionContext theEtk,
			final AeaEtkDataObject theEtkDataObject) {

		if (theEtkDataObject.getBusinessKey() == null) {
			return;
		}

		final Collection<DataElement> dataElements = theEtk.getDataElementService().getDataElements(
				theEtk.getDataObjectService().getDataObjectByBusinessKey(theEtkDataObject.getBusinessKey()));

		if (dataElements.isEmpty()) {
			theEtkDataObject.setDataElements(new ArrayList<>());

			theEtk.getLogger().error("No ETK_DATA_ELEMENTS for ETK_DATA_OBJECT with DATA_OBJECT_ID = "
					+ theEtkDataObject.getDataObjectId() + " found - blank data object?");
		} else {
			final List<AeaEtkDataElement> elements = new ArrayList<>();

			for (final DataElement dataElement : dataElements) {
				elements.add(getEtkDataElement(theEtk, theEtkDataObject, dataElement));
			}

			theEtkDataObject.setDataElements(elements);
		}
	}

	private static AeaEtkDataObject getEtkDataObject(final ExecutionContext theEtk,
			final Map<String, Object> etkDataObjectInfo, final boolean loadElements) {

		final AeaEtkDataObject edo = new AeaEtkDataObject();

		edo.setBusinessKey((String) etkDataObjectInfo.get("BUSINESS_KEY"));
		edo.setCardinality(intValue(etkDataObjectInfo.get("CARDINALITY")));
		edo.setDataObjectId(longValue(etkDataObjectInfo.get("DATA_OBJECT_ID")));

		if (longValue(etkDataObjectInfo.get("OBJECT_TYPE")) != null) {
			edo.setDataObjectType(DataObjectType.getDataObjectType(longValue(etkDataObjectInfo.get("OBJECT_TYPE"))));
		} else {
			edo.setDataObjectType(null);
		}

		edo.setDescription((String) etkDataObjectInfo.get("DESCRIPTION"));
		edo.setIsAppliedChanges(booleanValue(etkDataObjectInfo.get("APPLIED_CHANGES")));
		edo.setIsBaseObject(booleanValue(etkDataObjectInfo.get("BASE_OBJECT")));
		edo.setIsDocumentManagementEnabled(booleanValue(etkDataObjectInfo.get("DOCUMENT_MANAGEMENT_ENABLED")));
		edo.setIsDocumentManagementObject(booleanValue(etkDataObjectInfo.get("DOCUMENT_MANAGEMENT_OBJECT")));
		edo.setIsSearchable(booleanValue(etkDataObjectInfo.get("SEARCHABLE")));
		edo.setIsSeperateInbox(booleanValue(etkDataObjectInfo.get("SEPARATE_INBOX")));
		edo.setIsAutoAssignment(booleanValue(etkDataObjectInfo.get("AUTO_ASSIGNMENT")));
		edo.setLabel((String) etkDataObjectInfo.get("LABEL"));
		edo.setListOrder(intValue(etkDataObjectInfo.get("LIST_ORDER")));
		edo.setListStyle(intValue(etkDataObjectInfo.get("LIST_STYLE")));
		edo.setDesignator(intValue(etkDataObjectInfo.get("DESIGNATOR")));
		edo.setName((String) etkDataObjectInfo.get("NAME"));
		edo.setObjectName((String) etkDataObjectInfo.get("OBJECT_NAME"));
		edo.setParentObjectId(longValue(etkDataObjectInfo.get("PARENT_OBJECT_ID")));
		edo.setTableName((String) etkDataObjectInfo.get("TABLE_NAME"));
		edo.setTableSpace((String) etkDataObjectInfo.get("TABLE_SPACE"));
		edo.setTrackingConfigId(longValue(etkDataObjectInfo.get("TRACKING_CONFIG_ID")));

		if (loadElements) {
			populateEtkDataElements(theEtk, edo);
		}

		return edo;
	}

	private static AeaEtkDataElement getEtkDataElement(final ExecutionContext theEtk,
			final AeaEtkDataObject theEtkDataObject,
			final DataElement dataElement) {
		final AeaEtkDataElement ede = new AeaEtkDataElement();

		ede.setBusinessKey(dataElement.getBusinessKey());
		ede.setColumnName(dataElement.getColumnName());

		if (dataElement.getDataType() != null) {
			ede.setDataType(DataElementType.getDataElementType(dataElement.getDataType()));
		} else {
			ede.setDataType(null);
		}

		if (theEtkDataObject == null) {
			ede.setEtkDataObject(
					getEtkDataObjectById(theEtk,
							getEtkDataObjectIdByBusinessKey(theEtk, dataElement.getDataObject().getBusinessKey()), false));
		} else {
			ede.setEtkDataObject(theEtkDataObject);
		}

		if (dataElement.getLookup() != null) {

			// Prevent infinite lookup recursion
			if (dataElement.getLookup().getBusinessKey().equals(previousLookupBk.get())) {
				recursionCounter.set(recursionCounter.get() + 1);
			} else {
				recursionCounter.remove();
				previousLookupBk.set(dataElement.getLookup().getBusinessKey());
			}

			if (recursionCounter.get() < 10) {
				ede.setEtkLookupDefinition(getEtkLookupDefinitionById(theEtk,
						getEtkLookupDefinitionByBusinessKey(theEtk,
								dataElement.getLookup().getBusinessKey())));
			} else {
				theEtk.getLogger()
				.error("LookupDataUtility:getEtkDataElement warning: Data Object \""
						+ Optional.ofNullable(theEtkDataObject).map(AeaEtkDataObject::getName).orElse(null)
						+ "\" contains a self referenced lookup, skipping at recursive depth of 10.");

				recursionCounter.remove();
				ede.setEtkLookupDefinition(null);
			}
		} else {
			ede.setEtkLookupDefinition(null);
		}

		ede.setIsBoundToLookup(dataElement.isBoundToLookup());
		ede.setmTableName(dataElement.getTableName());
		ede.setName(dataElement.getName());

		return ede;
	}

	private static AeaEtkLookupDefinition getEtkLookupDefinition(final ExecutionContext theEtk,
			final Map<String, Object> etkLookupDefinitionInfo) {
		final AeaEtkLookupDefinition ld = new AeaEtkLookupDefinition();

		ld.setAscendingOrder(booleanValue(etkLookupDefinitionInfo.get("ASCENDING_ORDER")));
		ld.setBusiness_key((String) etkLookupDefinitionInfo.get("BUSINESS_KEY"));
		ld.setDescription((String) etkLookupDefinitionInfo.get("DESCRIPTION"));

		AeaEtkDataObject ldDataObject = null;

		if (etkLookupDefinitionInfo.get("DATA_OBJECT_ID") != null) {
			ldDataObject = getEtkDataObjectById(theEtk, longValue(etkLookupDefinitionInfo.get("DATA_OBJECT_ID")),
					false);
		}

		if (etkLookupDefinitionInfo.get("DISPLAY_ELEMENT_ID") != null) {
			ld.setDisplayElement(getEtkDataElementById(theEtk, ldDataObject,
					longValue(etkLookupDefinitionInfo.get("DISPLAY_ELEMENT_ID"))));
		} else {
			ld.setDisplayElement(null);
		}
		if (etkLookupDefinitionInfo.get("END_DATE_ELEMENT_ID") != null) {
			ld.setEndDateElement(getEtkDataElementById(theEtk, ldDataObject,
					longValue(etkLookupDefinitionInfo.get("END_DATE_ELEMENT_ID"))));
		} else {
			ld.setEndDateElement(null);
		}

		ld.setEtkDataObject(ldDataObject);
		ld.setLookupDefinitonId(longValue(etkLookupDefinitionInfo.get("LOOKUP_DEFINITION_ID")));
		ld.setLookupSql((String) etkLookupDefinitionInfo.get("LOOKUP_SQL"));

		if (etkLookupDefinitionInfo.get("LOOKUP_SOURCE_TYPE") != null) {
			ld.setLookupType(
					net.micropact.aea.utility.LookupSourceType.getLookupSourceType(
							longValue(etkLookupDefinitionInfo.get("LOOKUP_SOURCE_TYPE"))));
		} else {
			ld.setLookupType(null);
		}

		ld.setName((String) etkLookupDefinitionInfo.get("NAME"));

		if (etkLookupDefinitionInfo.get("ORDER_BY_ELEMENT_ID") != null) {
			ld.setOrderByElement(getEtkDataElementById(theEtk, ldDataObject,
					longValue(etkLookupDefinitionInfo.get("ORDER_BY_ELEMENT_ID"))));
		} else {
			ld.setOrderByElement(null);
		}

		ld.setPluginRegistrationId(longValue(etkLookupDefinitionInfo.get("PLUGIN_REGISTRATION_ID")));
		ld.setSqlScriptObjectId(longValue(etkLookupDefinitionInfo.get("SQL_SCRIPT_OBJECT_ID")));

		if (etkLookupDefinitionInfo.get("START_DATE_ELEMENT_ID") != null) {
			ld.setStartDateElement(getEtkDataElementById(theEtk, ldDataObject,
					longValue(etkLookupDefinitionInfo.get("START_DATE_ELEMENT_ID"))));
		} else {
			ld.setStartDateElement(null);
		}

		ld.setTrackingConfigId(longValue(etkLookupDefinitionInfo.get("TRACKING_CONFIG_ID")));

		if (etkLookupDefinitionInfo.get("VALUE_ELEMENT_ID") != null) {
			ld.setValueElement(getEtkDataElementById(theEtk, ldDataObject,
					longValue(etkLookupDefinitionInfo.get("VALUE_ELEMENT_ID"))));
		} else {
			ld.setValueElement(null);
		}

		if (etkLookupDefinitionInfo.get("VALUE_RETURN_TYPE") != null) {
			ld.setValueReturnType(
					DataElementType.getDataElementType(longValue(etkLookupDefinitionInfo.get("VALUE_RETURN_TYPE"))));
		} else {
			ld.setValueReturnType(null);
		}

		if (etkLookupDefinitionInfo.get("SYSTEM_OBJECT_TYPE") != null) {
			ld.setSystemObjectType(
					SystemObjectType.getById(intValue(etkLookupDefinitionInfo.get("SYSTEM_OBJECT_TYPE"))));
		} else {
			ld.setSystemObjectType(null);
		}

		if (etkLookupDefinitionInfo.get("SYSTEM_OBJECT_DISPLAY_FORMAT") != null) {
			ld.setSystemObjectDisplayFormat(SystemObjectDisplayFormat
					.getById(intValue(etkLookupDefinitionInfo.get("SYSTEM_OBJECT_DISPLAY_FORMAT"))));
		} else {
			ld.setSystemObjectDisplayFormat(null);
		}

		ld.setEnableCaching(booleanValue(etkLookupDefinitionInfo.get("ENABLE_CACHING")));

		return ld;
	}

	/**
	 * Method to convert a string value to a typed object based on the lookups defined return type.
	 *
	 * @param theEtk
	 *            entellitrak execution context
	 * @param dataValue
	 *            String representation of the lookup's value.
	 * @param dataType
	 *            The dataType of the lookup.
	 * @return Long/Double/java.sql.Date/String/null representation of the object.
	 */
	public static Object convertStringToTypedObject(final ExecutionContext theEtk, final String dataValue,
			final DataType dataType) {
		Object returnValue = null;

		if (dataValue == null || dataType == null) {
			return null;
		} else if (dataType == DataType.NUMBER ||
				dataType == DataType.YES_NO ||
				dataType == DataType.FILE) {
			try {
				returnValue = Long.valueOf(dataValue);
			} catch (final Exception e) {
				theEtk.getLogger().error(
						String.format("convertStringToTypedObject - Could not convert \"%s\" with DataType %s to Long data type.",
								dataValue,
								dataType),
						e);
			}
		} else if (dataType == DataType.CURRENCY) {
			try {
				returnValue = Double.valueOf(dataValue);
			} catch (final Exception e) {
				theEtk.getLogger().error(
						String.format("convertStringToTypedObject - Could not convert \"%s\" with DataType %s to Double data type.",
								dataValue,
								dataType),
						e);
			}
		} else if (dataType == DataType.DATE) {
			try {
				returnValue = new java.sql.Date(DateUtility.parseDate(dataValue).getTime());
			} catch (final Exception e) {
				theEtk.getLogger().error(
						String.format("convertStringToTypedObject - Could not convert \"%s\" with DataType %s to java.sql.Date data type.",
								dataValue,
								dataType),
						e);
			}
		} else if (dataType == DataType.TIMESTAMP) {
			try {
				returnValue = new java.sql.Date(DateUtility.parseDateTime(dataValue).getTime());
			} catch (final Exception e) {
				theEtk.getLogger().error(
						String.format("convertStringToTypedObject - Could not convert \"%s\" with DataType %s to java.sql.Date data type.",
								dataValue,
								dataType),
						e);
			}
		} else {
			returnValue = dataValue;
		}

		return returnValue;
	}

}
