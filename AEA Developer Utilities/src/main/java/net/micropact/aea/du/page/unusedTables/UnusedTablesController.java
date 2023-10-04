package net.micropact.aea.du.page.unusedTables;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.entellitrak.platform.DatabasePlatform;
import com.google.gson.Gson;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.query.QueryUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

/**
 * This page is used for displaying tables which exist in the database but do not appear to be being used by
 * entellitrak. This commonly occurs when a data object is deleted from the front end because entellitrak will not drop
 * the table.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class UnusedTablesController implements PageController {

	private static final Set<String> EXCLUDED_TABLES_LOWERCASE = Set.of("databasechangelog", "databasechangeloglock");

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Unused Tables",
								"page.request.do?page=du.page.unusedTables")));

		final String query = Map.of(DatabasePlatform.POSTGRESQL, "SELECT tables.table_name FROM information_schema.tables tables WHERE tables.table_type = 'BASE TABLE' AND tables.table_schema NOT IN('pg_catalog', 'information_schema')",
				DatabasePlatform.SQL_SERVER, "SELECT tables.table_name FROM information_schema.tables tables WHERE tables.table_type = 'BASE TABLE' AND tables.table_name NOT IN('sysdiagrams')",
				DatabasePlatform.ORACLE, "SELECT tables.table_name FROM user_tables tables WHERE /* Core has a temporary table HT_ETK_FORM_CONTROL */ tables.temporary != 'Y'")
				.get(etk.getPlatformInfo().getDatabasePlatform());

		final List<String> unusedTables = QueryUtility.<String>toSimpleList(etk.createSQL(
				query)
				.fetchList())
				.stream()
				.filter(tableName -> !(
						isSpecialCoreTable(tableName)
						|| isEtkTable(tableName)
						|| isDataObjectTable(etk, tableName)
						|| isMultiselectTable(etk, tableName)))
				.sorted()
				.collect(Collectors.toList());

		final Gson gson = new Gson();
		response.put("unusedTables", gson.toJson(unusedTables));

		return response;
	}

	private static boolean isSpecialCoreTable(final String tableName) {
		return EXCLUDED_TABLES_LOWERCASE.contains(tableName.toLowerCase());
	}

	private static boolean isDataObjectTable(final ExecutionContext etk, final String tableName) {
		final DataObjectService dataObjectService = etk.getDataObjectService();

		return dataObjectService.getDataObjects().stream()
				.anyMatch(dataObject -> Objects.equals(tableName.toUpperCase(), dataObject.getTableName()));
	}

	private static boolean isMultiselectTable(final ExecutionContext etk, final String tableName) {
		final DataObjectService dataObjectService = etk.getDataObjectService();
		final DataElementService dataElementService = etk.getDataElementService();

		return dataObjectService.getDataObjects().stream()
				.flatMap(dataObject -> dataElementService.getDataElements(dataObject).stream())
				.anyMatch(dataElement -> Objects.equals(tableName.toUpperCase(), dataElement.getTableName()));
	}

	private static boolean isEtkTable(final String tableName) {
		return tableName.toUpperCase().startsWith("ETK_");
	}
}
