package net.micropact.aea.du.page.mismatchedColumnTypes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.DataElement;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.entellitrak.platform.DatabasePlatform;
import com.google.gson.Gson;

import net.micropact.aea.core.cache.AeaCoreConfiguration;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.utility.EtkDataUtils;
import net.micropact.aea.du.utility.mismatchedColumnTypes.AllDatabaseColumnsMetadata;
import net.micropact.aea.du.utility.mismatchedColumnTypes.ElementAcceptableColumns;
import net.micropact.aea.du.utility.mismatchedColumnTypes.IDatabaseColumnMetadata;
import net.micropact.aea.du.utility.mismatchedColumnTypes.IMismatchFinderPlatformConfig;
import net.micropact.aea.du.utility.mismatchedColumnTypes.oracle.OracleMismatchFinderConfig;
import net.micropact.aea.du.utility.mismatchedColumnTypes.postgres.PostgresMismatchFinderConfig;
import net.micropact.aea.du.utility.mismatchedColumnTypes.sqlServer.SqlServerMismatchFinderConfig;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.Utility;

/**
 * This class attempts to find column type differences between the entellitrak meta-data tables and the actual database
 * columns. It not only looks at the actual data types, but also accounts for column lengths and precisions. Currently
 * this page makes no attempt to assist the user with correcting the problem, it only serves to point out where it
 * believes there is one.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class MismatchedColumnTypesController implements PageController {

	@Override
	public Response execute(final PageExecutionContext etk) throws ApplicationException {
		final TextResponse response = etk.createTextResponse();

		BreadcrumbUtility.setBreadcrumbAndTitle(response,
				BreadcrumbUtility.addLastChildFluent(
						DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
						new SimpleBreadcrumb("Mismatched Column Types",
								"page.request.do?page=du.page.mismatchedColumnTypes")));

		final IMismatchFinderPlatformConfig mismatchFinder = determineFinderConfig(etk);

		response.put("elementMismatches",
				new Gson().toJson(findMismatches(etk, mismatchFinder)));

		return response;
	}

	/**
	 * Determine the database platform finder config.
	 *
	 * @param etk
	 *            the execution context;
	 * @return the {@link IMismatchFinderPlatformConfig} object.
	 */
	private static IMismatchFinderPlatformConfig determineFinderConfig(final PageExecutionContext etk) {
		return Map.of(
				DatabasePlatform.SQL_SERVER, new SqlServerMismatchFinderConfig(),
				DatabasePlatform.ORACLE, new OracleMismatchFinderConfig(),
				DatabasePlatform.POSTGRESQL, new PostgresMismatchFinderConfig())
				.get(etk.getPlatformInfo().getDatabasePlatform());
	}

	/**
	 * Find all mismatches in the database.
	 *
	 * @param etk
	 *            entellitrak execution context
	 * @param platformConfig
	 *            the platform-specific configuration
	 * @return the mismatch data
	 */
	private static Object findMismatches(final ExecutionContext etk,
			final IMismatchFinderPlatformConfig platformConfig) {
		final AllDatabaseColumnsMetadata allTablesColumns = platformConfig.loadAllTablesColumns(etk);

		final Stream<DataElement> dataElements = EtkDataUtils.getAllDataElements(etk);

		final Stream<ElementAcceptableColumns> expectedColumns = dataElements
				.flatMap(platformConfig::getExpectedElementColumns)
				.filter(elementAcceptableColumns -> !isExcluded(etk, elementAcceptableColumns));

		return expectedColumns.map(elementColumns -> attemptColumnMatch(allTablesColumns, elementColumns))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.sorted(Comparator.comparing((final Map<String, Object> reason) -> (String) reason.get("dataObjectName"))
						.thenComparing((final Map<String, Object> reason) -> (String) reason.get("dataElementName")))
				.collect(Collectors.toList());
	}

	/**
	 * Determine whether a table/column has been excluded from the mismatch check by the aea core configuration.
	 *
	 * @param etk
	 *            entellitrak execution context
	 * @param elementAcceptableColumns
	 *            the element/column
	 * @return whether the column is excluded
	 */
	private static boolean isExcluded(final ExecutionContext etk,
			final ElementAcceptableColumns elementAcceptableColumns) {
		final String concatenatedValue = String.format("%s.%s",
				elementAcceptableColumns.getTableName(), elementAcceptableColumns.getColumnName());
		return AeaCoreConfiguration.getDuMismatchColumnExclusions(etk)
				.stream()
				.anyMatch(concatenatedValue::equals);
	}

	/**
	 * Attempt to match a specific element/column against all the metadata records in the database.
	 *
	 * @param tablesColumns
	 *            the tables/columns in the database
	 * @param elementColumns
	 *            the expected element/column combinations
	 * @return the potential mismatch
	 */
	private static Optional<Map<String, Object>> attemptColumnMatch(final AllDatabaseColumnsMetadata tablesColumns,
			final ElementAcceptableColumns elementColumns) {
		final Optional<Map<String, Object>> returnValue;

		final DataElement dataElement = elementColumns.getDataElement();
		final List<IDatabaseColumnMetadata> acceptableColumns = elementColumns.getAcceptableColumns();

		if (acceptableColumns.isEmpty()) {
			/* This case represents types which are not implemented (such as plugin) */
			returnValue = Optional.empty();
		} else {
			final String tableName = elementColumns.getTableName();
			final String columnName = elementColumns.getColumnName();

			final Optional<IDatabaseColumnMetadata> maybeDatabaseColumn = tablesColumns.getByTableColumn(tableName,
					columnName);
			if (maybeDatabaseColumn.isPresent()) {
				/* The column exists in the database */
				final IDatabaseColumnMetadata databaseColumn = maybeDatabaseColumn.get();

				/* Actually check whether the column matches one of our acceptable columns */
				final boolean isGood = acceptableColumns.stream().anyMatch(databaseColumn::equals);

				if (isGood) {
					returnValue = Optional.empty();
				} else {
					final String formattedAcceptableColumns = acceptableColumns.stream()
							.map(Object::toString)
							.collect(Collectors.joining("\n\n"));

					/* Suppress warning over newline. */
					@SuppressWarnings("squid:S3457")
					final String mismatchReason = String.format(
							"Expected column to match one of:\n\n%s\n\n\nActual column:\n\n%s",
							formattedAcceptableColumns,
							databaseColumn);

					returnValue = Optional.of(createMismatchReason(dataElement, mismatchReason));
				}
			} else {
				/* The column was not found in the database */
				returnValue = Optional.of(createMismatchReason(dataElement,
						String.format("Database Column not found: %s.%s ", tableName, columnName)));
			}
		}
		return returnValue;
	}

	/**
	 * Create a representation of a single mismatch reason.
	 *
	 * @param dataElement
	 *            the data element
	 * @param reason
	 *            the reason
	 * @return a representation of the reason
	 */
	private static Map<String, Object> createMismatchReason(final DataElement dataElement, final String reason) {
		return Utility.arrayToMap(String.class, Object.class, new Object[][] {
			{ "dataObjectName", dataElement.getDataObject().getName() },
			{ "dataElementName", dataElement.getName() },
			{ "elementType", dataElement.getDataType() },
			{ "reason", reason },
		});
	}
}
