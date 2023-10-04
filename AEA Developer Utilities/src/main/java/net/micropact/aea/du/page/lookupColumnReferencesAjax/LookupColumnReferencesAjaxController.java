package net.micropact.aea.du.page.lookupColumnReferencesAjax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.LookupDefinition;
import com.entellitrak.configuration.LookupDefinitionService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.ContentType;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.TextResponse;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.entellitrak.aea.gl.api.java.map.MapBuilder;
import net.micropact.aea.core.lookup.LookupMetadata;
import net.micropact.aea.core.lookup.LookupMetadata.TableColumn;
import net.micropact.aea.utility.LookupSourceType;
import net.micropact.aea.utility.Utility;

/**
 * This page returns metadata surrounding lookups in JSON format. It is primarily concerned with returning the Table and
 * Column that a lookup refers to.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class LookupColumnReferencesAjaxController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		response.setContentType(ContentType.JSON);
		response.put("out", new Gson().toJson(getLookupMetadata(etk)));

		return response;
	}

	/**
	 * Gets metadata for all of the lookups within this instance of entellitrak.
	 *
	 * @param etk entellitrak execution context
	 * @return Information about lookups in the form
	 *     <pre>
	 *     [{LOOKUP_BUSINESS_KEY: String,
	 *       NAME: String,
	 *       lookupBusinessKey: String,
	 *       tableName: String,
	 *       columnName: String,
	 *       tableColumnExists: Boolean,
	 *       lookupSourceTypeDisplay: String}]
	 *     </pre>

	 */
	private static List<Map<String, Object>> getLookupMetadata(final ExecutionContext etk) {
		final LookupDefinitionService lookupDefinitionService = etk.getLookupDefinitionService();

		return lookupDefinitionService.getLookupDefinitions()
				.stream()
				.map(lookupDefinition -> {
					final Map<String, Object> lookupData = new HashMap<>();

					lookupData.put("NAME", lookupDefinition.getName());
					lookupData.put("LOOKUP_BUSINESS_KEY", lookupDefinition.getBusinessKey());

					final Map<String, Object> tableColumnData = getTableColumnData(etk, lookupDefinition);
					lookupData.putAll(tableColumnData);

					lookupData.put("tableColumnExists", tableColumnExists(etk, (String) tableColumnData.get("tableName"), (String) tableColumnData.get("columnName")));
					lookupData.put("lookupSourceTypeDisplay", LookupSourceType.getLookupSourceTypeByCoreLookupSourceType(lookupDefinition.getSourceType()).getDisplay());

					return lookupData;
				}).collect(Collectors.toList());
	}

	/**
	 * This function takes a lookupDefinition key and returns a map with tableName and columnName
	 * which will indicate the table and column which the lookup pulls its values from.
	 *
	 * @param etk entellitrak execution context
	 * @param lookupDefinition A Map containing a description of the lookup definition
	 */
	private static Map<String, Object> getTableColumnData(final ExecutionContext etk, final LookupDefinition lookupDefinition) {
		final TableColumn tableColumn = LookupMetadata.getLookupReference(etk, lookupDefinition);

		return new MapBuilder<String, Object>()
				.put("tableName", tableColumn == null ? null : tableColumn.getTable())
				.put("columnName", tableColumn == null ? null : tableColumn.getColumn())
				.build();
	}

	/**
	 * This method checks to see whether a particular table and column exist within the database.
	 *
	 * @param etk entellitrak execution context
	 * @param tableName name of the table
	 * @param columnName name of the column
	 * @return null if tableName and columnName are null, otherwise true if the table/column exists in the database and false if it does not.
	 */
	private static Boolean tableColumnExists(final ExecutionContext etk,
			final String tableName,
			final String columnName) {
		try {
			if (tableName == null || columnName == null) {
				return null;
			} else {
				return 1 == etk.createSQL(Utility.isSqlServer(etk) || Utility.isPostgreSQL(etk)
						? "SELECT COUNT(*) FROM information_schema.columns WHERE UPPER(table_name) = :tableName AND UPPER(column_name) = :columnName"
								: "SELECT COUNT(*) FROM ALL_TAB_COLUMNS WHERE UPPER(table_name) = :tableName AND UPPER(column_name) = :columnName")
						.setParameter("tableName", tableName)
						.setParameter("columnName", columnName)
						.fetchLong();
			}
		} catch (final IncorrectResultSizeDataAccessException e) {
			throw new GeneralRuntimeException(e);
		}
	}
}
