package net.micropact.aea.core.cache;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.entellitrak.ExecutionContext;
import com.entellitrak.UserService;
import com.entellitrak.user.User;

import net.entellitrak.aea.core.cache.CacheManager;
import net.micropact.aea.core.enums.AeaCoreConfigurationItem;

/**
 * This class should be used to access values of {@link AeaCoreConfigurationItem}s.
 * This is because this class is strongly typed and will Cache the values.
 *
 * @author zmiller
 */
public final class AeaCoreConfiguration{

	/**
	 * This is a Utility class and does not need a constructor.
	 */
	private AeaCoreConfiguration(){}

	/**
	 * This method will get a Cached Map of {@link AeaCoreConfigurationItem} codes to values.
	 * The values will already have been deserialized to the correct type.
	 *
	 * @param etk entellitrak execution context
	 * @return Map
	 */
	private static Map<String, Object> getMap(final ExecutionContext etk) {
		return CacheManager.loadSerializable(etk, new AeaCoreConfigurationCacheable(etk));
	}

	/**
	 * This gets the cached, typed value for a particular {@link AeaCoreConfigurationItem}.
	 *
	 * @param etk entellitrak execution context
	 * @param configurationItem {@link AeaCoreConfigurationItem} to get the value for
	 * @return The value of the {@link AeaCoreConfigurationItem}
	 */
	private static Object getCacheValue(final ExecutionContext etk, final AeaCoreConfigurationItem configurationItem) {
		return getMap(etk).get(configurationItem.getCode());
	}

	/**
	 * This method returns the number of days until emails should be deleted from the Email Queue.
	 * Returns null if emails should never be deleted.
	 *
	 * @param etk entellitrak execution context
	 * @return number of days until emails should be deleted from the Email Queue
	 */
	public static Long getEuDaysUntilDeleteEmailsFromQueue(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.EU_DAYS_UNTIL_DELETE_EMAILS_FROM_QUEUE);
	}

	/**
	 * Returns whether sending of Email Utility emails is enabled.
	 *
	 * @param etk entellitrak execution context
	 * @return whether sending of Email Utility emails is enabled.
	 */
	public static boolean isEuEmailEnabled(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.EU_ENABLE_EMAIL);
	}

	/**
	 * Returns the number of minutes until the Email Queue will give up resending failed emails.
	 *
	 * @param etk entellitrak execution context
	 * @return the number of minutes until the Email Queue will give up resending failed emails.
	 */
	public static Long getEuMinutesUntilAbortResendingErrors(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.EU_MINUTES_UNTIL_ABORT_RESENDING_ERRORS);
	}

	/**
	 * Returns whether or not Enhanced Inbox is enabled.
	 *
	 * @param etk entellitrak execution context
	 * @return whether or not Enhanced Inbox is enabled.
	 */
	public static boolean getDashboardToolsEnhancedInboxEnabled(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.DASHBOARD_TOOLS_ENHANCED_INBOX_ENABLED);
	}

	public static long getDbUtilsRdoExportMaxLines(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.DBUTILS_RDOEXPORT_EXPORT_MAX_LINES);
	}


	/**
	 * Returns whether the enhanced inbox should group the inboxes into smaller groups.
	 *
	 * @param etk entellitrak execution context
	 * @return whether the inboxes should be grouped
	 */
	public static boolean getEnhancedInboxUsesGroups(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.ENHANCED_INBOX_USES_GROUPS);
	}

	/**
	 * Returns whether the enhanced inbox should show the total number of items in an inbox.
	 *
	 * @param etk entellitrak execution context
	 * @return whether the enhanced inbox should show the total number of items in an inbox
	 */
	public static boolean getEnhancedInboxShowCount(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.ENHANCED_INBOX_SHOW_COUNT);
	}

	/**
	 * Returns whether the enhanced inbox should show the total number of items in an inbox.
	 *
	 * @param etk entellitrak execution context
	 * @return whether all the inboxes should be shown on the dashboard at once
	 */
	public static boolean getEnhancedInboxShowAllInboxes(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.ENHANCED_INBOX_SHOW_ALL_INBOXES);
	}

	/**
	 * Returns the title which should be used for the enhanced inbox.
	 *
	 * @param etk entellitrak execution context
	 * @return the title which should be used for the enhanced inbox
	 */
	public static String getEnhancedInboxDisplayTitle(final ExecutionContext etk) {
		return (String) getCacheValue(etk, AeaCoreConfigurationItem.ENHANCED_INBOX_DISPLAY_TITLE);
	}

	/**
	 * Returns the number of rows which should appear on a single page of an enhanced inbox grid.
	 *
	 * @param etk entellitrak execution context
	 * @return the number of rows which should appear on a single grid
	 */
	public static long getEnhancedInboxDisplayLength(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.ENHANCED_INBOX_DISPLAY_LENGTH);
	}

	/**
	 * Returns the business key of the custom javascript page which should be used for an inbox decorator.
	 *
	 * @param etk entellitrak execution context
	 * @return the business key of the custom javascript page which should be used for an inbox decorator.
	 */
	public static String getEnhancedInboxCustomJavascript(final ExecutionContext etk) {
		return (String) getCacheValue(etk, AeaCoreConfigurationItem.ENHANCED_INBOX_CUSTOM_JAVASCRIPT);
	}

	/**
	 * Returns the business key of the custom CSS page which should be used for the inbox.
	 *
	 * @param etk entellitrak execution context
	 * @return the business key of the custom css page which should be used for the inbox.
	 */
	public static String getEnhancedInboxCustomCSS(final ExecutionContext etk) {
		return (String) getCacheValue(etk, AeaCoreConfigurationItem.ENHANCED_INBOX_CUSTOM_CSS);
	}

	/**
	 * Returns a space separated list of classes to add to the class attribute of the inbox table.
	 *
	 * @param etk entellitrak execution context
	 * @return a space separated list of classes to add to the class attribute of the inbox table.
	 */
	public static String getEnhancedInboxCustomCssClasses(final ExecutionContext etk) {
		return (String) getCacheValue(etk, AeaCoreConfigurationItem.ENHANCED_INBOX_CUSTOM_CSS_CLASSSES);
	}

	/**
	 * Returns whether ae architecture debugging is enabled.
	 *
	 * @param etk entellitrak execution context
	 * @return whether ae architecture debuging is enabled
	 */
	public static boolean getAeaWriteDebugToLog(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.AEA_CORE_WRITE_DEBUG_TO_LOG);
	}

	/**
	 * Returns whether ae architecture recursive debugging is enabled.
	 *
	 * @param etk entellitrak execution context
	 * @return whether ae architecture recursive debugging is enabled
	 */
	public static boolean getAeaAdvancedRecursiveDebug(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.AEA_CORE_ADVANCED_RECURSIVE_DEBUG);
	}

	/**
	 * Returns whether or not System Wide Broadcast is enabled.
	 *
	 * @param etk entellitrak execution context
	 * @return whether or not System Wide Broadcast is enabled.
	 */
	public static boolean getDashboardToolsSystemWideBroadcastEnabled(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.DASHBOARD_TOOLS_SWB_ENABLED);
	}

	/**
	 * Returns the label of the fieldset which contains the current broadcasts.
	 *
	 * @param etk entellitrak execution context
	 * @return the label of the fieldset which contains the current broadcasts
	 */
	public static String getSystemWideBroadcastLabel(final ExecutionContext etk) {
		return (String) getCacheValue(etk, AeaCoreConfigurationItem.SWB_LABEL);
	}

	/**
	 * Returns whether or not the System Wide Broadcast fieldset should be shown when there are no
	 * currently active broadcasts to display.
	 *
	 * @param etk entellitrak execution context
	 * @return whether or not the System Wide Broadcast fieldset should be shown when there are no
	 *      currently active broadcasts to display.
	 */
	public static boolean getSystemWideBroadcastShowFieldsetWhenEmpty(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.SWB_SHOW_FIELDSET_WHEN_EMPTY);
	}

	/**
	 * Returns whether or not the date when the broadcast started appearing should be shown.
	 *
	 * @param etk entellitrak execution context
	 * @return whether or not the date when the broadcast started appearing should be shown.
	 */
	public static boolean getSystemWideBroadcastShowDate(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.SWB_SHOW_DATE);
	}

	/**
	 * Returns the message to show when there are no currently active broadcasts.
	 *
	 * @param etk entellitrak execution context
	 * @return the message to show when there are no currently active broadcasts.
	 */
	public static String getSystemWideBroadcastNoBroadcastsMessage(final ExecutionContext etk) {
		return (String) getCacheValue(etk, AeaCoreConfigurationItem.SWB_NO_BROADCASTS_MESSAGE);
	}

	/**
	 * Returns whether or not to paginate the broadcasts.
	 *
	 * @param etk entellitrak execution context
	 * @return whether or not to paginate the broadcasts
	 */
	public static boolean getSystemWideBroadcastPaginate(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.SWB_PAGINATE);
	}

	/**
	 * Returns the height in pixels (Integer) or "dynamic".
	 *
	 * @param etk entellitrak execution context
	 * @return the height in pixels (Integer) or "dynamic"
	 */
	public static String getSystemWideBroadcastHeight(final ExecutionContext etk) {
		return (String) getCacheValue(etk, AeaCoreConfigurationItem.SWB_HEIGHT);
	}

	/**
	 * Returns the number of broadcasts to be displayed per page when paginating.
	 *
	 * @param etk entellitrak execution context
	 * @return the number of broadcasts to be displayed per page when paginating
	 */
	public static long getSystemWideBroadcastBroadcastsPerPage(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.SWB_BROADCASTS_PER_PAGE);
	}

	/**
	 * Returns the number of months that should not be archived.  Default is 1
	 * month (the current month).
	 *
	 * @param etk entellitrak execution context
	 * @return the number of months that should not be archived
	 */
	public static long getCalendarMonthsNotArchived(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.CALENDAR_MONTHS_NOT_ARCHIVED);
	}

	/**
	 * Returns whether or not Calendar is enabled.
	 *
	 * @param etk entellitrak execution context
	 * @return whether or not Calendar is enabled.
	 */
	public static boolean getDashboardToolsCalendarEnabled(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.DASHBOARD_TOOLS_CALENDAR_ENABLED);
	}

	/**
	 * Returns the number of months that should not be deleted.  Default is 3
	 * month (the current month).
	 *
	 * @param etk entellitrak execution context
	 * @return the number of months that should not be deleted
	 */
	public static long getCalendarMonthsNotDeleted(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.CALENDAR_MONTHS_NOT_DELETED);
	}

	/**
	 * Returns whether the archived events should be displayed on the calendar.
	 *
	 * @param etk entellitrak execution context
	 * @return whether the archived events should be displayed on the calendar
	 */
	public static boolean getCalendarDisplayArchivedEvents(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.CALENDAR_DISPLAY_ARCHIVED_EVENTS);
	}

	/**
	 * Returns the width in pixels (Integer) or "max".
	 *
	 * @param etk entellitrak execution context
	 * @return the width in pixels (Integer) or "max"
	 */
	public static String getCalendarWidth(final ExecutionContext etk) {
		return (String) getCacheValue(etk, AeaCoreConfigurationItem.CALENDAR_WIDTH);
	}


	/**
	 * Returns a list of ; separated TABLE.COLUMN values that are excluded from being detected by the DU
	 * Mismatched Column Types utility.
	 *
	 * @param etk entellitrak execution context
	 * @return String containing TABLE.COLUMN;TABLE.COLUMN values to exclude.
	 */
	public static List<String> getDuMismatchColumnExclusions(final ExecutionContext etk) {
		@SuppressWarnings("unchecked")
		final List<String> typedValue = (List<String>) getCacheValue(etk,
				AeaCoreConfigurationItem.DU_MISMATCHED_COLUMN_TYPE_EXCLUSIONS);
		return typedValue;
	}

	/**
	 * Returns a list of Strings representing the regular expressions of object names that should be skipped by code search.
	 *
	 * @param etk entellitrak execution context
	 * @return The list of strings representing regular expressions.
	 */
	public static List<String> getCodeSearchObjectNameExclusionRegexes(final ExecutionContext etk) {
		@SuppressWarnings("unchecked")
		final List<String> typedValue = (List<String>) getCacheValue(etk,
				AeaCoreConfigurationItem.DU_CODESEARCH_OBJECT_NAME_EXCLUSION_REGEXES);
		return Optional.ofNullable(typedValue).orElse(Collections.emptyList());
	}

	/**
	 * Returns the account name (distributedtransaction or configured) for the Distributed Transaction System User.
	 *
	 * @param etk entellitrak execution context
	 * @return the account name for the distributedtransaction System user.
	 */
	public static String getDistributedTransactionAccountName(final ExecutionContext etk) {
		return (String) getCacheValue(etk, AeaCoreConfigurationItem.DISTRIBUTED_TRANSACTION_ACCOUNT_NAME);
	}

	/**
	 * Returns the client ID for the distributedtransaction System user.
	 *
	 * @param etk entellitrak execution context
	 * @return   the client ID for the distributedtransaction System user.
	 */
	public static String getDistributedTransactionOauth2ClientId(final ExecutionContext etk) {
		return (String) getCacheValue(etk, AeaCoreConfigurationItem.DISTRIBUTED_TRANSACTION_OAUTH2_CLIENT_ID);
	}

	/**
	 * Returns the client secret for the distributedtransaction System user.
	 *
	 * @param etk entellitrak execution context
	 * @return  the client secret for the distributedtransaction System user.
	 */
	public static String getDistributedTransactionOauth2ClientSecret(final ExecutionContext etk) {
		return (String) getCacheValue(etk, AeaCoreConfigurationItem.DISTRIBUTED_TRANSACTION_OAUTH2_CLIENT_SECRET);
	}

	/**
	 * The public URL for the site.
	 *
	 * @param etk entellitrak execution context
	 * @return The public URL for the site.
	 */
	public static String getDistributedTransactionUrl(final ExecutionContext etk) {
		return (String) getCacheValue(etk, AeaCoreConfigurationItem.DISTRIBUTED_TRANSACTION_URL);
	}

	/**
	 * The size of the transaction batch.
	 *
	 * @param etk entellitrak execution context
	 * @return The size of the transaction batch.
	 */
	public static Long getDistributedTransactionBatchSize(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.DISTRIBUTED_TRANSACTION_BATCH_SIZE);
	}

	/**
	 * The maximum number of times the distributed transaction will attempt to retry a failed transaction.
	 *
	 * @param etk entellitrak execution context
	 * @return Maximum number of times the distrubuted transaction service will retry the transaction.
	 */
	public static Long getDistributedTransactionMaxRetries(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.DISTRIBUTED_TRANSACTION_MAX_RETRIES);
	}

	/**
	 * The maximum number of worker threads the distributed transaction service will start.
	 *
	 * @param etk entellitrak execution context
	 * @return Maximum number of worker threads the distributed transaction service will start.
	 */
	public static Long getDistributedTransactionMaxThreadCount(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.DISTRIBUTED_TRANSACTION_MAX_THREAD_COUNT);
	}

	/**
	 * The minimum number of worker threads the distributed transaction service will start.
	 *
	 * @param etk entellitrak execution context
	 * @return Minimum number of worker threads the distributed transaction service will start.
	 */
	public static Long getDistributedTransactionMinThreadCount(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.DISTRIBUTED_TRANSACTION_MIN_THREAD_COUNT);
	}

	/**
	 * How often the distributed transaction scheduler will check to see if distributed transction threads
	 * have completed processing, in ms.
	 *
	 * @param etk entellitrak execution context
	 * @return How often the distributed transaction scheduler will check to see if distributed transction threads
	 *      have completed processing, in ms.
	 */
	public static Long getDistributedTransactionPollingInterval(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.DISTRIBUTED_TRANSACTION_POLLING_INTERVAL);
	}

	/**
	 * How long a worker thread will be allowed to process before being marked as error and the transaction is forced
	 * to roll back, in seconds.
	 *
	 * @param etk entellitrak execution context
	 * @return How long a worker thread will be allowed to process before being marked as error and the transaction is forced
	 *      to roll back, in seconds.
	 */
	public static Long getDistributedTransactionProcessingTimeout(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.DISTRIBUTED_TRANSACTION_PROCESSING_TIMEOUT);
	}

	/**
	 * Username that should be used in jobs instead of etk.getCurrentUser().
	 * @param etk entellitrak execution context
	 * @return Username that should be used in job contexts
	 */
	public static User getServiceAccount(final ExecutionContext etk) {
		final UserService userService = etk.getUserService();

		final String username = (String) getCacheValue(etk, AeaCoreConfigurationItem.SERVICE_ACCOUNT);
		return userService.getUser(username);
	}

	public static long getLsMaxSearchResults(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.LS_MAX_SEARCH_RESULTS);
	}

	public static boolean getLsAccessEnabled(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.LS_ENABLE_ACCESS_VALIDATION);
	}

	public static long getLsCharactersNeededForSearch(final ExecutionContext etk) {
		return (Long) getCacheValue(etk, AeaCoreConfigurationItem.LS_CHARACTERS_NEEDED_FOR_SEARCH);
	}

	public static boolean getLsDebugMode(final ExecutionContext etk) {
		return (Boolean) getCacheValue(etk, AeaCoreConfigurationItem.LS_DEBUG_MODE);
	}
}
