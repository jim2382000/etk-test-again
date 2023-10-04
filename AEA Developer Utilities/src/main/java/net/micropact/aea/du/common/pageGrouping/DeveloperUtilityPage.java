package net.micropact.aea.du.common.pageGrouping;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.entellitrak.menu.MenuExecutionContext;
import com.entellitrak.menu.MenuItem;

/**
 * Enum representing a single developer utility page.
 *
 * @author Zachary.Miller
 */
public enum DeveloperUtilityPage {

    /** CODE_SEARCH */
    CODE_SEARCH(DeveloperUtilityPageGroup.COMMON, "Code Search", new PageDuPageType("du.page.codeSearch")),
    /** LOG_VIEWER */
    LOG_VIEWER(DeveloperUtilityPageGroup.COMMON, "Log Viewer", new PageDuPageType("du.page.logViewer")),
    /** RDO_TEXT_SEARCH */
    RDO_TEXT_SEARCH(DeveloperUtilityPageGroup.COMMON, "RDO Text Search", new PageDuPageType("du.page.rdoTextSearch")),
    /** SHARE_PAGES_REPORTS_ADMINISTRATOR */
    SHARE_PAGES_REPORTS_ADMINISTRATOR(DeveloperUtilityPageGroup.COMMON, "Grant Administrators Page/Report Permissions", new PageDuPageType("du.page.sharePagesReportsWithAdministrators")),
    /** TRACKING_CONFIGURATION_ID */
    TRACKING_CONFIGURATION_ID(DeveloperUtilityPageGroup.COMMON, "Tracking Configuration Id", new PageDuPageType("du.page.trackingConfigurationId")),
    /** JAVADOC */
    JAVADOC(DeveloperUtilityPageGroup.COMMON, "Open Javadoc", new DirectRelativeLinkDuPageType("documentation/javadocs/index.html")),
    /** VIEW_PAGE_PERMISSIONS */
    VIEW_PAGE_PERMISSIONS(DeveloperUtilityPageGroup.EXPLORATION, "View Page Permissions", new PageDuPageType("du.page.viewPagePermissions")),
    /** TRACKED_DATA_OBJECT_GRAPH */
    TRACKED_DATA_OBJECT_GRAPH(DeveloperUtilityPageGroup.EXPLORATION, "Tracked Data Object Graph", new PageDuPageType("du.page.trackedDataObjectGraph")),
    /** CHANGED_DATA_MODEL_NAMES */
    CHANGED_DATA_MODEL_NAMES(DeveloperUtilityPageGroup.EXPLORATION, "Changed Data Model Names", new PageDuPageType("du.page.changedDataModelNames")),
    /** DATA_DICTIONARY */
    DATA_DICTIONARY(DeveloperUtilityPageGroup.EXPLORATION, "Data Dictionary", new PageDuPageType("du.page.dataDictionary")),
    /** ROLE_DATA_PERMISSIONS */
    ROLE_DATA_PERMISSIONS(DeveloperUtilityPageGroup.EXPLORATION, "View Role Data Permissions", new PageDuPageType("du.page.roleDataPermissions")),
    /** PUBLIC_RESOURCES */
    PUBLIC_RESOURCES(DeveloperUtilityPageGroup.EXPLORATION, "Public Resources", new PageDuPageType("du.page.publicResources")),
    /** SCRIPT_OBJECT_USAGE */
    SCRIPT_OBJECT_USAGE(DeveloperUtilityPageGroup.EXPLORATION, "Script Object Usage", new PageDuPageType("du.page.scriptObjectUsage")),
    /** VIEW_OBJECT_DATA */
    VIEW_OBJECT_DATA(DeveloperUtilityPageGroup.EXPLORATION, "View Object Data", new PageDuPageType("du.page.viewObjectData")),
    /** DUPLICATE_CODE_VALUES */
    DUPLICATE_CODE_VALUES(DeveloperUtilityPageGroup.EXPLORATION, "Duplicate Code Values", new PageDuPageType("du.page.duplicateCodeValues")),
    /** VIEW_LOCKED_DOCUMENT_MANAGEMENT_FILES */
    VIEW_LOCKED_DOCUMENT_MANAGEMENT_FILES(DeveloperUtilityPageGroup.EXPLORATION, "View Locked Document Management Files", new PageDuPageType("du.page.viewLockedDocumentManagementFiles")),
    /** SMTP_TESTER */
    SMTP_TESTER(DeveloperUtilityPageGroup.DIAGNOSTIC, "SMTP Tester", new PageDuPageType("du.page.smtpTester")),
    /** EXECUTION_REPOSITORY */
    EXECUTION_REPOSITORY(DeveloperUtilityPageGroup.DIAGNOSTIC, "Execution Repository", new PageDuPageType("du.page.executionRepository")),
    /** MISMATCHED_COLUMN_TYPES */
    MISMATCHED_COLUMN_TYPES(DeveloperUtilityPageGroup.DIAGNOSTIC, "Mismatched Column Types", new PageDuPageType("du.page.mismatchedColumnTypes")),
    /** VIEW_PAGE_REPORT_DASHBOARD_DISPLAY */
    VIEW_PAGE_REPORT_DASHBOARD_DISPLAY(DeveloperUtilityPageGroup.DIAGNOSTIC, "View Page Report Dashboard Display", new PageDuPageType("du.page.viewPageReportDashboardDisplay")),
    /** VIEW_BAD_DATES */
    VIEW_BAD_DATES(DeveloperUtilityPageGroup.DIAGNOSTIC, "View Bad Dates", new PageDuPageType("du.page.viewBadDates")),
    /** CHECK_SIMPLE_WORKFLOW_CORRUPTION */
    CHECK_SIMPLE_WORKFLOW_CORRUPTION(DeveloperUtilityPageGroup.DIAGNOSTIC, "Check Simple Workflow Corruption", new PageDuPageType("du.page.checkSimpleWorkflowCorruption")),
    /** SCHEDULER_JOB_STATUS */
    SCHEDULER_JOB_STATUS(DeveloperUtilityPageGroup.DIAGNOSTIC, "Scheduler Job Status", new PageDuPageType("du.page.schedulerJobStatusStandalone")),
    /** UPDATE_LOG_VIEWER */
    UPDATE_LOG_VIEWER(DeveloperUtilityPageGroup.DIAGNOSTIC, "View Update Log", new PageDuPageType("du.page.updateLogViewer")),
    /** BUNDLE_ORPHANS */
    BUNDLE_ORPHANS(DeveloperUtilityPageGroup.DIAGNOSTIC, "View Bundle Orphans", new PageDuPageType("du.page.bundleOrphans")),
    /** VIEW_JAVA_SYSTEM_PROPERTIES */
    VIEW_JAVA_SYSTEM_PROPERTIES(DeveloperUtilityPageGroup.DIAGNOSTIC, "View Java System Properties", new PageDuPageType("du.page.viewJavaSystemProperties")),
    /** VIEW_HTTP_Headers */
    VIEW_HTTP_HEADERS(DeveloperUtilityPageGroup.DIAGNOSTIC, "View HTTP Headers", new PageDuPageType("du.page.viewHttpHeaders")),
    /** CREATE_STANDARD_RDO */
    CREATE_STANDARD_RDO(DeveloperUtilityPageGroup.TASKS, "Create Standard RDO", new PageDuPageType("du.page.createStandardRDO")),
    /** CREATE_STANDARD_PAGE */
    CREATE_STANDARD_PAGE(DeveloperUtilityPageGroup.TASKS, "Create Standard Page", new PageDuPageType("du.page.createStandardPage")),
    /** BULK_DELETE_DATA */
    BULK_DELETE_DATA(DeveloperUtilityPageGroup.TASKS, "Bulk Delete Data", new PageDuPageType("du.page.bulkDeleteData")),
    /** CONVERT_SUBREPORT_EXPRESSIONS */
    CONVERT_SUBREPORT_EXPRESSIONS(DeveloperUtilityPageGroup.TASKS, "Convert Subreport Expressions", new PageDuPageType("du.page.convertSubreportExpressions")),
    /** SYSTEM_PREFERENCES_TOOL */
    SYSTEM_PREFERENCES_TOOL(DeveloperUtilityPageGroup.TASKS, "System Preferences Tool", new PageDuPageType("du.page.systemPreferencesTool")),
    /** ORGANIZATIONAL_UNIT_IMPORT */
    ORGANIZATIONAL_UNIT_IMPORT(DeveloperUtilityPageGroup.TASKS, "Hierarchy Import/Export", new PageDuPageType("du.page.organizationalUnitImport")),
    /** UNUSED_TABLES */
    UNUSED_TABLES(DeveloperUtilityPageGroup.UNUSED_ITEMS, "View Unused Tables", new PageDuPageType("du.page.unusedTables")),
    /** UNUSED_COLUMNS */
    UNUSED_COLUMNS(DeveloperUtilityPageGroup.UNUSED_ITEMS, "View Unused Columns", new PageDuPageType("du.page.unusedColumns")),
    /** UNUSED_DATA_ELEMENTS */
    UNUSED_DATA_ELEMENTS(DeveloperUtilityPageGroup.UNUSED_ITEMS, "View Unused Data Elements", new PageDuPageType("du.page.unusedDataElements")),
    /** UNUSED_SCRIPT_OBJECTS */
    UNUSED_SCRIPT_OBJECTS(DeveloperUtilityPageGroup.UNUSED_ITEMS, "View Unused Script Objects", new PageDuPageType("du.page.unusedScriptObjects")),
    /** VIEW_MUTABLE_READ_ONLY_FIELDS */
    VIEW_MUTABLE_READ_ONLY_FIELDS(DeveloperUtilityPageGroup.UNUSED_ITEMS, "View Mutable Read Only Fields", new PageDuPageType("du.page.viewMutableReadOnlyFields")),
    /** TRANSFER_PAGE_OWNERSHIP */
    TRANSFER_PAGE_OWNERSHIP(DeveloperUtilityPageGroup.TRANSFER_OWNERSHIP, "Transfer Page Ownership", new PageDuPageType("du.page.transferPageOwnership")),
    /** TRANSFER_REPORT_OWNERSHIP */
    TRANSFER_REPORT_OWNERSHIP(DeveloperUtilityPageGroup.TRANSFER_OWNERSHIP, "Transfer Report Ownership", new PageDuPageType("du.page.transferReportOwnership")),
    /** TRANSFER_QUERY_OWNERSHIP */
    TRANSFER_QUREY_OWNERSHIP(DeveloperUtilityPageGroup.TRANSFER_OWNERSHIP, "Transfer Query Ownership", new PageDuPageType("du.page.transferQueryOwnership")),
    /** CACHE_MANAGER */
    CACHE_MANAGER(DeveloperUtilityPageGroup.CACHE, "Cache Manager", new PageDuPageType("du.page.cacheManager")),
    /** CLEAR_CACHE */
    CLEAR_CACHE(DeveloperUtilityPageGroup.CACHE, "Clear Cache", new PageDuPageType("du.page.clearCache")),
    /** LOOKUP_DEFINITION_USAGE */
    LOOKUP_DEFINITION_USAGE(DeveloperUtilityPageGroup.LOOKUPS, "Lookup Definition Usage", new PageDuPageType("du.page.lookupDefinitionUsage")),
    /** LOOKUP_COLUMN_REFERENCES */
    LOOKUP_COLUMN_REFERENCES(DeveloperUtilityPageGroup.LOOKUPS, "Lookup Column References", new PageDuPageType("du.page.lookupColumnReferences")),
    /** FOREIGN_KEY_EXPLANATION */
    FOREIGN_KEY_EXPLANATION(DeveloperUtilityPageGroup.LOOKUPS, "Foreign Keys", new PageDuPageType("du.page.foreignKeyExplanation")),
    /** VIEW_ORPHANED_FILES */
    VIEW_ORPHANED_FILES(DeveloperUtilityPageGroup.MAINTENANCE, "View Orphaned Files", new PageDuPageType("du.page.viewOrphanedFiles")),
    /** CLEAN_ORPHANED_ASSIGNMENTS */
    CLEAN_ORPHANED_ASSIGNMENTS(DeveloperUtilityPageGroup.MAINTENANCE, "Clean Orphaned Assignments", new PageDuPageType("du.page.cleanOrphanedAssignments")),
    /** CLEAN_ORPHANED_M_TABLE_ENTRIES */
    CLEAN_ORPHANED_M_TABLE_ENTRIES(DeveloperUtilityPageGroup.MAINTENANCE, "Clean Orphaned M_ Table Entries", new PageDuPageType("du.page.cleanOrphanedMTableEntries")),
    /** DUPLICATE_PAGE_DASHBOARD_OPTIONS */
    DUPLICATE_PAGE_DASHBOARD_OPTIONS(DeveloperUtilityPageGroup.MAINTENANCE, "View Duplicate Page Dashboard Options", new PageDuPageType("du.page.duplicatePageDashboardOptions")),
    /** DUPLICATE_REPORT_DASHBOARD_OPTIONS */
    DUPLICATE_REPORT_DASHBOARD_OPTIONS(DeveloperUtilityPageGroup.MAINTENANCE, "View Duplicate Report Dashboard Options", new PageDuPageType("du.page.duplicateReportDashboardOptions")),
    /** DUPLICATE_VIEW_FILTER */
    DUPLICATE_VIEW_FILTER(DeveloperUtilityPageGroup.MAINTENANCE, "View Duplicate View Filters", new PageDuPageType("du.page.duplicateViewFilters")),
    /** DUPLICATE_DATA_PERMISSIONS */
    DUPLICATE_DATA_PERMISSIONS(DeveloperUtilityPageGroup.MAINTENANCE, "View Duplicate Data Permissions", new PageDuPageType("du.page.duplicateDataPermissions")),
    /** CLEAN_ORPHANED_SUBJECTS */
    CLEAN_ORPHANED_SUBJECTS(DeveloperUtilityPageGroup.MAINTENANCE, "Clean Orphaned Subjects", new PageDuPageType("du.page.cleanOrphanedSubjects")),
    /** ORPHANED_SHARED_OBJECT_PERMISSIONS */
    ORPHANED_SHARED_OBJECT_PERMISSIONS(DeveloperUtilityPageGroup.MAINTENANCE, "View Orphaned Shared Object Permissions", new PageDuPageType("du.page.orphanedSharedObjectPermissions")),
    /** CLEAN_RDO_BASE_OBJECT_FLAG */
    CLEAN_RDO_BASE_OBJECT_FLAG(DeveloperUtilityPageGroup.MAINTENANCE, "Clean RDO Base Object Flag", new PageDuPageType("du.page.cleanRdoBaseObjectFlag"));

    private final DeveloperUtilityPageGroup developerUtilityPageGroup;
    private final String name;
    private final IDuPageType duPageType;


    DeveloperUtilityPage(final DeveloperUtilityPageGroup theDeveloperUtilityPageGroup, final String theName, final IDuPageType theDuPageType){
        developerUtilityPageGroup = theDeveloperUtilityPageGroup;
        name = theName;
        duPageType = theDuPageType;
    }

    /**
     * Get the name of a resource.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the menu item for a particular resource.
     *
     * @param etk entellitrak execution context
     * @return the menu item
     */
    public MenuItem getMenuItem(final MenuExecutionContext etk) {
        return duPageType.getMenuItem(etk, this);
    }

    /**
     * Get the URL of the page.
     *
     * @return the URL
     */
    public String getUrl() {
        return duPageType.getUrl();
    }

    /**
     * Get the Pages in a particular group.
     *
     * @param developerUtilityPageGroup the group
     * @return the pages
     */
    public static List<DeveloperUtilityPage> getDeveloperUtilityPagesInGroup(final DeveloperUtilityPageGroup developerUtilityPageGroup){
        return Arrays.stream(values())
            .filter(page -> Objects.equals(developerUtilityPageGroup, page.developerUtilityPageGroup))
            .collect(Collectors.toList());
    }
}