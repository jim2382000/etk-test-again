package net.micropact.aea.utility;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.entellitrak.BaseObjectEventContext;
import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.configuration.ThemeType;
import com.entellitrak.configuration.WorkspaceService;
import com.entellitrak.legacy.workflow.WorkflowResult;
import com.entellitrak.platform.DatabasePlatform;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * This class contains static functions which will be generally useful throughout entellitrak.
 *
 * @author zmiller
 */
public final class Utility {

	/**
	 * Hide default constructor since all methods are static.
	 */
	private Utility() {
	}

	/**
	 * This function is used to give java "reader syntax" for creating a Map.
	 *
	 * @param <K>
	 *            Type of the keys
	 * @param <V>
	 *            Type of the values
	 * @param keyClass
	 *            The class which the keys of the Map should be an instance of
	 * @param valueClass
	 *            The class which the values of the Map should be an instance of
	 * @param array
	 *            An Nx2 array where the first dimension contains the keys of the Map and the 2nd dimension contains the
	 *            Values.
	 * @return The Map specified by the input array.
	 */
	public static <K, V> Map<K, V> arrayToMap(final Class<K> keyClass,
			final Class<V> valueClass,
			final Object[][] array) {
		final Map<K, V> map = new LinkedHashMap<>();
		for (final Object[] item : array) {
			if (item.length != 2) {
				throw new GeneralRuntimeException(
						String.format(
								"array parameter's 2nd dimension should be 2, instead one of its entries has length of: %s",
								item.length));
			}

			final K key;
			final V value;

			try {
				key = keyClass.cast(item[0]);
			} catch (final ClassCastException e) {
				throw new GeneralRuntimeException(
						String.format("Key does not match requested type. Expected type: %s, Actual type: %s",
								keyClass.getName(),
								item[0].getClass().getName()),
						e);
			}

			try {
				value = valueClass.cast(item[1]);
			} catch (final ClassCastException e) {
				throw new GeneralRuntimeException(
						String.format("Value does not match requested type. Expected type: %s, Actual type: %s",
								valueClass.getName(),
								item[1].getClass().getName()),
						e);
			}

			map.put(key, value);
		}
		return map;
	}

	/**
	 * This function will return true if the database being used by entellitrak is some version of Microsoft SQL Server.
	 *
	 * @param etk
	 *            entellitrak execution context
	 * @return Whether or not the database is Microsoft SQL Server
	 */
	public static boolean isSqlServer(final ExecutionContext etk) {
		return etk.getPlatformInfo().getDatabasePlatform() == DatabasePlatform.SQL_SERVER;
	}

	/**
	 * This function will return true if the database being used by entellitrak is Oracle.
	 *
	 * @param etk
	 *            entellitrak execution context
	 * @return Whether or not the database is Oracle
	 */
	public static boolean isOracle(final ExecutionContext etk) {
		return etk.getPlatformInfo().getDatabasePlatform() == DatabasePlatform.ORACLE;
	}

	/**
	 * This function will return true if the database being used by entellitrak is PostgreSQL.
	 *
	 * @param etk
	 *            entellitrak execution context
	 * @return Whether or not the database is PostgreSQL.
	 */
	public static boolean isPostgreSQL(final ExecutionContext etk) {
		return etk.getPlatformInfo().getDatabasePlatform() == DatabasePlatform.POSTGRESQL;
	}

	/**
	 * This function returns the first non-null entry in values.
	 *
	 * @param <T>
	 *            type of values.
	 * @param values
	 *            Possible null values
	 * @return The first non-null value in values. If all values are null, then null is returned.
	 */
	@SafeVarargs
	public static <T> T nvl(final T... values) {
		return Stream.of(values)
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
	}

	/**
	 * Returns true if the string is null or the empty String.
	 *
	 * @param string
	 *            The string which should be tested.
	 * @return Whether the string is null or the empty String.
	 */
	public static boolean isBlank(final String string) {
		return string == null || "".equals(string);
	}

	/**
	 * Since result.cancelTransaction should always be accompanied by result.addMessage, this function combines the two.
	 *
	 * @param etk
	 *            entellitrak execution context.
	 * @param message
	 *            message to be displayed. HTML is NOT escaped.
	 */
	public static void cancelTransactionMessage(final BaseObjectEventContext etk, final String message) {
		final WorkflowResult result = etk.getResult();
		result.cancelTransaction();
		result.addMessage(message);
	}

	/**
	 * Helper method to indicate whether or not the site is configured to use the hydrogen UI. Value is stored in the
	 * etk cache for performance reasons.
	 *
	 * @param etk
	 *            The execution context.
	 * @return true if hydrogen UI.
	 */
	public static boolean isHydrogenUI(final ExecutionContext etk) {
		return ThemeType.HYDROGEN == etk.getConfigurationService().getApplicationInformation().getTheme();
	}

	/**
	 * Helper method to indicate whether or not the site is configured to use the dynamic UI. Value is stored in the etk
	 * cache for performance reasons.
	 *
	 * @param etk
	 *            The execution context.
	 * @return true if helium ui.
	 */
	public static boolean isHeliumUI(final ExecutionContext etk) {
		return ThemeType.HELIUM == etk.getConfigurationService().getApplicationInformation().getTheme();
	}

	/**
	 * Returns the web-pub path depending on the system's current setting.
	 *
	 * @param etk
	 *            The Execution context.
	 * @return The web-pub folder path.
	 */
	public static String getWebPubPath(final ExecutionContext etk) {
		if (isHydrogenUI(etk)) {
			return "themes/default/web-pub";
		} else if (isHeliumUI(etk)) {
			return "themes/helium/web-pub";
		} else {
			return "web-pub";
		}
	}

	/**
	 * Get the tracking configuration id of the configuration which will be deployed next time apply changes is done.
	 * This is the configuration which is visible in the Configuration tab.
	 *
	 * @param etk
	 *            entellitrak execution context
	 * @return The next tracking config id
	 * @throws IncorrectResultSizeDataAccessException
	 *             If there was an underlying {@link IncorrectResultSizeDataAccessException}
	 */
	public static long getTrackingConfigIdNext(final ExecutionContext etk)
			throws IncorrectResultSizeDataAccessException {
		return etk.createSQL(
				"SELECT tracking_config_id FROM etk_tracking_config WHERE config_version = (SELECT MAX(config_version) FROM etk_tracking_config)")
				.fetchLong();

	}

	// APINOW: Should there be anything needing to access the current tracking
	// configuration anymore?
	/**
	 * Get the tracking configuration id of the configuration which is currently deployed. This is the configuration
	 * that the users are currently interacting with, not the configuration which the AE is interacting with under the
	 * configuration tab.
	 *
	 * @param etk
	 *            entellitrak execution context
	 * @return The currently deployed tracking config id
	 * @throws IncorrectResultSizeDataAccessException
	 *             If there was an underlying {@link IncorrectResultSizeDataAccessException}
	 */
	public static long getTrackingConfigIdCurrent(final ExecutionContext etk)
			throws IncorrectResultSizeDataAccessException {
		return etk.createSQL("SELECT MAX (tracking_config_id) FROM etk_tracking_config_archive")
				.fetchLong();

	}

	/**
	 * Get the workspace id of the system repository.
	 *
	 * @param etk
	 *            entellitrak execution context
	 * @return The id of the system repository workspace
	 * @throws IncorrectResultSizeDataAccessException
	 *             If there was an underlying {@link IncorrectResultSizeDataAccessException}
	 */
	public static long getSystemRepositoryWorkspaceId(final ExecutionContext etk)
			throws IncorrectResultSizeDataAccessException {
		// APINOW: This method can be deleted. It is only used by package-mover
		// which is no longer supported.
		return etk
				.createSQL("select workspace_id from etk_workspace where workspace_name = 'system' and user_id is null")
				.fetchLong();
	}

	/**
	 * Add an in clause to an SQL string builder that supports more than 1000 records.
	 *
	 * <p>
	 * inObjectList is split into a bracketed set of multiple groups: (columnName in (:inObjectList0-500) or columnName
	 * in (:inObjectList501-1000) or columnName in (:inObjectList1001-1500))
	 * </p>
	 * <p>
	 * The resulting SQL is inserted directly into the provided queryBuilder.
	 * </p>
	 *
	 * @param columnName
	 *            The column name to compare in the in clause.
	 * @param queryBuilder
	 *            The query to insert the in clause into.
	 * @param outputParamMap
	 *            The parameter map that will be passed into the query.
	 * @param inObjectList
	 *            The list of objects to insert into the in(:objects) clause.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void addLargeInClause(final String columnName, final StringBuilder queryBuilder,
			final Map outputParamMap, final List inObjectList) {
		final int groupSize = 1000;
		final String noPeriodColumnName = columnName.replace("\\.", "_");

		queryBuilder.append(" (");

		if (inObjectList == null || inObjectList.isEmpty()) {
			queryBuilder.append(columnName);
			queryBuilder.append(" in (null)");
		} else if (inObjectList.size() == 1) {
			queryBuilder.append(columnName);
			queryBuilder.append(" = :");
			queryBuilder.append(noPeriodColumnName);
			outputParamMap.put(noPeriodColumnName, inObjectList.get(0));
		} else {
			int paramGroup = 0;

			for (int i = 0; i < inObjectList.size(); i = i + groupSize) {
				if (i + groupSize < inObjectList.size()) {
					queryBuilder.append(columnName);
					queryBuilder.append(" in (:" + noPeriodColumnName + paramGroup + ") OR ");
					outputParamMap.put(noPeriodColumnName + paramGroup, inObjectList.subList(i, i + groupSize));
				} else {
					queryBuilder.append(columnName);
					queryBuilder.append(" in (:" + noPeriodColumnName + paramGroup + ")");
					outputParamMap.put(noPeriodColumnName + paramGroup, inObjectList.subList(i, inObjectList.size()));
				}
				paramGroup++;
			}
		}

		queryBuilder.append(") ");
	}

	/**
	 * Determines the workspace id that the desired user is using for execution.
	 *
	 * @param etk
	 *            entellitrak execution context.
	 * @param userId
	 *            The id of the desired user.
	 * @return The Workspace Id of the workspace that the specified user is using for execution.
	 * @throws IncorrectResultSizeDataAccessException
	 *             If there was an underlying {@link IncorrectResultSizeDataAccessException}.
	 */
	public static long getWorkspaceId(final ExecutionContext etk, final Long userId)
			throws IncorrectResultSizeDataAccessException {
		final WorkspaceService workspaceService = etk.getWorkspaceService();

		final String workspaceName = etk.createSQL("SELECT current_workspace FROM etk_user u JOIN etk_development_preferences developmentPreferences ON developmentPreferences.development_preferences_id = u.development_preferences_id WHERE u.user_id = :userId")
				.setParameter("userId", userId)
				.returnEmptyResultSetAs(workspaceService.getSystemWorkspace().getName())
				.fetchString();

		return etk.createSQL("SELECT workspace_id FROM etk_workspace WHERE workspace_name = :workspaceName")
				.setParameter("workspaceName", workspaceName)
				.fetchLong();
	}
}
