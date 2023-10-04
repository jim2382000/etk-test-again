package net.micropact.aea.core.service;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.entellitrak.DataAccessException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;

import net.entellitrak.aea.core.service.IDeploymentResult;
import net.entellitrak.aea.core.service.IDeploymentService;
import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.exceptionTools.ExceptionUtility;
import net.micropact.aea.core.utility.StringUtils;
import net.micropact.aea.utility.Utility;

/*
 * This class pretty much threads the mutable DeploymentResult through all of the sub-methods.
 * This could all be refactored so that each of the private methods we call to do something implements an interface
 * and returns its result but there's not much point in doing that at this point time.
 */

/**
 * Implementation of {@link IDeploymentService} public API.
 *
 * @author Zachary.Miller
 */
public class DeploymentService implements IDeploymentService {

	private final ExecutionContext etk;

	/**
	 * Simple constructor.
	 *
	 * @param executionContext entellitrak execution context
	 */
	public DeploymentService(final ExecutionContext executionContext) {
		etk = executionContext;
	}

	@Override
	public IDeploymentResult runComponentSetup() {
		final DeploymentResult deploymentResult = new DeploymentResult();

		generateDatabaseArtifacts(etk, deploymentResult);
		configureAeaCoreConfigurationRdo(etk, deploymentResult);

		/* Clear caches to ensure new values are picked up. */
		clearCache(etk, deploymentResult);

		return deploymentResult;
	}

	/**
	 * Clear the cache.
	 *
	 * @param etk entellitrak execution context
	 * @param deploymentResult deployment result
	 */
	private static void clearCache(final ExecutionContext etk, final DeploymentResult deploymentResult) {
		etk.getSerializableCache().clearCache();
		etk.getDataCacheService().clearDataCaches();
		etk.getCache().clearCache();

		deploymentResult.addMessage("Cache Cleared.");
	}

	/**
	 * Generate database objects (functions/procedures/views).
	 *
	 * @param etk entellitrak execution context
	 * @param deploymentResult deployment result
	 */
	private static void generateDatabaseArtifacts(final ExecutionContext etk, final DeploymentResult deploymentResult) {
		createScriptPackageView(etk, deploymentResult);
		createScriptPackageViewSysOnly(etk, deploymentResult);
		dropAeaUpdateFileReferenceId(etk, deploymentResult);
		dropAddJbpmLogEntry(etk, deploymentResult);
		dropAddJbpm(etk, deploymentResult);
	}

	/**
	 * Creates AEA_SCRIPT_PKG_VIEW View for both Oracle and SQLServer. This view simplifies retrieval of the fully
	 * qualified package associated with a script object.
	 *
	 * @param etk entellitrak execution context
	 * @param deploymentResult deployment result
	 */
	private static void createScriptPackageView(final ExecutionContext etk, final DeploymentResult deploymentResult) {
		try {
			if (Utility.isSqlServer(etk)) {
				try {
					etk.createSQL("DROP VIEW AEA_SCRIPT_PKG_VIEW").execute();
					deploymentResult.addMessage("Dropped existing AEA_SCRIPT_PKG_VIEW.");
				} catch (final Exception e) {
					final String message = "Error dropping existing AEA_SCRIPT_PKG_VIEW, ignore if first time import.";
					etk.getLogger().debug(message, e);
					deploymentResult
					.addMessage(message);
				}

				etk.createSQL(
						"CREATE VIEW AEA_SCRIPT_PKG_VIEW\n (SCRIPT_ID, SCRIPT_NAME, SCRIPT_LANGUAGE_TYPE, SCRIPT_BUSINESS_KEY, WORKSPACE_ID,\n PACKAGE_PATH, PACKAGE_NODE_ID, PACKAGE_TYPE, FULLY_QUALIFIED_SCRIPT_NAME, SCRIPT_HANDLER_TYPE) AS\n\n WITH etk_packages (package_node_id, workspace_id, path, package_type) AS\n   (SELECT rootPackage.package_node_id,\n           rootPackage.workspace_id,\n           CAST(rootPackage.name as VARCHAR(4000)) path,\n           rootPackage.package_type\n     FROM etk_package_node rootPackage\n     WHERE rootPackage.parent_node_id IS NULL\n\n     UNION ALL\n\n     SELECT childPackage.package_node_id,\n           childPackage.workspace_id,\n           cast(parentPackage.path + \'.\' + childPackage.name as varchar(4000)) as path,   \n           childPackage.package_type \n     FROM etk_package_node childPackage\n     JOIN etk_packages parentPackage ON parentPackage.package_node_id = childPackage.parent_node_id\n   )\n   SELECT scriptObject.script_id script_id,\n          scriptObject.name script_name,\n          scriptObject.language_type script_language_type,\n          scriptObject.business_key script_business_key,\n          scriptObject.workspace_id workspace_id,\n          packages.path package_path,\n          packages.package_node_id package_node_id,\n          packages.package_type package_type,\n          CASE \n            WHEN scriptObject.package_node_id IS NULL\n            THEN scriptObject.name \n            ELSE\n            packages.path + \'.\' + scriptObject.name \n          END fully_qualified_script_name,\n          scriptObject.handler_type script_handler_type\n   FROM etk_script_object scriptObject\n   LEFT JOIN etk_packages packages ON scriptObject.package_node_id = packages.package_node_id")
				.execute();
			} else if (Utility.isPostgreSQL(etk)) {
				etk.createSQL(
						"CREATE OR REPLACE VIEW AEA_SCRIPT_PKG_VIEW  \n   (SCRIPT_ID, SCRIPT_NAME, SCRIPT_LANGUAGE_TYPE, SCRIPT_BUSINESS_KEY, WORKSPACE_ID,  \n    PACKAGE_PATH, PACKAGE_NODE_ID, PACKAGE_TYPE, FULLY_QUALIFIED_SCRIPT_NAME, SCRIPT_HANDLER_TYPE) AS \n   WITH RECURSIVE etk_package (package_node_id, workspace_id, path, package_type) AS \n            (SELECT rootPackage.package_node_id, \n                    rootPackage.workspace_id, \n                    CAST(rootPackage.name AS VARCHAR(4000)) path, \n                    rootPackage.package_type \n             FROM etk_package_node rootPackage\n             WHERE rootPackage.parent_node_id IS NULL \n             \n             UNION ALL \n             \n             SELECT childPackage.package_node_id, \n                    childPackage.workspace_id,\n                    CAST(parentPackage.path || '.' || childPackage.name as varchar(4000)) path, \n                    childPackage.package_type \n             FROM etk_package_node childPackage\n                  JOIN etk_package parentPackage ON parentPackage.package_node_id = childPackage.parent_node_id)\n\n   SELECT scriptObject.script_id script_id, \n          scriptObject.name script_name, \n          scriptObject.language_type script_lanugage_type, \n          scriptObject.business_key script_business_key, \n          scriptObject.workspace_id workspace_id, \n          packages.path package_path, \n          packages.package_node_id package_node_id, \n          packages.package_type package_type, \n          CASE \n            WHEN scriptObject.package_node_id IS NULL \n            THEN scriptObject.name \n            ELSE packages.path \n                || '.' \n                || scriptObject.name \n          END fully_qualified_script_name, \n          scriptObject.handler_type script_handler_type \n   FROM etk_package packages  \n  LEFT JOIN  etk_script_object scriptObject ON scriptObject.package_node_id = packages.package_node_id ")
				.execute();
			} else {
				etk.createSQL(
						"CREATE OR REPLACE VIEW AEA_SCRIPT_PKG_VIEW  \n   (SCRIPT_ID, SCRIPT_NAME, SCRIPT_LANGUAGE_TYPE, SCRIPT_BUSINESS_KEY, WORKSPACE_ID,  \n    PACKAGE_PATH, PACKAGE_NODE_ID, PACKAGE_TYPE, FULLY_QUALIFIED_SCRIPT_NAME, SCRIPT_HANDLER_TYPE) AS \n   WITH etk_package (package_node_id, workspace_id, path, package_type) AS \n            (SELECT rootPackage.package_node_id, \n                    rootPackage.workspace_id, \n                    CAST(rootPackage.name AS VARCHAR(4000)) path, \n                    rootPackage.package_type \n             FROM etk_package_node rootPackage\n             WHERE rootPackage.parent_node_id IS NULL \n             \n             UNION ALL \n             \n             SELECT childPackage.package_node_id, \n                    childPackage.workspace_id,\n                    CAST(parentPackage.path || '.' || childPackage.name as varchar(4000)) path, \n                    childPackage.package_type \n             FROM etk_package_node childPackage\n                  JOIN etk_package parentPackage ON parentPackage.package_node_id = childPackage.parent_node_id)\n\n   SELECT scriptObject.script_id script_id, \n          scriptObject.name script_name, \n          scriptObject.language_type script_lanugage_type, \n          scriptObject.business_key script_business_key, \n          scriptObject.workspace_id workspace_id, \n          packages.path package_path, \n          packages.package_node_id package_node_id, \n          packages.package_type package_type, \n          CASE \n            WHEN scriptObject.package_node_id IS NULL \n            THEN scriptObject.name \n            ELSE packages.path \n                || '.' \n                || scriptObject.name \n          END fully_qualified_script_name, \n          scriptObject.handler_type script_handler_type \n   FROM etk_script_object scriptObject \n   LEFT JOIN etk_package packages ON scriptObject.package_node_id = packages.package_node_id ")
				.execute();
			}

			deploymentResult.addMessage("Successfully created view AEA_SCRIPT_PKG_VIEW");

		} catch (final Exception e) {
			deploymentResult.addMessage("Error creating AEA_SCRIPT_PKG_VIEW.");
			deploymentResult.addMessage(ExceptionUtility.getFullStackTrace(e));
		}
	}

	/**
	 * Creates AEA_SCRIPT_PKG_VIEW View for both Oracle and SQLServer. This view simplifies retrieval of the fully
	 * qualified package associated with a script object.
	 *
	 * Filtered by SYSTEM workspace only. Useful because you don't need to filter this one by workspace ID and tracking
	 * config ID.
	 *
	 * @param etk entellitrak execution context
	 * @param deploymentResult deployment result
	 */
	private static void createScriptPackageViewSysOnly(final ExecutionContext etk,
			final DeploymentResult deploymentResult) {
		try {
			if (Utility.isPostgreSQL(etk)) {
				etk.createSQL("DROP VIEW IF EXISTS AEA_SCRIPT_PKG_VIEW_SYS_ONLY").execute();
			} else {
				etk.createSQL("DROP VIEW AEA_SCRIPT_PKG_VIEW_SYS_ONLY").execute();
			}
			deploymentResult.addMessage("Dropped existing AEA_SCRIPT_PKG_VIEW_SYS_ONLY.");
		} catch (final Exception e) {
			final String message = "Error dropping existing AEA_SCRIPT_PKG_VIEW_SYS_ONLY, ignore if first time import.";
			etk.getLogger().debug(message, e);
			deploymentResult.addMessage(message);
		}

		etk.createSQL("CREATE VIEW AEA_SCRIPT_PKG_VIEW_SYS_ONLY \n  (SCRIPT_ID, SCRIPT_NAME, SCRIPT_LANGUAGE_TYPE, SCRIPT_BUSINESS_KEY,  \n   WORKSPACE_ID, PACKAGE_PATH, PACKAGE_NODE_ID, PACKAGE_TYPE, \n   FULLY_QUALIFIED_SCRIPT_NAME, SCRIPT_HANDLER_TYPE) AS\n  SELECT script_id, script_name, script_language_type, script_business_key,\n         workspace_id, package_path, package_node_id, package_type, \n         fully_qualified_script_name, script_handler_type \n  FROM aea_script_pkg_view \n  WHERE workspace_id = (SELECT workspace_id \n                        FROM etk_workspace  \n                        WHERE workspace_name = 'system' and user_id is null)")
		.execute();

		deploymentResult.addMessage("Successfully created view AEA_SCRIPT_PKG_VIEW_SYS_ONLY");
	}

	/**
	 * Configure values for the T_AEA_CORE_CONFIGURATION table.
	 *
	 * @param etk
	 *            entellitrak execution context
	 * @param deploymentResult
	 *            deployment result
	 */
	private static void configureAeaCoreConfigurationRdo(final ExecutionContext etk,
			final DeploymentResult deploymentResult) {

		deploymentResult.addMessage(
				Arrays.stream(AeaCoreConfigurationRecord.values())
				.map(recordToInstall -> {
					final String message;

					final String code = recordToInstall.getCode();
					final String defaultValue = recordToInstall.getDefaultValue();
					final String description = recordToInstall.getDescription();

					Long matchingId;
					try {
						matchingId = etk.createSQL("SELECT ID FROM t_aea_core_configuration WHERE c_code = :c_code")
								.setParameter("c_code", code)
								.returnEmptyResultSetAs(null)
								.fetchLong();
					} catch (final IncorrectResultSizeDataAccessException e) {
						throw new GeneralRuntimeException(
								String.format("Got multiple t_aea_core_configuration records with code %s",
										code),
								e);
					}

					if (matchingId == null) {
						etk.createSQL(Utility.isSqlServer(etk) || Utility.isPostgreSQL(etk)
								? "INSERT INTO t_aea_core_configuration(c_code, c_description, c_value) VALUES(:c_code, :description, :value)"
										: "INSERT INTO t_aea_core_configuration(id, c_code, c_description, c_value) VALUES(OBJECT_ID.NEXTVAL, :c_code, :description, :value)")
						.setParameter("c_code", code)
						.setParameter("description", description)
						.setParameter("value", defaultValue)
						.execute();
						message =String.format("Inserted T_AEA_CORE_CONFIGURATION %s = %s", code,
								defaultValue);
					} else {
						etk.createSQL("UPDATE t_aea_core_configuration SET c_description = :description WHERE id = :id")
						.setParameter("description", description)
						.setParameter("id", matchingId)
						.execute();
						message = String.format("Updated Description of T_AEA_CORE_CONFIGURATION %s", code);
					}

					return message;
				}).collect(Collectors.joining("\n")));
	}

	/**
	 * Drop a stored procedure.
	 *
	 * @param etk
	 *            entellitrak execution context
	 * @param deploymentResult
	 *            deployment result
	 * @param procedureName
	 *            The procedure name
	 */
	private static void dropProcedure(final ExecutionContext etk, final DeploymentResult deploymentResult,
			final String procedureName) {
		try {
			if (Utility.isPostgreSQL(etk)) {
				etk.createSQL(String.format("DROP PROCEDURE IF EXISTS %s", procedureName))
				.execute();
			} else {
				etk.createSQL(String.format("DROP PROCEDURE %s", procedureName))
				.execute();
			}

			deploymentResult.addMessage(String.format("Dropped Procedure %s", procedureName));
		} catch (final DataAccessException e) {
			etk.getLogger().debug(String.format("Could not drop procedure %s. This is expected if the procedure does not exist.", procedureName), e);
			deploymentResult.addMessage(String.format("Could not drop procedure %s", procedureName));
		}
	}

	/**
	 * Drop the AEA_UPDATE_FILE_REFERENCE_ID procedure.
	 *
	 * @param etk entellitrak execution context
	 * @param deploymentResult deployment result
	 */
	private static void dropAeaUpdateFileReferenceId(final ExecutionContext etk, final DeploymentResult deploymentResult) {
		dropProcedure(etk, deploymentResult, "AEA_UPDATE_FILE_REFERENCE_ID");
	}

	/**
	 * Drop the AEA_CORE_ADD_JBPM_LOG_ENTRY procedure.
	 *
	 * @param etk entellitrak execution context
	 * @param deploymentResult deployment result
	 */
	private static void dropAddJbpmLogEntry(final ExecutionContext etk, final DeploymentResult deploymentResult) {
		dropProcedure(etk, deploymentResult, "AEA_CORE_ADD_JBPM_LOG_ENTRY");
	}

	/**
	 * Ensure the AEA_CORE_ADD_JBPM procedure exists.
	 *
	 * @param etk entellitrak execution context
	 * @param deploymentResult deployment result
	 */
	private static void dropAddJbpm(final ExecutionContext etk, final DeploymentResult deploymentResult) {
		dropProcedure(etk, deploymentResult, "AEA_CORE_ADD_JBPM");
	}


	private enum AeaCoreConfigurationRecord {
		A_1("distributedTransaction.accountName", "distributedtransaction",
				"The user account name of the Distributed Transaction System User. This user must be created under Administration -> Authentication -> Client Authentication."),
		A_2("distributedTransaction.batchSize", "5",
				"Number of records to process in a batch. Batches are created during the initial createTransaction process." ),
		A_3("distributedTransaction.oauth2ClientId", "",
				"The Client ID generated when the Distributed Transaction System User was created in Administration -> Authentication -> Client Authentication." ),
		A_4( "distributedTransaction.oauth2ClientSecret", "",
				"The Client Secret generated when the Distributed Transaction System User was created in Administration -> Authentication -> Client Authentication." ),
		A_5( "distributedTransaction.url", "", "External URL to send distributed transactions to." ),
		A_6( "distributedTransaction.maxRetries", "5",
				"The maximum number of attempts that the system will attempt to reprocess a transaction." ),
		A_7( "distributedTransaction.minThreadCount", "3",
				"The minimum number of worker threads the distributed transaction service will start." ),
		A_8( "distributedTransaction.maxThreadCount", "6",
				"The maximum number of worker threads the distributed transaction service will start." ),
		A_9( "distributedTransaction.pollingInterval", "1000",
				"How often the distributed transaction scheduler will check to see if distributed transction threads have completed processing, in ms." ),
		A_10( "distributedTransaction.processingTimeout", "1800",
				"How long a worker thread will be allowed to process before being marked as error and the transaction is forced to roll back, in seconds." ),
		A_11( "ls.enableAccessValidation", "true",
				"If true (default), Live Search will perform access validation security checks. Set to 'false' to disable." ),
		A_12( "cf.enableAccessValidation", "true",
				"If true (default), Calculated Fields (Banners) will perform access validation security checks. Set to 'false' to disable." ),
		A_13( "aea.core.cacheStaticContent", "1",
				"Deprecated: Superseded by core's public caching system preferences.\n\nIf set to 1, aearchitecture pages such as DefaultJavascriptController and DefaultCssController will instruct browsers to cache the content. This leads to increased performance on production. \n\nIf set to 0, these pages will not cache which means that developers will not need to clear their cache constantly" ),
		A_14( "dt.enhancedInboxEnabled", "true",
				"Valid values are 'true' and 'false'.\n\nIf false the page display option record for this feature is removed when the Dashboard Tools scheduler job is run.  If this feature won't be used it should be set to false to improve performance on the Dashboard." ),
		A_15( "dt.systemWideBroadcastEnabled", "true",
				"Valid values are 'true' and 'false'.\n\nIf false the page display option record for this feature is removed when the Dashboard Tools scheduler job is run.  If this feature won't be used it should be set to false to improve performance on the Dashboard." ),
		A_16( "dt.calendarEnabled", "true",
				"Valid values are 'true' and 'false'.\n\nIf false the page display option record for this feature is removed when the Dashboard Tools scheduler job is run.  If this feature won't be used (at least on the Dashboard) it should be set to false to improve performance on the Dashboard." ),
		A_17( "enhancedInbox.customJavaScript", "",
				"The business key of the page which returns any custom JavaScript needed to handle any custom html elements added to an inbox via an InboxDecorator." ),
		A_18( "enhancedInbox.customCSS", "",
				"The business key of the page which returns any custom CSS to decorate the Enhanced Inbox." ),
		A_19( "enhancedInbox.customCssClasses", "",
				"Space separated list of classes to add to the class attribute of the table displaying the inbox data (table ID 'inbox_content_table' when not showing all inboxes).  This can be used in conjunction with the 'enhancedInbox.customCSS' configuration in order to customize the look of the Enhanced Inbox datatable." ),
		A_20( "enhancedInbox.displayLength", "20", "The number of records to show in an inbox per page." ),
		A_21( "enhancedInbox.displayTitle", "Enhanced Inbox",
				"The title displayed in the header of the inbox accordion." ),
		A_22( "enhancedInbox.showCount", "true",
				"Valid values are 'true' or 'false'.\n\nWhether to calculate and show the count of rows for each inbox in the Inbox Selection drop-down list.  Not using the count can improve performance if the count display isn't required by the project." ),
		A_23( "enhancedInbox.showAllInboxes", "false",
				"Valid values are 'true' or 'false'.\n\nWhether to show all inboxes on the dashboard at once.  This will disregard any groups the inboxes may belong to.  Each inbox will be in its own accordion with the inbox name as the title." ),
		A_24( "enhancedInbox.usesGroups", "true",
				"Valid values are 'true' or 'false'.\n\nIf set to 'false' group selection will be hidden and all inboxes which would have been shown separately per group will all be in a single dropdown list." ),
		A_25( "advancedRecursiveDebug", "false", "Enabled advanced, recursive debugging output for aeaLog" ),
		A_26( "swb.label", "System Wide Broadcasts",
				"Label of the fieldset which contains the current broadcasts." ),
		A_27( "swb.height", "125",
				"Height of the iframe displaying the broadcasts.  This can be an integer value in pixels or 'dynamic'.  Setting to dynamic will adjust the height to be the height of the first page of broadcasts (if paginated) or the height of all the broadcasts (if not paginated).  Dynamic is best used when not paginating and you just want all broadcasts visible without scrolling." ),
		A_28( "swb.paginate", "true",
				"Valid values are 'true' or 'false'.\n\nIf set to 'false' all broadcasts will be displayed at once." ),
		A_29( "swb.showDate", "true",
				"Valid values are 'true' or 'false'.\n\nIf set to 'true' all broadcasts will have the date they started appearing next to the title." ),
		A_30( "swb.broadcastsPerPage", "1",
				"An integer representing how many broadcasts should be shown per page (if paginating)." ),
		A_31( "swb.noBroadcastsMessage", "No System Wide Broadcasts at this time.",
				"If configured so that the System Wide Broadcast fieldset is shown even when there are no broadcasts, this value will be shown in the fieldset whenever there are no broadcasts." ),
		A_32( "swb.showFieldsetWhenEmpty", "true",
				"Valid values are 'true' or 'false'.\n\nIf set to 'false', nothing will be shown on the home page when there are no currently active broadcasts to display." ),
		A_33( "calendar.displayArchivedEvents", "true",
				"Valid values are 'true' or 'false'.\n\nIf false the calendar will not show any events which have been moved to the archive.  You may want this option if you just want to hold on to the data but not slow down the calendar by showing archived events, which could be a large number." ),
		A_34( "calendar.monthsNotArchived", "1",
				"Number of months to keep calendar events in the main table.  Anything older will be moved to an archive table.  This value works in conjunction with the number configured for 'calendar.monthsNotDeleted' in that once in the archive table, any event older than the number of months in 'calendar.monthsNotDeleted' will be removed from the archive table." ),
		A_35( "calendar.monthsNotDeleted", "3",
				"Once an event occurred X months ago it will be deleted from the archive table.  This value works in conjunction with the number configured for 'calendar.monthsNotArchived'.  If this value is smaller than 'calendar.monthsNotArchived' then nothing will ever get deleted." ),
		A_36( "calendar.dashboardWidth", "max",
				"Width of the calendar on the dashboard.  This can be an integer value in pixels or 'max'.  Setting to max will adjust the width of the calendar to fit the width of the dashboard." ),
		A_37("du.codeSearch.objectNameExclusionRegexes", null, "A newline-separated list of regular expressions. When code search runs, it will compare the object name (like full script object path, or database view name) against each regex. If any regex matches the name, the record will not be returned.\nUseful for excluding product kit's auto-generated RDO Export files.\nNote: You may separate entries by more than one blank line."),
		A_38( "du.mismatchedColumnTypeExclusions",
				StringUtils.join(Arrays.asList(
						""),
						"\n"),
				"A newline-separated list of TABLE.COLUMN to exclude from detection by the Mismatched Column Types Developer Utility." ),
		A_39( "eu.daysUntilDeleteEmailsFromQueue", "7",
				"Email Utility - This is the Number of days that an item needs to be in the \"EU Email Queue\" list (by \"Created Time\") before it will be deleted from the system.\n\nIf you leave it blank, items will never be deleted." ),
		A_40( "eu.enableEmail", "1",
				"Email Utility - If this value is set to 1, then emails are enabled and will be sent to the email server normally. If this value is set to 0, then emails will not be sent to the email server. \n\nEmails in EU Email Queue will still change their status to sent, however they will not be sent to the Email Server." ),
		A_41( "eu.minutesUntilAbortResendingErrors", "20",
				"Email Utility - The Email Utility will try to resend emails which have encountered an error automatically, however this is a not good if the problem emails are never corrected. This value puts a limit on how long ago an email must have been created before the Email Queue will stop trying to send it.\n\nIf this value is blank, the system will never give up trying to resend emails." ),
		A_42( "dbutils.rdoExport.rdoExportMaxLines", "1000",
				"DEPRECATED. Defines the ideal maximum total size (in number of lines) of the database_inserts_X.sql inside of the rdo_export_XXX.zip. If the header / footer / errors exceed the limit, 1 statement will be included per file. A value of 0 will print all statements in a single file reguardless of size. A size of 1 is recommended for debugging." ),
		A_43( "aea.core.serviceAccount", "administrator",
				"Username for the service account that should be used in scheduler jobs."),
		A_44("ls.maxSearchResults", "20", "The maximum number of search results live search windows will return. Must be a positive integer between 1 and 200, otherwise a default of 20 will be used."),
		A_45("writeDebugToLog", "true", "Enables advanced AEA Debug Logging"),
		A_46("ls.charactersNeededForSearch", "3", "Number of characters a user must enter before live search performs a search. Must be a positive integer between 1 and 10, otherwise the system will default to a value of 3."),
		A_47("ls.debugMode", "false", "Valid values are \"true\", \"t\" and \"1\". All others evaluate to false."),
		;

		private String code;
		private String defaultValue;
		private String description;

		private AeaCoreConfigurationRecord(final String theCode, final String theDefaultValue, final String theDescription) {
			code = theCode;
			defaultValue = theDefaultValue;
			description = theDescription;
		}

		public String getCode() {
			return code;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public String getDescription() {
			return description;
		}
	}
}