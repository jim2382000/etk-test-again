package net.micropact.aea.core.utility;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.user.User;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.query.Coersion;
import net.micropact.aea.core.query.DatabaseSequence;
import net.micropact.aea.utility.PackageType;
import net.micropact.aea.utility.Utility;

/**
 * This class contains methods dealing with entellitrak packages.
 *
 * @author Zachary.Miller
 */
public final class PackageUtility {

    /**
     * Utility classes do not need public constructors.
     */
    private PackageUtility() {
    }

    /**
     * Get a list of all the packages, including the PACKAGE_NODE_ID, NAME, PARENT_NODE_ID, path.
     *
     * @param etk entellitrak execution context
     * @return the packages
     */
    public static List<Map<String, Object>> getPackages(final ExecutionContext etk) {
        try {
            final List<Map<String, Object>> packages = etk.createSQL("SELECT PACKAGE_NODE_ID, NAME, PARENT_NODE_ID FROM etk_package_node WHERE workspace_id = :workspaceId")
            .setParameter("workspaceId", Utility.getSystemRepositoryWorkspaceId(etk))
            .fetchList();

            /* Index the packages by PACKAGE_NODE_ID.
             * This is needed because we'll be doing a lot of looking up information about parent packages. */
            final Map<Long, Map<String, Object>> packagesByNodeId = packages.stream()
                    .collect(Collectors.toMap(thePackage -> Coersion.toLong(thePackage.get("PACKAGE_NODE_ID")),
                            Function.identity()));

            /* Add the path attribute */
            packagesByNodeId
            .keySet()
            .forEach(packageId -> ensurePath(packagesByNodeId, packageId));

            packages.sort(Comparator.comparing(thePackage -> (String) thePackage.get("path")));

            return packages;
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Get the combined path of two potentially empty packages.
     *
     * @param parentPackage the parent package
     * @param subPackage the sub package
     * @return the combined path
     */
    public static String combinePackagePaths(final String parentPackage, final String subPackage) {
        final String path;
        if(!Objects.equals("", parentPackage) && !Objects.equals("", subPackage)) {
            path = String.format("%s.%s", parentPackage, subPackage);
        } else {
            path = String.format("%s%s", parentPackage, subPackage);
        }
        return path;
    }

    /**
     * Ensure that a package with a given path exists in the system repository.
     * If it does not exist, this method will create it.
     * This method will not refresh the entellitrak workspace cache.
     *
     * @param etk entellitrak execution context
     * @param path the path
     */
    public static void ensurePackageExistsInSystemRepository(final ExecutionContext etk, final String path) {
        if(!Objects.equals("", path)) {
            final List<String> segments = new LinkedList<>(Arrays.asList(path.split("\\.")));

            ensurePackageExistsBelowParent(etk, null, segments);
        }
    }

    /**
     * Generate a package business key.
     *
     * @param packageName the package name
     * @return the business key
     */
    private static String generatePackageBusinessKey(final String packageName) {
        return String.format("package.%s.%s",
                packageName,
                UUID.randomUUID());
    }

    /**
     * Find the package id corresponding to a particular path (in the system repository).
     * The path should exist.
     *
     * @param etk entellitrak execution context
     * @param path the non-null path
     * @return the package id
     */
    public static Long getPackageIdByPathInSystemRepository(final ExecutionContext etk, final String path) {
        try {
            Long packageId = null;

            final List<String> segments = Arrays.asList(path.split("\\."));

            for (final String segment : segments) {
                packageId = etk.createSQL(
                		Utility.isPostgreSQL(etk) ? "SELECT packageNode.package_node_id FROM etk_package_node packageNode JOIN etk_workspace workspace ON workspace.workspace_id = packageNode.workspace_id WHERE workspace.workspace_name = 'system' AND ( packageNode.parent_node_id IS NULL AND :parent_node_id::bigint IS NULL OR packageNode.parent_node_id = :parent_node_id::bigint ) AND packageNode.name = :name"
                				: "SELECT packageNode.package_node_id FROM etk_package_node packageNode JOIN etk_workspace workspace ON workspace.workspace_id = packageNode.workspace_id WHERE workspace.workspace_name = 'system' AND ( packageNode.parent_node_id IS NULL AND :parent_node_id IS NULL OR packageNode.parent_node_id = :parent_node_id ) AND packageNode.name = :name")
                        .setParameter("parent_node_id", packageId)
                        .setParameter("name", segment)
                        .fetchLong();
            }

            return packageId;
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Ensure that a package path exists underneath a particular parent node.
     * If the packages do not exist, they will be created.
     *
     * @param etk entellitrak execution context
     * @param parentNodeId the parent node id
     * @param packageSegments the remaining package names
     */
    private static void ensurePackageExistsBelowParent(final ExecutionContext etk, final Long parentNodeId, final List<String> packageSegments) {
        final User currentUser = etk.getCurrentUser();

        try {
            if(!packageSegments.isEmpty()) {
                final String firstSegment = packageSegments.get(0);

                final Long existingFirstSegmentNodeId = etk.createSQL(
                		Utility.isPostgreSQL(etk) ? "SELECT packageNode.package_node_id FROM etk_package_node packageNode JOIN etk_workspace workspace ON workspace.workspace_id = packageNode.workspace_id WHERE workspace.workspace_name = 'system' AND (packageNode.parent_node_id IS NULL AND :parent_node_id::bigint IS NULL OR packageNode.parent_node_id = :parent_node_id::bigint ) AND packageNode.name = :name"
                				: "SELECT packageNode.package_node_id FROM etk_package_node packageNode JOIN etk_workspace workspace ON workspace.workspace_id = packageNode.workspace_id WHERE workspace.workspace_name = 'system' AND (packageNode.parent_node_id IS NULL AND :parent_node_id IS NULL OR packageNode.parent_node_id = :parent_node_id ) AND packageNode.name = :name")
                        .setParameter("parent_node_id", parentNodeId)
                        .setParameter("name", firstSegment)
                        .returnEmptyResultSetAs(null)
                        .fetchLong();

                final long firstSegmentNodeId;

                if(existingFirstSegmentNodeId != null) {
                    firstSegmentNodeId = existingFirstSegmentNodeId;
                } else {
                    final Map<String, Object> insertQueryParameters = Utility.arrayToMap(String.class, Object.class, new Object[][] {
                        {"business_key", generatePackageBusinessKey(firstSegment)},
                        {"workspace_id", Utility.getSystemRepositoryWorkspaceId(etk)},
                        {"name", firstSegment},
                        {"parent_node_id", parentNodeId},
                        {"created_by", currentUser.getAccountName()},
                        {"created_on", new Date()},
                        {"created_locally", 0},
                        {"modified_locally", 0},
                        {"deleted_locally", 0},
                        {"delete_merge_required", 0},
                        {"revision", 1},
                        {"package_type", PackageType.STANDARD.getPackageTypeNumber()},
                    });

                    if(Utility.isOracle(etk)) {
                    	firstSegmentNodeId = DatabaseSequence.HIBERNATE_SEQUENCE.getNextVal(etk);

                    	etk.createSQL("INSERT INTO etk_package_node(package_node_id, business_key, workspace_id, name, parent_node_id, created_by, created_on, created_locally, modified_locally, deleted_locally, delete_merge_required, revision, package_type) VALUES(:package_node_id, :business_key, :workspace_id, :name, :parent_node_id, :created_by, :created_on, :created_locally, :modified_locally, :deleted_locally, :delete_merge_required, :revision, :package_type)")
                    	.setParameter(insertQueryParameters)
                    	.setParameter("package_node_id", firstSegmentNodeId)
                    	.execute();
                    } else {
                    	firstSegmentNodeId = etk.createSQL("INSERT INTO etk_package_node(business_key, workspace_id, name, parent_node_id, created_by, created_on, created_locally, modified_locally, deleted_locally, delete_merge_required, revision, package_type) VALUES(:business_key, :workspace_id, :name, :parent_node_id, :created_by, :created_on, :created_locally, :modified_locally, :deleted_locally, :delete_merge_required, :revision, :package_type)")
                    			.setParameter(insertQueryParameters)
                    			.execute("package_node_id");
                    }
                }

                packageSegments.remove(0);

                ensurePackageExistsBelowParent(etk, firstSegmentNodeId, packageSegments);
            }
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Get the full path of a package given its id.
     *
     * @param etk entellitrak execution context
     * @param packageId the package id
     * @return the non-null package path
     */
    public static String getPackagePathById(final ExecutionContext etk, final Long packageId) {
        try {
            if(packageId == null) {
                return "";
            } else {
                final Map<String, Object> packageInfo = etk.createSQL("SELECT PARENT_NODE_ID, NAME FROM etk_package_node WHERE package_node_id = :package_node_id")
                        .setParameter("package_node_id", packageId)
                        .fetchMap();

                final Long parentNodeId = Coersion.toLong(packageInfo.get("PARENT_NODE_ID"));
                final String name = (String) packageInfo.get("NAME");

                if(parentNodeId == null) {
                    return name;
                } else {
                    return String.format("%s.%s",
                            getPackagePathById(etk, parentNodeId),
                            name);
                }
            }
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Determine whether a string looks like a valid java package path.
     *
     * @param packagePath the package path
     * @return whether it looks like a valid package
     */
    public static boolean looksLikeValidJavaPackageFormat(final String packagePath) {
        final Pattern pattern = Pattern.compile("^([a-z\\d]+)(\\.[a-z\\d]+)*+$", Pattern.CASE_INSENSITIVE);

        return pattern.matcher(packagePath).matches();
    }

    /**
     * Helper method for {@link getPackages}.
     * Ensures that the path exists for a particular package.
     * If it does not exist, it will add it (and as a side-effect add it for
     * all its parents).
     *
     * @param packagesByNodeId the packages by node id
     * @param thePackageId the package id
     */
    private static void ensurePath(final Map<Long, Map<String, Object>> packagesByNodeId, final Long thePackageId) {
        final Map<String, Object> thePackage = packagesByNodeId.get(thePackageId);

        if(!thePackage.containsKey("path")) {
            /* path wasn't found. We must add it. */

            final Long parentNodeId = Coersion.toLong(thePackage.get("PARENT_NODE_ID"));

            final String path;
            final String name = (String) thePackage.get("NAME");

            if(parentNodeId == null) {
                /* We have no parent, our path is just our name */
                path = name;
            } else {
                /* We have no parent. We ensure the parent's path exists.
                 * We'll let ensure-path handle short-circuiting if the parent's path does already exist.
                 * After that, calculating our path is easy. */
                ensurePath(packagesByNodeId, parentNodeId);

                final Map<String, Object> parentPackage = packagesByNodeId.get(parentNodeId);
                final String parentPath = (String) parentPackage.get("path");

                path = String.format("%s.%s", parentPath, name);
            }

            thePackage.put("path", path);
        }
    }
}
