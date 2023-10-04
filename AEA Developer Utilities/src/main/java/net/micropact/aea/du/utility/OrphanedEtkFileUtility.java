package net.micropact.aea.du.utility;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ExecutionContext;
import com.entellitrak.WorkExecutionException;
import com.entellitrak.configuration.DataObjectService;

import net.micropact.aea.core.query.QueryUtility;
import net.micropact.aea.du.model.OrphanedFile;

/**
 * Contains utility functionality for dealing with orphaned etk_file records. It can provide a summary of orphaned files
 * as well as delete those files. TODO: When core fixes
 * etk.getDocumentManagementServiceFactory().getFileService().delete(fileId) this file needs to be updated to delete
 * Document Management files.
 *
 * @author Zachary.Miller
 */
public final class OrphanedEtkFileUtility {

    /**
     * Utility classes do not need public constructors.
     */
    private OrphanedEtkFileUtility() {
    }

    /**
     * Clean the orphaned files in the etk_file table.
     *
     * @param etk
     *            entellitrak execution context
     * @return a summary of the cleaned files
     */
    public static List<OrphanedFile> cleanOrphanedFiles(final ExecutionContext etk) {
        final List<OrphanedFile> orphanedFiles = findOrphanedFiles(etk);

        orphanedFiles.forEach(orphanedFile -> {
        	try {
				etk.doWork(etk2 -> FileUtility.deleteFile(etk2, orphanedFile.getFileId()));
			} catch (final WorkExecutionException e) {
				etk.getLogger().error(String.format("Problem deleting file %s", orphanedFile), e);
			}
        });

        return orphanedFiles;
    }

    /**
     * Finds orphaned files within entellitrak.
     *
     * @param etk
     *            entellitrak execution context
     * @return the list of orphaned file ids
     */
    public static List<OrphanedFile> findOrphanedFiles(final ExecutionContext etk) {
        // Get a list of all objectTypes (tableNames) in etk_file.
        final List<Map<String, Object>> objectTypeInfos = etk
            .createSQL("SELECT DISTINCT OBJECT_TYPE \"OBJECT_TYPE\" FROM etk_file ORDER BY object_type")
            .fetchList();

        return objectTypeInfos.stream()
            .flatMap(objectTypeInfo -> {
                final Stream<Long> returnValue;

                final String objectType = (String) objectTypeInfo.get("OBJECT_TYPE");

                // Check if the table actually exists in entellitrak
                if (doesTableExistInTrackingConfig(etk, objectType)) {
                    // The table exists so delete all files which are in etk_file but do not have a matching reference id
                	// We do not delete items with a null reference_id. core should be deleting these as scanned files.
                    returnValue = QueryUtility.mapsToLongs(etk.createSQL(
                        "SELECT f.id FROM etk_file f WHERE f.object_type = :objectType AND reference_id IS NOT NULL AND NOT EXISTS (SELECT * FROM " + objectType + " obj WHERE obj.id = f.reference_id) ORDER BY f.id")
                        .setParameter("objectType", objectType)
                        .fetchList()).stream();
                } else {
                    // The table does not exist so delete all files with that object type
                    returnValue = QueryUtility.mapsToLongs(etk.createSQL(
                        "SELECT f.id FROM etk_file f WHERE f.object_type = :objectType ORDER BY f.id")
                        .setParameter("objectType", objectType)
                        .fetchList())
                        .stream();
                }

                return returnValue
                    .flatMap(fileId -> Stream.of(new OrphanedFile(fileId, objectType)));
            })
            .collect(Collectors.toList());
    }

    /**
     * Determine whether a table exists as a table for a data object in the tracking configuration.
     *
     * @param etk
     *            entellitrak execution context
     * @param tableName
     *            the table name
     * @return whether the table exists
     */
    private static boolean doesTableExistInTrackingConfig(final ExecutionContext etk, final String tableName) {
        final DataObjectService dataObjectService = etk.getDataObjectService();
        return dataObjectService.getDataObjects()
            .stream()
            .anyMatch(dataObject -> dataObject.getTableName().equals(tableName));
    }
}
