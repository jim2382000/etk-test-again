package net.micropact.aea.du.page.unusedColumns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.configuration.DataElementService;
import com.entellitrak.configuration.DataObject;
import com.entellitrak.configuration.DataObjectService;
import com.entellitrak.configuration.DataType;
import com.entellitrak.configuration.ObjectType;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.entellitrak.platform.DatabasePlatform;
import com.entellitrak.platform.PlatformInfo;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.etk.DataObjectUtil;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.query.QueryUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/**
 * This page attempts to find all columns which exist in the database but do not appear to be being used by entellitrak.
 * This is a common occurrence when a data element is deleted through the front-end because entellitrak will not
 * automatically delete the database column.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class UnusedColumnsController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final PlatformInfo platformInfo = etk.getPlatformInfo();
		final DataObjectService dataObjectService = etk.getDataObjectService();

		final TextResponse response = etk.createTextResponse();

		setBreadcrumbAndTitle(response);

		final Set<TableColumn> expectedTableColumns = dataObjectService.getDataObjects()
				.stream()
				.flatMap(dataObject -> getExpectedColumnsForDataObject(etk, dataObject))
				.collect(Collectors.toSet());

		final String allColumnsSqlQuery = Map.of(
				DatabasePlatform.POSTGRESQL, "SELECT table_name, column_name FROM information_schema.columns",
				DatabasePlatform.SQL_SERVER, "SELECT table_name, column_name FROM information_schema.columns",
				DatabasePlatform.ORACLE, "SELECT table_name, column_name FROM user_tab_cols WHERE virtual_column NOT IN ('YES')"
				).get(platformInfo.getDatabasePlatform());

		final List<TableColumn> databaseColumns = etk.createSQL(allColumnsSqlQuery)
				.fetchList()
				.stream()
				.map(row -> new TableColumn(((String) row.get("table_name")).toUpperCase(), ((String) row.get("column_name")).toUpperCase()))
				.sorted(Comparator.comparing(TableColumn::getTableName).thenComparing(Comparator.comparing(TableColumn::getColumnName)))
				.collect(Collectors.toList());

		final Set<String> tablesWeCareAbout = expectedTableColumns
				.stream()
				.map(TableColumn::getTableName)
				.collect(Collectors.toSet());

		final List<TableColumn> extraDatabaseColumns = databaseColumns.stream()
				.filter(dbColumn -> tablesWeCareAbout.contains(dbColumn.getTableName()))
				.filter(dbColumn -> !expectedTableColumns.contains(dbColumn))
				.collect(Collectors.toList());

		/*
		 * Indexes: Note, this is not supported in Oracle
		 */
		final List<Map<String, Object>> unusedColumns = extraDatabaseColumns.stream()
				.map(tableColumn -> {
					final List<String> indexes;

					if (Utility.isPostgreSQL(etk)) {
						indexes = Collections.emptyList();
					} else if (Objects.equals(Boolean.TRUE, Utility.isSqlServer(etk))) {
						indexes = QueryUtility.toSimpleList(etk.createSQL("SELECT indexes.NAME FROM sys.indexes indexes "
								+ "JOIN sys.index_columns indexColumns ON indexColumns.index_id = indexes.index_id "
								+ "AND indexColumns.object_id = indexes.object_id JOIN sys.columns columns "
								+ "ON columns.column_id = indexColumns.column_id "
								+ "AND columns.object_id = indexColumns.object_id "
								+ "JOIN sys.tables tables ON tables.object_id = columns.object_id "
								+ "WHERE columns.name = :columnName AND tables.name = :tableName "
								+ "ORDER BY indexes.name, indexes.index_id")
								.setParameter("tableName", tableColumn.getTableName())
								.setParameter("columnName", tableColumn.getColumnName())
								.fetchList());
					} else {
						indexes = Collections.emptyList();
					}

					return Utility.arrayToMap(String.class, Object.class, new Object[][] {
						{"TABLE_NAME", tableColumn.getTableName()},
						{"COLUMN_NAME", tableColumn.getColumnName()},
						{"indexes", indexes}
					});
				})
				.collect(Collectors.toList());

		response.put("unusedColumns", new Gson().toJson(unusedColumns));

		return response;
	}

	private static void setBreadcrumbAndTitle(final TextResponse response) {
		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Unused Columns",
								"page.request.do?page=du.page.unusedColumns")));
	}

	private static Stream<TableColumn> getExpectedColumnsForDataObject(final ExecutionContext etk, final DataObject dataObject) {
		final DataElementService dataElementService = etk.getDataElementService();

		final List<TableColumn> tableColumns = new ArrayList<>();

		/* Every data object has id */
		tableColumns.add(new TableColumn(dataObject.getTableName(), "ID"));

		/* Base Tracked Objects have workflow columns */
		if(Objects.equals(ObjectType.TRACKING, dataObject.getObjectType())
				&& DataObjectUtil.isBaseDataObject(etk, dataObject)) {
			tableColumns.add(new TableColumn(dataObject.getTableName(), "ID_ARCHIVE"));
			tableColumns.add(new TableColumn(dataObject.getTableName(), "ID_HIERARCHY"));
			tableColumns.add(new TableColumn(dataObject.getTableName(), "ID_WORKFLOW"));
			tableColumns.add(new TableColumn(dataObject.getTableName(), "STATE_LABEL"));
		}

		/* Child Objects have Parent and Base */
		if(!DataObjectUtil.isBaseDataObject(etk, dataObject)) {
			tableColumns.add(new TableColumn(dataObject.getTableName(), "ID_BASE"));
			tableColumns.add(new TableColumn(dataObject.getTableName(), "ID_PARENT"));
		}

		/* Get data element columns */
		dataElementService.getDataElements(dataObject)
		.forEach(dataElement -> tableColumns.addAll(getTableColumnsForDataElement(dataObject, dataElement)));

		return tableColumns.stream();
	}

	private static Set<TableColumn> getTableColumnsForDataElement(final DataObject dataObject, final DataElement dataElement) {
		final Set<TableColumn> returnValue = new HashSet<>();

		if(dataElement.isMultiValued().booleanValue()) {
			/* Multiselects */
			returnValue.add(new TableColumn(dataElement.getTableName(), "ID"));
			returnValue.add(new TableColumn(dataElement.getTableName(), "ID_OWNER"));
			returnValue.add(new TableColumn(dataElement.getTableName(), "LIST_ORDER"));
			returnValue.add(new TableColumn(dataElement.getTableName(), dataElement.getColumnName()));
		} else if(Objects.equals(DataType.PASSWORD, dataElement.getDataType())) {
			/* Passwords */
			returnValue.add(new TableColumn(dataObject.getTableName(), dataElement.getColumnName()));
			returnValue.add(new TableColumn(dataObject.getTableName(), String.format("%s_DTS", dataElement.getColumnName())));
			returnValue.add(new TableColumn(dataObject.getTableName(), String.format("%s_UID", dataElement.getColumnName())));
		} else if(dataElement.isStoredInDocumentManagement().booleanValue()) {
			returnValue.add(new TableColumn(dataObject.getTableName(), "ETK_DM_CONTAINER_ID"));
			returnValue.add(new TableColumn(dataObject.getTableName(), "ETK_RESOURCE_CONTAINER"));
			returnValue.add(new TableColumn(dataObject.getTableName(), dataElement.getColumnName()));
		} else {
			returnValue.add(new TableColumn(dataObject.getTableName(), dataElement.getColumnName()));
		}

		return returnValue;
	}

	static class TableColumn {

		private final String tableName;
		private final String columnName;

		public TableColumn(final String theTableName, final String theColumnName) {
			tableName = theTableName;
			columnName = theColumnName;
		}

		public String getTableName() {
			return tableName;
		}

		public String getColumnName() {
			return columnName;
		}

		@Override
		public boolean equals(final Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj);
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}
}
