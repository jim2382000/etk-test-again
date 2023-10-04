package net.micropact.aea.du.page.cleanOrphanedSubjects;

import java.util.List;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;

import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.query.QueryUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;

/**
 * <p>
 *  This is the controller code for a page which will delete orphaned assignments from ETK_SUBJECT.
 *  If you use the entellitrak APIs normally, then you should never have orphaned records in ETK_SUBJECT, however
 *  it appears that the migration scripts that are used on a number of sites created subjects, but then deleted them
 *  so that they could re-run the imports.
 * </p>
 * <p>
 *  The correct way to solve this issue would be to add proper database constraints on tables such as ETK_SUBJECT so
 *  that the database would not get junk data in it to begin with. Additionally core could have deletes cascade so
 *  that every table would not need to be deleted from manually. After all, that's what relational databases are
 *  for
 * </p>
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class CleanOrphanedSubjectsController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final TextResponse response = etk.createTextResponse();

        BreadcrumbUtility.setBreadcrumbAndTitle(response,
                BreadcrumbUtility.addLastChildFluent(
                        DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                        new SimpleBreadcrumb("Clean Orphaned Subjects",
                                "page.request.do?page=du.page.cleanOrphanedSubjects")));

        cleanOrphanedSubjects(etk);

        return response;
    }

    /**
     * <p>
     *  Deletes any subjects which are not referenced by ETK_USER or ETK_GROUP.
     *  This entire page could be written as just a couple SQL calls, but instead it's a bunch of java methods.
     *  If performance is a problem, then we can go and start combining them into larger SQL queries which delete more
     *  than one method at a time.
     * </p>
     *
     * @param etk entellitrak execution context
     */
    private static void cleanOrphanedSubjects(final ExecutionContext etk) {
        getOrphanedSubjects(etk).forEach(subjectId -> deleteSubject(etk, subjectId));
    }

    /**
     * Deletes a single ETK_SUBJECT record and any of its dependencies.
     *
     * @param etk entellitrak execution context
     * @param subjectId id of the subject to be deleted
     */
    private static void deleteSubject(final ExecutionContext etk, final long subjectId) {
        deleteSubjectRoles(etk, subjectId);
        deleteSubjectPreferences(etk, subjectId);
        deleteSharedObjectPermissions(etk, subjectId);

        etk.createSQL("DELETE FROM etk_subject WHERE subject_id = :subjectId")
            .setParameter("subjectId", subjectId)
            .execute();
    }

    /**
     * Deletes all ETK_LISTING_PREFERENCE records for an ETK_SUBJECT_PREFERENCE id.
     *
     * @param etk entellitrak execution context
     * @param subjectPreferenceId subject preference id which the listing preferences belong to
     */
    private static void deleteListingPreferences(final ExecutionContext etk, final long subjectPreferenceId) {
        QueryUtility.mapsToLongs(
                etk.createSQL("SELECT listing_preference_id FROM etk_listing_preference WHERE subject_preference_id = :subjectPreferenceId")
                    .setParameter("subjectPreferenceId", subjectPreferenceId)
                    .fetchList())
            .forEach(listingPreferenceId -> deleteListingPreference(etk, listingPreferenceId));
    }

    /**
     * Deletes an ETK_LISTING_PREFERENCE record and all of its dependencies.
     *
     * @param etk entellitrak execution context
     * @param listingPreferenceId id of the listing preference to delete
     */
    private static void deleteListingPreference(final ExecutionContext etk, final long listingPreferenceId) {
        etk.createSQL("DELETE FROM etk_listing_preference WHERE listing_preference_id = :listingPreferenceId")
            .setParameter("listingPreferenceId", listingPreferenceId)
            .execute();
    }

    /**
     * Deletes all subject preferences belonging to a particular ETK_SUBJECT record.
     *
     * @param etk entellitrak execution context
     * @param subjectId subject id which the preferences belong to
     */
    private static void deleteSubjectPreferences(final ExecutionContext etk, final long subjectId) {
        QueryUtility.mapsToLongs(
                etk.createSQL("SELECT subject_preference_id FROM etk_subject_preference WHERE subject_id = :subjectId")
                    .setParameter("subjectId", subjectId)
                    .fetchList())
            .forEach(subjectPreferenceId -> deleteSubjectPreference(etk, subjectPreferenceId));
    }

    /**
     * Deletes an ETK_SUBJECT_PREFRENECE record and all of its dependencies.
     *
     * @param etk entellitrak execution context
     * @param subjectPreferenceId id of the subject preference to delete
     */
    private static void deleteSubjectPreference(final ExecutionContext etk, final long subjectPreferenceId) {
        deleteInboxPreferences(etk, subjectPreferenceId);
        deleteListingPreferences(etk, subjectPreferenceId);

        etk.createSQL("DELETE FROM etk_subject_preference WHERE subject_preference_id = :subjectPreferenceId")
            .setParameter("subjectPreferenceId", subjectPreferenceId)
            .execute();
    }

    /**
     * Deletes all shared object permissions ETK_SUBJECT record.
     *
     * @param etk entellitrak execution context
     * @param subjectId subject id which the permissions belong to
     */
    private static void deleteSharedObjectPermissions(final ExecutionContext etk, final long subjectId) {
    	QueryUtility.mapsToLongs(
    			etk.createSQL("SELECT shared_object_permission_id FROM etk_shared_object_permission WHERE subject_id = :subjectId")
    			.setParameter("subjectId", subjectId)
    			.fetchList())
    	.forEach(sharedObjectPermissionId -> deleteSharedObjectPermission(etk, sharedObjectPermissionId));
    }

    /**
     * Deletes an ETK_SHARED_OBJECT_PERMISSION record and all of its dependencies.
     *
     * @param etk entellitrak execution context
     * @param sharedObjectPermissionId the id of the shared object permission to delete
     */
    private static void deleteSharedObjectPermission(final ExecutionContext etk, final long sharedObjectPermissionId) {
    	deleteReportPermission(etk, sharedObjectPermissionId);
    	deletePagePermission(etk, sharedObjectPermissionId);

        etk.createSQL("DELETE FROM etk_shared_object_permission WHERE shared_object_permission_id = :sharedObjectPermissionId")
            .setParameter("sharedObjectPermissionId", sharedObjectPermissionId)
            .execute();
    }

    private static void deleteReportPermission(final ExecutionContext etk, final long sharedObjectPermissionId) {
    	etk.createSQL("DELETE FROM etk_report_permission WHERE report_permission_id = :sharedObjectPermissionId")
        .setParameter("sharedObjectPermissionId", sharedObjectPermissionId)
        .execute();
    }

    private static void deletePagePermission(final ExecutionContext etk, final long sharedObjectPermissionId) {
    	etk.createSQL("DELETE FROM etk_page_permission WHERE page_permission_id = :sharedObjectPermissionId")
        .setParameter("sharedObjectPermissionId", sharedObjectPermissionId)
        .execute();
    }

    /**
     * Deletes the subject roles associated with a particular subject.
     *
     * @param etk entellitrak execution context
     * @param subjectId id of the subject which the roles belong to
     */
    private static void deleteSubjectRoles(final ExecutionContext etk, final long subjectId) {
        QueryUtility.mapsToLongs(
                etk.createSQL("SELECT subject_role_id FROM etk_subject_role WHERE subject_id = :subjectId")
                    .setParameter("subjectId", subjectId)
                    .fetchList())
            .forEach(subjectRoleId -> deleteSubjectRole(etk, subjectRoleId));
    }

    /**
     * Deletes an ETK_SUBJECT_ROLE record and all of its dependencies.
     *
     * @param etk entellitrak execution context
     * @param subjectRoleId id of the subject role to be deleted
     */
    private static void deleteSubjectRole(final ExecutionContext etk, final long subjectRoleId) {
        deleteAccessLevels(etk, subjectRoleId);

        etk.createSQL("DELETE FROM etk_subject_role WHERE subject_role_id = :subjectRoleId")
            .setParameter("subjectRoleId", subjectRoleId)
            .execute();
    }

    /**
     * Deletes all of the access levels belonging to a particular subject role.
     *
     * @param etk entellitrak execution context
     * @param subjectRoleId id of the subject role which the access levels belong to.
     */
    private static void deleteAccessLevels(final ExecutionContext etk, final long subjectRoleId) {
        QueryUtility.mapsToLongs(
                etk.createSQL("SELECT access_level_id FROM etk_access_level WHERE subject_role_id = :subjectRoleId")
                    .setParameter("subjectRoleId", subjectRoleId)
                    .fetchList())
            .forEach(accessLevelId -> deleteAccessLevel(etk, accessLevelId));
    }

    /**
     * Deletes a single record from ETK_ACCESS_LEVEL.
     *
     * @param etk entellitrak execution context
     * @param accessLevelId The id of the access level to be deleted
     */
    private static void deleteAccessLevel(final ExecutionContext etk, final long accessLevelId) {
        etk.createSQL("DELETE FROM etk_access_level WHERE access_level_id = :accessLevelId")
            .setParameter("accessLevelId", accessLevelId)
            .execute();
    }

    /**
     * Deletes all inbox preferences belonging to a particular subject preference.
     *
     * @param etk entellitrak execution context
     * @param subjectPreferencId id of the subject preference
     */
    private static void deleteInboxPreferences(final ExecutionContext etk, final long subjectPreferencId) {
        QueryUtility.mapsToLongs(etk.createSQL("SELECT inbox_preference_id FROM etk_inbox_preference WHERE subject_preference_id = :subjectPreferenceId")
                .setParameter("subjectPreferenceId", subjectPreferencId)
                .fetchList())
            .forEach(inboxPreferenceId -> deleteInboxPreference(etk, inboxPreferenceId));
    }

    /**
     * Deletes a record from ETK_INBOX_PREFERENCE and all of its dependencies.
     *
     * @param etk entellitrak execution context
     * @param inboxPreferenceId id of the inbox preference
     */
    private static void deleteInboxPreference(final ExecutionContext etk, final long inboxPreferenceId) {
        deleteStateFilters(etk, inboxPreferenceId);

        etk.createSQL("DELETE FROM etk_inbox_preference WHERE inbox_preference_id = :inboxPreferenceId")
            .setParameter("inboxPreferenceId", inboxPreferenceId)
            .execute();

    }

    /**
     * Deletes the state filters for a particular inbox preference.
     *
     * @param etk entellitrak execution context
     * @param inboxPreferenceId id of the inbox preference which the state filters belong to
     */
    private static void deleteStateFilters(final ExecutionContext etk, final Long inboxPreferenceId) {
        QueryUtility.mapsToLongs(etk.createSQL("SELECT state_filter_id FROM etk_state_filter WHERE inbox_preference_id = :inboxPreferenceId")
                .setParameter("inboxPreferenceId", inboxPreferenceId)
                .fetchList())
            .forEach(stateFilterId -> deleteStateFilter(etk, stateFilterId));
    }

    /**
     * Deletes a record from ETK_STATE_FILTER.
     *
     * @param etk entellitrak execution context
     * @param stateFilterId id of the state filter
     */
    private static void deleteStateFilter(final ExecutionContext etk, final long stateFilterId) {
        etk.createSQL("DELETE FROM etk_state_filter WHERE state_filter_id = :stateFilterId")
            .setParameter("stateFilterId", stateFilterId)
            .execute();
    }

    /**
     * Gets a list of all ETK_SUBJECT records which are no longer representing valid users or groups within the system.
     *
     * @param etk entellitrak execution context
     * @return A list of the subject ids from ETK_SUBJECT
     */
    private static List<Long> getOrphanedSubjects(final ExecutionContext etk) {
        return QueryUtility.mapsToLongs(etk.createSQL("SELECT subject.subject_id FROM etk_subject subject WHERE subject.subject_id NOT IN(SELECT u.user_id FROM etk_user u UNION SELECT g.group_id FROM etk_group g)")
                .fetchList());
    }
}
