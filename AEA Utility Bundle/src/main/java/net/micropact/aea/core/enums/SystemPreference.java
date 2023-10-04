package net.micropact.aea.core.enums;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This enum represents the values in the ETK_SYSTEM_PREFERENCE table.
 * The authoritative list seems to come from entellitrak-core/src/main/resources/SystemPreferences.xml
 *
 * @author zmiller
 */
public enum SystemPreference {
    /* name, exportable by default?, default production value */

    /** ADJUST_DAYLIGHT_SAVING. */
    ADJUST_DAYLIGHT_SAVING("adjustDaylightSaving", true, Optional.empty()),
    /** ALLOW_ACCESSIBLE_ASSIGNMENT_ROLES_ONLY. */
    ALLOW_ACCESSIBLE_ASSIGNMENT_ROLES_ONLY("allowAccessibleAssignmentRolesOnly", true, Optional.empty()),
    /** ALLOW_MULTIPLE_LOGIN. */
    ALLOW_MULTIPLE_LOGIN("allowMultipleLogin", true, Optional.empty()),
    /** ALLOW_PASSWORD_RESET_FOR_LOCKED_ACCOUNTS. */
    ALLOW_PASSWORD_RESET_FOR_LOCKED_ACCOUNTS("allowPasswordResetForLockedAccounts", false, Optional.empty()),
    /** ALLOW_SCRIPTS_IN_LOGIN_TEXT. */
    ALLOW_SCRIPTS_IN_LOGIN_TEXT("allowScriptsInLoginText", true, Optional.empty()),
    /** AUTO_REMEMBER_SEARCH_CRITERIA. */
    AUTO_REMEMBER_SEARCH_CRITERIA("autoRememberSearchCriteria", true, Optional.empty()),
    /** BLOCK_SIZE_JR_SWAP_FILE. */
    BLOCK_SIZE_JR_SWAP_FILE("blockSizeJRSwapFile", false, Optional.empty()),
    /** BUSINESS_DAY_EXPRESSED_IN_HOURS. */
    BUSINESS_DAY_EXPRESSED_IN_HOURS("businessDayExpressedInHours", true, Optional.empty()),
    /** BUSINESS_MONTH_EXPRESSED_IN_BUSINESS_DAYS. */
    BUSINESS_MONTH_EXPRESSED_IN_BUSINESS_DAYS("businessMonthExpressedInBusinessDays", true, Optional.empty()),
    /** BUSINESS_WEEK_EXPRESSED_IN_HOURS. */
    BUSINESS_WEEK_EXPRESSED_IN_HOURS("businessWeekExpressedInHours", true, Optional.empty()),
    /** BUSINESS_YEAR_EXPRESSED_IN_BUSINESS_DAYS. */
    BUSINESS_YEAR_EXPRESSED_IN_BUSINESS_DAYS("businessYearExpressedInBusinessDays", true, Optional.empty()),
    /** CALCULATE_LEAP_TIME. */
    CALCULATE_LEAP_TIME("calculateLeapTime", true, Optional.empty()),
    /** CALCULATE_OVERTIME. */
    CALCULATE_OVERTIME("calculateOvertime", true, Optional.empty()),
    /** CAPTURE_READ_DATA_EVENTS_IN_AUDIT_LOG. */
    CAPTURE_READ_DATA_EVENTS_IN_AUDIT_LOG("captureReadDataEventsInAuditLog", false, Optional.empty()),
    /** CASE_SENSITIVE_USERNAME_AUTHENTICATION. */
    CASE_SENSITIVE_USERNAME_AUTHENTICATION("caseSensitiveUsernameAuthentication", true, Optional.empty()),
    /** CHECK_PASSWORD_SIMILARITIES. */
    CHECK_PASSWORD_SIMILARITIES("checkPasswordSimilarities", false, Optional.empty()),
    /** CURRENCY_PRECISION. */
    CURRENCY_PRECISION("currencyPrecision", true, Optional.empty()),
    /** CURRENCY_SCALE. */
    CURRENCY_SCALE("currencyScale", true, Optional.empty()),
    /** CURRENT_ETDL_VERSION. */
    CURRENT_ETDL_VERSION("currentETDLVersion", false, Optional.empty()),
    /** CUSTOM_LOGIN_SCREEN_AGREE. */
    CUSTOM_LOGIN_SCREEN_AGREE("customLoginScreenAgree", true, Optional.empty()),
    /** CUSTOM_LOGIN_SCREEN_HEADER. */
    CUSTOM_LOGIN_SCREEN_HEADER("customLoginScreenHeader", true, Optional.empty()),
    /** CUSTOM_LOGIN_SCREEN_TEXT. */
    CUSTOM_LOGIN_SCREEN_TEXT("customLoginScreenText", true, Optional.empty()),
    /** DATA_GRID_COOKIE_AGE. */
    DATA_GRID_COOKIE_AGE("dataGridCookieAge", true, Optional.empty()),
    /** DEFAULT_MULTI_VALUE_DELIMITER. */
    DEFAULT_MULTI_VALUE_DELIMITER("defaultMultiValueDelimiter", true, Optional.empty()),
    /** DEFAULT_TEXT_SIZE. */
    DEFAULT_TEXT_SIZE("defaultTextSize", true, Optional.empty()),
    /** DEFAULT_TIMESHEET_MANAGER_ROLE. */
    DEFAULT_TIMESHEET_MANAGER_ROLE("defaultTimesheetManagerRole", true, Optional.empty()),
    /** DEFAULT_TIME_ZONE. */
    DEFAULT_TIME_ZONE("defaultTimeZone", false, Optional.empty()),
    /** DISABLE_BROWSER_CACHING. */
    DISABLE_BROWSER_CACHING("disableBrowserCaching", false, Optional.empty()),
    /** DISABLE_PUBLIC_RESOURCE_CACHING. */
    DISABLE_PUBLIC_RESOURCE_CACHING("disablePublicResourceCaching", false, Optional.empty()),
    /** DISPLAY_STATE_ON_SEARCH_FORM. */
    DISPLAY_STATE_ON_SEARCH_FORM("displayStateOnSearchForm", true, Optional.empty()),
    /** DM_SEARCH_SHOW_LINKS. */
    DM_SEARCH_SHOW_LINKS("dmSearchShowLinks", true, Optional.empty()),
    /** DM_SEARCH_THRESHOLD. */
    DM_SEARCH_THRESHOLD("dmSearchThreshold", true, Optional.empty()),
    /** DOC_MGMT_SERVICE_ACCOUNT. */
    DOC_MGMT_SERVICE_ACCOUNT("docMgmtServiceAccount", false, Optional.empty()),
    /** DOWNLOAD_FILE_CHUNK_SIZE. */
    DOWNLOAD_FILE_CHUNK_SIZE("downloadFileChunkSize", true, Optional.empty()),
    /** ENABLE_ADVANCED_SEARCH. */
    ENABLE_ADVANCED_SEARCH("enableAdvancedSearch", true, Optional.empty()),
    /** ENABLE_ADVANCED_SEARCH_LIMIT. */
    ENABLE_ADVANCED_SEARCH_LIMIT("enableAdvancedSearchLimit", true, Optional.empty()),
    /** ENABLE_ADVANCED_SEARCH_LOOKUP_CONTEXT. */
    ENABLE_ADVANCED_SEARCH_LOOKUP_CONTEXT("enableAdvancedSearchLookupContext", true, Optional.empty()),
    /** ENABLE_ANTICLICKJACK. */
    ENABLE_ANTICLICKJACK("enableAnticlickjack", true, Optional.empty()),
    /** ENABLE_AUTOCOMPLETE_OFF. */
    ENABLE_AUTOCOMPLETE_OFF("enableAutocompleteOff", true, Optional.empty()),
    /** ENABLE_DOC_VERSION_COMPARISON. */
    ENABLE_DOC_VERSION_COMPARISON("enableDocVersionComparison", true, Optional.empty()),
    /** ENABLE_DOCUMENT_MANAGEMENT. */
    ENABLE_DOCUMENT_MANAGEMENT("enableDocumentManagement", true, Optional.empty()),
    /** ENABLE_ENDPOINTS. */
    ENABLE_ENDPOINTS("enableEndpoints", true, Optional.empty()),
    /** ENABLE_FORM_CONTROL_TOOLTIP. */
    ENABLE_FORM_CONTROL_TOOLTIP("enableFormControlTooltip", true, Optional.empty()),
    /** ENABLE_FORM_PDFPRINTING. */
    ENABLE_FORM_PDFPRINTING("enableFormPDFPrinting", true, Optional.empty()),
    /** ENABLE_HTML_ESCAPING. */
    ENABLE_HTML_ESCAPING("enableHtmlEscaping", true, Optional.empty()),
    /** ENABLE_LDAP_AUTHENTICATION. */
    ENABLE_LDAP_AUTHENTICATION("enableLdapAuthentication", false, Optional.empty()),
    /** ENABLE_LOCAL_AUTHENTICATION. */
    ENABLE_LOCAL_AUTHENTICATION("enableLocalAuthentication", false, Optional.empty()),
    /** ENABLE_MOBILE_INBOX. */
    ENABLE_MOBILE_INBOX("enableMobileInbox", true, Optional.empty()),
    /** ENABLE_ONLINE_HELP. */
    ENABLE_ONLINE_HELP("enableOnlineHelp", true, Optional.empty()),
    /** ENABLE_ORACLE_CI_SEARCH. */
    ENABLE_ORACLE_CI_SEARCH("enableOracleCISearch", true, Optional.empty()),
    /** ENABLE_PASSWORD_RESET_FEATURE. */
    ENABLE_PASSWORD_RESET_FEATURE("enablePasswordResetFeature", true, Optional.empty()),
    /** ENABLE_PRINT_PERMISSION. */
    ENABLE_PRINT_PERMISSION("enablePrintPermissions", true, Optional.empty()),
    /** ENABLE_PRINTER_FRIENDLY_FORMAT_AND_PRINT. */
    ENABLE_PRINTER_FRIENDLY_FORMAT_AND_PRINT("enablePrinterFriendlyFormatAndPrint", true, Optional.empty()),
    /** ENABLE_PUBLIC_PAGES. */
    ENABLE_PUBLIC_PAGES("enablePublicPages", true, Optional.empty()),
    /** ENABLE_REDIRECT_ON_SESSION_TIMEOUT. */
    ENABLE_REDIRECT_ON_SESSION_TIMEOUT("enableRedirectOnSessionTimeout", true, Optional.empty()),
    /** ENABLE_SAVE_AND_NEW_BUTTON. */
    ENABLE_SAVE_AND_NEW_BUTTON("enableSaveAndNewButton", true, Optional.empty()),
    /** ENABLE_SEARCH_DETAILS. */
    ENABLE_SEARCH_DETAILS("enableSearchDetails", true, Optional.empty()),
    /** ENABLE_SEARCH_EXECUTION_LOG. */
    ENABLE_SEARCH_EXECUTION_LOG("enableSearchExecutionLog", false, Optional.empty()),
    /** ENABLE_SEARCH_FORM_EVENT_HANDLERS. */
    ENABLE_SEARCH_FORM_EVENT_HANDLERS("enableSearchFormEventHandlers", true, Optional.empty()),
    /** ENABLE_SINGLE_FILE_COMPILATION. */
    ENABLE_SINGLE_FILE_COMPILATION("enableSingleFileCompilation", true, Optional.empty()),
    /** ENABLE_SINGLE_RESULT_LOOKUP_CONTEXT. */
    ENABLE_SINGLE_RESULT_LOOKUP_CONTEXT("enableSingleResultLookupContext", true, Optional.empty()),
    /** ENABLE_SINGLE_SIGN_ON. */
    ENABLE_SINGLE_SIGN_ON("enableSingleSignOn", false, Optional.empty()),
    /** ENABLE_STANDARD_SEARCH_EXECUTION_LOG. */
    ENABLE_STANDARD_SEARCH_EXECUTION_LOG("enableStandardSearchExecutionLog", true, Optional.empty()),
    /** ENABLE_TIME_ZONE_MODULE. */
    ENABLE_TIME_ZONE_MODULE("enableTimeZoneModule", true, Optional.empty()),
    /** ENABLE_VIEW_FILTERS. */
    ENABLE_VIEW_FILTERS("enableViewFilters", true, Optional.empty()),
    /** ENABLE_WEBDAV. */
    ENABLE_WEBDAV("enableWebdav", true, Optional.empty()),
    /** ENFORCE_CURRENT_PASSWORD. */
    ENFORCE_CURRENT_PASSWORD("enforceCurrentPassword", false, Optional.of("true")),
    /** ENFORCE_PASSWORD_HISTORY. */
    ENFORCE_PASSWORD_HISTORY("enforcePasswordHistory", false, Optional.of("true")),
    /** ENFORCE_SUBREPORT_PERMISSIONS. */
    ENFORCE_SUBREPORT_PERMISSIONS("enforceSubreportPermissions", true, Optional.empty()),
    /** ENTELLISQL_ENABLED. */
    ENTELLISQL_ENABLED("entelliSqlEnabled", true, Optional.empty()),
    /** FOUO_HEADER. */
    FOUO_HEADER("fouoHeader", true, Optional.empty()),
    /** FOUO_HEADER_ENABLED. */
    FOUO_HEADER_ENABLED("fouoHeaderEnabled", true, Optional.empty()),
    /** FOUO_FOOTER. */
    FOUO_FOOTER("fouoFooter", true, Optional.empty()),
    /** FOUO_FOOTER_ENABLED. */
    FOUO_FOOTER_ENABLED("fouoFooterEnabled", true, Optional.empty()),
    /** HELP_SOURCE */
    HELP_SOURCE("helpSource", false, Optional.empty()),
    /** HELP_URL */
    HELP_URL("helpURL", false, Optional.empty()),
    /** HOURS_FOR_PUBLIC_RESOURCE_CACHING. */
    HOURS_FOR_PUBLIC_RESOURCE_CACHING("hoursForPublicResourceCaching", false, Optional.empty()),
    /** IGNORE_MISSING_REPORT_FONTS. */
    IGNORE_MISSING_REPORT_FONTS("ignoreMissingReportFonts", true, Optional.empty()),
    /** INSTRUCTIONS_FIELD_TEXT. */
    INSTRUCTIONS_FIELD_TEXT("instructionsFieldText", true, Optional.empty()),
    /** JOB_STATUS_UPDATE_INTERVAL. */
    JOB_STATUS_UPDATE_INTERVAL("jobStatusUpdateInterval", true, Optional.empty()),
    /** LDAP_USER_ID_ATTRIBUTE. */
    LDAP_USER_ID_ATTRIBUTE("ldapUserIdAttribute", false, Optional.empty()),
    /** LOAD_CFG_FROM_ETDL. */
    LOAD_CFG_FROM_ETDL("loadCfgFromETDL", true, Optional.empty()),
    /** MAX_ADVANCED_SEARCH_SIZE. */
    MAX_ADVANCED_SEARCH_SIZE("maxAdvancedSearchSize", true, Optional.empty()),
    /** MAX_NUM_BACKUP_INDICES. */
    MAX_NUM_BACKUP_INDICES("maxNumBackupIndices", false, Optional.empty()),
    /** MAX_NUM_CHARACTERS_IN_LONG_TEXT_COLUMNS. */
    MAX_NUM_CHARACTERS_IN_LONG_TEXT_COLUMNS("maxNumCharactersInLongTextColumns", true, Optional.empty()),
    /** MAX_PAGE_SIZE. */
    MAX_PAGE_SIZE("maxPageSize", true, Optional.empty()),
    /** MAX_SIZE_JR_VIRTUAL. */
    MAX_SIZE_JR_VIRTUAL("maxSizeJRVirtual", false, Optional.empty()),
    /** MIN_CHANGE_IN_PASSWORD. */
    MIN_CHANGE_IN_PASSWORD("minimumChangeInPassword", false, Optional.empty()),
    /** MIN_GROW_COUNT_JR_SWAP_FILE. */
    MIN_GROW_COUNT_JR_SWAP_FILE("minGrowCountJRSwapFile", false, Optional.empty()),
    /** MINUTES_LEFT_FOR_TIMEOUT_WARNING. */
    MINUTES_LEFT_FOR_TIMEOUT_WARNING("minutesLeftForTimeoutWarning", false, Optional.empty()),
    /** NUM_PASSWORD_ROTATIONS. */
    NUM_PASSWORD_ROTATIONS("numPasswordRotations", false, Optional.of("10")),
    /** ON_CANCELED_TRANSACTION_CONTINUE_EVALUATING_SUBSEQUENT_TRANSITIONS. */
    ON_CANCELED_TRANSACTION_CONTINUE_EVALUATING_SUBSEQUENT_TRANSITIONS("onCanceledTransactionContinueEvaluatingSubsequentTransitions", true, Optional.empty()),
    /** PASSWORD_EXPIRATION_IN_DAYS. */
    PASSWORD_EXPIRATION_IN_DAYS("passwordExpirationInDays", false, Optional.of("90")),
    /** PASSWORD_FORMAT. */
    PASSWORD_FORMAT("passwordFormat", false, Optional.empty()),
    /** PASSWORD_FORMAT_MESSAGE. */
    PASSWORD_FORMAT_MESSAGE("passwordFormatMessage", false, Optional.empty()),
    /** PASSWORD_RESET_EMAIL_TEXT. */
    PASSWORD_RESET_EMAIL_TEXT("passwordResetEmailText", false, Optional.empty()),
    /** PASSWORDS_EXPIRE. */
    PASSWORDS_EXPIRE("passwordsExpire", false, Optional.of("true")),
    /** PASSWORDS_MINIMUM_AGE. */
    PASSWORDS_MINIMUM_AGE("passwordsMinimumAge", false, Optional.empty()),
    /** PASSWORDS_MINIMUM_AGE_IN_DAYS. */
    PASSWORDS_MINIMUM_AGE_IN_DAYS("passwordsMinimumAgeInDays", false, Optional.empty()),
    /** REPORT_OPTION_MESSAGE. */
    REPORT_OPTION_MESSAGE("reportOptionMessage", true, Optional.empty()),
    /** RESET_BEANSHELL_INTERPRETER. */
    RESET_BEANSHELL_INTERPRETER("resetBeanshellInterpreter", true, Optional.empty()),
    /** RETRIEVE_USERS_FROM_LDAP. */
    RETRIEVE_USERS_FROM_LDAP("retrieveUsersFromLdap", false, Optional.empty()),
    /** SANDBOX_PERMISSIONS. */
    SANDBOX_PERMISSIONS("sandboxPermissions", true, Optional.empty()),
    /** SEARCH_QUERY_TIMEOUT. */
    SEARCH_QUERY_TIMEOUT("searchQueryTimeout", true, Optional.empty()),
    /** SEARCH_QUERY_TIMEOUT_MESSAGE. */
    SEARCH_QUERY_TIMEOUT_MESSAGE("searchQueryTimeoutMessage", true, Optional.empty()),
    /** SERVER_TIME_OFFSET. */
    SERVER_TIME_OFFSET("serverTimeOffset", false, Optional.empty()),
    /** SESSION_TIMEOUT_WARNING_ENABLED. */
    SESSION_TIMEOUT_WARNING_ENABLED("sessionTimeoutWarningEnabled", true, Optional.empty()),
    /** SHOW_STACK_TRACE. */
    SHOW_STACK_TRACE("showStackTrace", false, Optional.of("false")),
    /** SINGLE_SIGN_ON_HEADER. */
    SINGLE_SIGN_ON_HEADER("singleSignOnHeader", false, Optional.empty()),
    /** SINGLE_SIGN_ON_REDIRECT. */
    SINGLE_SIGN_ON_REDIRECT("singleSignOnRedirect", false, Optional.empty()),
    /** SINGLE_SIGN_ON_USE_REMOTE_USER. */
    SINGLE_SIGN_ON_USE_REMOTE_USER("singleSignOnUseRemoteUser", false, Optional.empty()),
    /** SSO_LOGOUT_REDIRECT_URL. */
    SSO_LOGOUT_REDIRECT_URL("ssoLogoutRedirectUrl", false, Optional.empty()),
    /** SYSTEM_CLIENT_ID. */
    SYSTEM_CLIENT_ID("systemClientId", false, Optional.empty()),
    /** TIME_LENGTH_FOR_CODE_EXPIRATION. */
    TIME_LENGTH_FOR_CODE_EXPIRATION("timeLengthForCodeExpiration", true, Optional.empty()),
    /** TIMESHEET_ON_APPROVE. */
    TIMESHEET_ON_APPROVE("timesheetOnApprove", true, Optional.empty()),
    /** TIMESHEET_ON_SAVE. */
    TIMESHEET_ON_SAVE("timesheetOnSave", true, Optional.empty()),
    /** TIMESHEET_ON_SIGN. */
    TIMESHEET_ON_SIGN("timesheetOnSign", true, Optional.empty()),
    /** USE_CUSTOM_LOGIN_SCREEN. */
    USE_CUSTOM_LOGIN_SCREEN("useCustomLoginScreen", true, Optional.empty()),
    /** USE_DATA_GRIDS. */
    USE_DATA_GRIDS("useDataGrids", true, Optional.empty()),
    /** USE_SSO_AUTH_KEY. */
    USE_SSO_AUTH_KEY("useSsoAuthKey", false, Optional.empty()),
    /** VERIFY_LOOKUP_ON_SAVE. */
    VERIFY_LOOKUP_ON_SAVE("verifyLookupValuesOnSave", true, Optional.empty());

    private final String name;
    private final boolean exportableByDefault;
    private final Optional<String> defaultProductionValue;

    /**
     * Simple Constructor.
     *
     * @param theName The name core uses to refer to the System Preference
     * @param isExportableByDefault If all environments should use the same value for the preference
     * @param theDefaultProductionValue The value which should be used in production (or null if there is no recommended
     *          value)
     */
    SystemPreference(final String theName, final boolean isExportableByDefault, final Optional<String> theDefaultProductionValue){
        name = theName;
        exportableByDefault = isExportableByDefault;
        defaultProductionValue = theDefaultProductionValue;
    }

    /**
     * Get the name which core uses for the System Preference.
     *
     * @return The name core uses for the System Preference
     */
    public String getName(){
        return name;
    }

    /**
     * Is the System Preference one which should be the same in all environments in a project.
     *
     * @return Whether this preference is one which should be exported by default. Many values stored in the
     *      ETK_SYSTEM_PREFERENCE table (such as analytics) should have different values in development and production
     */
    public boolean isExportable(){
        return exportableByDefault;
    }

    /**
     * Returns the default value recommended for production. For instance showStackTrace's would return "true".
     * If there is no recommended default for production, this method returns null.
     *
     * @return The recommended preference or null if there is no recommendation
     */
    public Optional<String> getDefaultProductionValue(){
        return defaultProductionValue;
    }

    /**
     * Gets a System Preference by its name.
     *
     * @param name The name of the preference to find
     * @return The System Preference
     */
    public static SystemPreference getPreferenceByName(final String name){
        return Arrays.stream(values())
                .filter(preference -> preference.getName().equals(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Could not find system preference with name \"%s\"",
                                name)));
    }

    public static SystemPreference getPreferenceByCorePreference(final com.entellitrak.SystemPreference corePreference) {
    	return getPreferenceByName(corePreference.getName());
    }

    /**
     * Get all System Preferences which have a recommended value in project systems.
     *
     * @return All System Preferences which have a recommended production value.
     */
    public static Set<SystemPreference> getProductionSystemPreferences(){
        return Arrays.stream(values())
                .filter(preference -> preference.getDefaultProductionValue().isPresent())
                .collect(Collectors.toSet());
    }
}
