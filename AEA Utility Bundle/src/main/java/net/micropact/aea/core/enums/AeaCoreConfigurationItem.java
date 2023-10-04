package net.micropact.aea.core.enums;

import net.micropact.aea.core.deserializer.IDeserializer;
import net.micropact.aea.core.deserializer.IdentityDeserializer;
import net.micropact.aea.core.deserializer.LongDeserializer;
import net.micropact.aea.core.deserializer.NewlineTrimmedNoBlanksDeserializer;
import net.micropact.aea.core.deserializer.NotZeroDeserializer;
import net.micropact.aea.core.deserializer.NullDefaultingDeserializer;
import net.micropact.aea.core.deserializer.TrueDeserializer;

/**
 * This Enum holds information regarding the AEA CORE Configuration RDO.
 *
 * @author zmiller
 */
public enum AeaCoreConfigurationItem{

    /** EU_DAYS_UNTIL_DELETE_EMAILS_FROM_QUEUE. */
    EU_DAYS_UNTIL_DELETE_EMAILS_FROM_QUEUE("eu.daysUntilDeleteEmailsFromQueue", true, new LongDeserializer(null)),
    /** EU_MINUTES_UNTIL_ABORT_RESENDING_ERRORS. */
    EU_MINUTES_UNTIL_ABORT_RESENDING_ERRORS("eu.minutesUntilAbortResendingErrors", true, new LongDeserializer(null)),
    /** EU_ENABLE_EMAIL. */
    EU_ENABLE_EMAIL("eu.enableEmail", true, new NotZeroDeserializer()),

    /** AEA_CORE_CACHE_STATIC_CONTENT. */
    AEA_CORE_CACHE_STATIC_CONTENT("aea.core.cacheStaticContent", true, new NotZeroDeserializer()),
    /** AEA_CORE_WRITE_DEBUG_TO_LOG. */
    AEA_CORE_WRITE_DEBUG_TO_LOG("writeDebugToLog", true, new TrueDeserializer()),
    /** AEA_CORE_ADVANCED_RECURSIVE_DEBUG. */
    AEA_CORE_ADVANCED_RECURSIVE_DEBUG("advancedRecursiveDebug", true, new TrueDeserializer()),

    /** DU_CODESEARCH_OBJECT_NAME_EXCLUSION_REGEXES. */
    DU_CODESEARCH_OBJECT_NAME_EXCLUSION_REGEXES("du.codeSearch.objectNameExclusionRegexes",
    		true, new NewlineTrimmedNoBlanksDeserializer()),
    /** DU_MISMATCHED_COLUMN_TYPE_EXCLUSIONS. */
    DU_MISMATCHED_COLUMN_TYPE_EXCLUSIONS("du.mismatchedColumnTypeExclusions",
            true, new NewlineTrimmedNoBlanksDeserializer()),

    /** DASHBOARD_TOOLS_ENHANCED_INBOX_ENABLED. */
    DASHBOARD_TOOLS_ENHANCED_INBOX_ENABLED("dt.enhancedInboxEnabled", true, new TrueDeserializer()),
    /** DASHBOARD_TOOLS_SWB_ENABLED. */
    DASHBOARD_TOOLS_SWB_ENABLED("dt.systemWideBroadcastEnabled", true, new TrueDeserializer()),
    /** DASHBOARD_TOOLS_CALENDAR_ENABLED. */
    DASHBOARD_TOOLS_CALENDAR_ENABLED("dt.calendarEnabled", true, new TrueDeserializer()),

    DBUTILS_RDOEXPORT_EXPORT_MAX_LINES("dbutils.rdoExport.rdoExportMaxLines", true, new LongDeserializer(0L)),

    /** DISTRIBUTED_TRANSACTION_ACCOUNT_NAME. */
    DISTRIBUTED_TRANSACTION_ACCOUNT_NAME("distributedTransaction.accountName", true, new IdentityDeserializer()),
    /** DISTRIBUTED_TRANSACTION_OAUTH2_CLIENT_ID. */
    DISTRIBUTED_TRANSACTION_OAUTH2_CLIENT_ID("distributedTransaction.oauth2ClientId", true, new IdentityDeserializer()),
    /** DISTRIBUTED_TRANSACTION_OAUTH2_CLIENT_SECRET. */
    DISTRIBUTED_TRANSACTION_OAUTH2_CLIENT_SECRET("distributedTransaction.oauth2ClientSecret", true, new IdentityDeserializer()),
    /** DISTRIBUTED_TRANSACTION_URL. */
    DISTRIBUTED_TRANSACTION_URL("distributedTransaction.url", true, new IdentityDeserializer()),
    /** DISTRIBUTED_TRANSACTION_BATCH_SIZE. */
    DISTRIBUTED_TRANSACTION_BATCH_SIZE("distributedTransaction.batchSize", true, new LongDeserializer(5L)),
    /** DISTRIBUTED_TRANSACTION_MAX_RETRIES. */
    DISTRIBUTED_TRANSACTION_MAX_RETRIES("distributedTransaction.maxRetries", true, new LongDeserializer(5L)),
    /** DISTRIBUTED_TRANSACTION_MAX_THREAD_COUNT. */
    DISTRIBUTED_TRANSACTION_MAX_THREAD_COUNT("distributedTransaction.maxThreadCount", true, new LongDeserializer(6L)),
    /** DISTRIBUTED_TRANSACTION_MIN_THREAD_COUNT. */
    DISTRIBUTED_TRANSACTION_MIN_THREAD_COUNT("distributedTransaction.minThreadCount", true, new LongDeserializer(3L)),
    /** DISTRIBUTED_TRANSACTION_POLLING_INTERVAL. */
    DISTRIBUTED_TRANSACTION_POLLING_INTERVAL("distributedTransaction.pollingInterval", true, new LongDeserializer(1000L)),
    /** DISTRIBUTED_TRANSACTION_PROCESSING_TIMEOUT. */
    DISTRIBUTED_TRANSACTION_PROCESSING_TIMEOUT("distributedTransaction.processingTimeout", true, new LongDeserializer(1800L)),

    /** ENHANCED_INBOX_USES_GROUPS. */
    ENHANCED_INBOX_USES_GROUPS("enhancedInbox.usesGroups", true, new TrueDeserializer()),
    /** ENHANCED_INBOX_SHOW_COUNT. */
    ENHANCED_INBOX_SHOW_COUNT("enhancedInbox.showCount", true, new TrueDeserializer()),
    /** ENHANCED_INBOX_SHOW_ALL_INBOXES. */
    ENHANCED_INBOX_SHOW_ALL_INBOXES("enhancedInbox.showAllInboxes", true, new TrueDeserializer()),
    /** ENHANCED_INBOX_DISPLAY_TITLE. */
    ENHANCED_INBOX_DISPLAY_TITLE("enhancedInbox.displayTitle", true, new IdentityDeserializer()),
    /** ENHANCED_INBOX_DISPLAY_LENGTH. */
    ENHANCED_INBOX_DISPLAY_LENGTH("enhancedInbox.displayLength", true, new LongDeserializer(20L)),
    /** ENHANCED_INBOX_CUSTOM_JAVASCRIPT. */
    ENHANCED_INBOX_CUSTOM_JAVASCRIPT("enhancedInbox.customJavaScript", true, new IdentityDeserializer()),
    /** ENHANCED_INBOX_CUSTOM_CSS. */
    ENHANCED_INBOX_CUSTOM_CSS("enhancedInbox.customCSS", true, new IdentityDeserializer()),
    /** ENHANCED_INBOX_CUSTOM_CSS_CLASSSES. */
    ENHANCED_INBOX_CUSTOM_CSS_CLASSSES("enhancedInbox.customCssClasses", true, new IdentityDeserializer()),

    LS_CHARACTERS_NEEDED_FOR_SEARCH("ls.charactersNeededForSearch", true, new LongDeserializer(3L)),
    LS_DEBUG_MODE("ls.debugMode", true, new TrueDeserializer()),
    LS_ENABLE_ACCESS_VALIDATION("ls.enableAccessValidation", true, new TrueDeserializer()),
    LS_MAX_SEARCH_RESULTS("ls.maxSearchResults", true, new LongDeserializer(20L)),

    /** SWB_LABEL. */
    SWB_LABEL("swb.label", true, new IdentityDeserializer()),
    /** SWB_SHOW_FIELDSET_WHEN_EMPTY. */
    SWB_SHOW_FIELDSET_WHEN_EMPTY("swb.showFieldsetWhenEmpty", true, new TrueDeserializer()),
    /** SWB_SHOW_DATE. */
    SWB_SHOW_DATE("swb.showDate", true, new TrueDeserializer()),
    /** SWB_PAGINATE. */
    SWB_PAGINATE("swb.paginate", true, new TrueDeserializer()),
    /** SWB_HEIGHT. */
    SWB_HEIGHT("swb.height", true, new IdentityDeserializer()),
    /** SWB_BROADCASTS_PER_PAGE. */
    SWB_BROADCASTS_PER_PAGE("swb.broadcastsPerPage", true, new LongDeserializer(1L)),
    /** SWB_NO_BROADCASTS_MESSAGE. */
    SWB_NO_BROADCASTS_MESSAGE("swb.noBroadcastsMessage", true, new IdentityDeserializer()),

    /** CALENDAR_MONTHS_NOT_ARCHIVED. */
    CALENDAR_MONTHS_NOT_ARCHIVED("calendar.monthsNotArchived", true, new LongDeserializer(1L)),
    /** CALENDAR_MONTHS_NOT_DELETED. */
    CALENDAR_MONTHS_NOT_DELETED("calendar.monthsNotDeleted", true, new LongDeserializer(3L)),
    /** CALENDAR_DISPLAY_ARCHIVED_EVENTS. */
    CALENDAR_DISPLAY_ARCHIVED_EVENTS("calendar.displayArchivedEvents", true, new TrueDeserializer()),
    /** CALENDAR_WIDTH. */
    CALENDAR_WIDTH("calendar.dashboardWidth", true, new IdentityDeserializer()),

    /** JOB_SERVICE_ACCOUNT */
    SERVICE_ACCOUNT("aea.core.serviceAccount", true, new NullDefaultingDeserializer<>("administrator", new IdentityDeserializer()));

    private final String configurationCode;
    private final boolean isCacheable;
    private final IDeserializer<?> deserializer;

    /**
     * Simple Constructor.
     *
     * @param code The value of the &quot;Code&quot; element used to identify this Configuration Item
     * @param shouldBeCached whether or not the value of the configuration item should be cached
     * @param theDeserializer Method of deserializing the value stored in the RDO to an actual strongly typed value
     */
    AeaCoreConfigurationItem(final String code,
            final boolean shouldBeCached,
            final IDeserializer<?> theDeserializer){
        configurationCode = code;
        isCacheable = shouldBeCached;
        deserializer = theDeserializer;
    }

    /**
     * The value of the "Code" element used to identify this Configuration Item.
     *
     * @return The value of the "Code" element used to identify this Configuration Item
     */
    public String getCode(){
        return configurationCode;
    }

    /**
     * Whether this configuration item should have its value cached.
     *
     * @return if the {@link AeaCoreConfigurationItem} should have its value cached
     */
    public boolean isCacheable(){
        return isCacheable;
    }

    /**
     * An object which is capable of deserializing the String that comes from the RDO into a more appropriate type.
     *
     * @return an object which is capable of deserializing the String that comes from the RDO into a more appropriate
     *      type.
     */
    public IDeserializer<?> getDeserializer(){
        return deserializer;
    }
}
