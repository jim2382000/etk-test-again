package net.micropact.aea.du.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.OrganizationInfo;
import com.entellitrak.OrganizationTree;

import net.entellitrak.aea.du.service.IOrganizationalHierarchyMigrationService;
import net.entellitrak.aea.du.service.organizationalHierarchy.IOrganizationalHierarchyImportResult;
import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.entellitrak.aea.gl.api.java.io.noisyinputstream.INoisyInputStream;
import net.entellitrak.aea.gl.api.java.io.noisyinputstream.INoisyInputStream.IWrapRequest;
import net.micropact.aea.core.gson.GsonUtility;
import net.micropact.aea.core.query.Coersion;
import net.micropact.aea.core.query.DatabaseSequence;
import net.micropact.aea.du.utility.orgUnit.OrganizationalHierarchyImportResult;
import net.micropact.aea.utility.Utility;

/**
 * This class is the private implementation of the {@link IOrganizationalHierarchyMigrationService} interface in the
 * public API.
 *
 * @author zachary.miller
 */
public class OrganizationalHierarchyMigrationService implements IOrganizationalHierarchyMigrationService {

    private final ExecutionContext etk;

    /**
     * Simple constructor.
     *
     * @param executionContext
     *            entellitrak execution context
     */
    public OrganizationalHierarchyMigrationService(final ExecutionContext executionContext) {
        etk = executionContext;
    }

    /**
     * Ensures that the hierarchy in the database has unique code values. entellitrak does not enforce this, but if they
     * want to use the export the codes should be unique.
     *
     * <p>
     * Throws an exception if there are duplicate codes.
     * </p>
     *
     * @param etk
     *            entellitrak execution context
     */
    private static void ensureUniqueHierarchyCodes(final ExecutionContext etk) {
        final OrganizationTree organizationTree = etk.getOrganizationTree();

        final List<Entry<String, List<OrganizationInfo>>> nonUniqueCodes = organizationTree
            .getOrgTree()
            .values()
            .stream()
            .collect(Collectors.groupingBy(OrganizationInfo::getCode))
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(Collectors.toList());

        if (!nonUniqueCodes.isEmpty()) {
            final Collection<String> entryDescriptions = nonUniqueCodes
                .stream()
                .map(entry -> String.format("Code: \"%s\", Count \"%s\";",
                    entry.getKey(),
                    entry.getValue().size()))
                .collect(Collectors.toList());

            final String entriesDescriptions = String.join("; ", entryDescriptions);

            throw new GeneralRuntimeException(String.format("Found organizational units with duplicate codes: %s",
                entriesDescriptions));
        }

    }

    @Override
    public InputStream exportToStream() throws Exception {
        ensureUniqueHierarchyCodes(etk);

        /* The ORDER BY clause is currently important to the importer. */
        final List<EtkHierarchyDTO> hierarchies = etk
            .createSQL("SELECT NODE_ID, PARENT_ID, MAX_CHILD_ID, CODE, NAME FROM etk_hierarchy")
            .fetchList()
            .stream()
            .map(queryResult -> new EtkHierarchyDTO(
                Coersion.toLong(queryResult.get("NODE_ID")),
                Coersion.toLong(queryResult.get("PARENT_ID")),
                Coersion.toLong(queryResult.get("MAX_CHILD_ID")),
                (String) queryResult.get("NAME"),
                (String) queryResult.get("CODE")))
            .sorted(Comparator.comparing(EtkHierarchyDTO::getNodeId))
            .collect(Collectors.toList());

        return INoisyInputStream.wrap(IWrapRequest.builder(etk)
        		.setInputStream(new ByteArrayInputStream(GsonUtility.getStandardPrettyPrintingGson()
        	            .toJson(hierarchies)
        	            .getBytes(StandardCharsets.UTF_8)))
        		.build());
    }

    @Override
    public IOrganizationalHierarchyImportResult importFromStream(final InputStream orgHierarchyStream)
        throws Exception {
        ensureUniqueHierarchyCodes(etk);

        return OrgUnitImportLogic.performImport(etk, orgHierarchyStream);
    }

    /**
     * Holds the import logic for importing Organizational Units.
     *
     * @author Zachary.Miller
     */
    private static final class OrgUnitImportLogic {

        /**
         * Utility classes do not need public constructors.
         */
        private OrgUnitImportLogic() {
        }

        /**
         * Import an input stream containing organizational unit data.
         *
         * @param etk
         *            entellitrak execution context
         * @param inputStream
         *            input stream containing the data to import
         * @return the code of the nodes which existed in the destination site but not in the source site
         */
        public static IOrganizationalHierarchyImportResult performImport(final ExecutionContext etk,
            final InputStream inputStream) {
            try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                final List<EtkHierarchyDTO> hierarchies = Arrays.asList(GsonUtility.getStandardPrettyPrintingGson()
                    .fromJson(reader, EtkHierarchyDTO[].class));

                return new OrganizationalHierarchyImportResult(importOrgUnits(etk, hierarchies));
            } catch (final IOException e) {
                throw new GeneralRuntimeException(e);
            }
        }

        /**
         * This method is the entry point for the main algorithm.
         *
         * <h3>Constraints</h3>
         * <ul>
         * <li>Code will be the unique identifier. If a node with a given code already exists in the destination site it
         * will be updated. This means that Users/Objects referencing it by hierarchy_id will not get their links
         * broken.</li>
         * <li>No nodes will be deleted. Sites will have to delete nodes after the import is complete. If there are
         * nodes in the destination system which were not in the import then those nodes will be flattened out and be
         * made children of the new root (after the imported nodes).</li>
         * <li>An attempt will be made to preserve order of nodes of the import file, and nodes which get reparented to
         * the root.</li>
         * </ul>
         *
         * <h3>Assumptions</h3>
         * <ul>
         * <li>The nodes in the document are sorted by node id</li>
         * <li>The root node is 0</li>
         * <li>Node ids are non-negative</li>
         * </ul>
         *
         * <h3>Algorithm</h3>
         * <ol>
         * <li>"Orphan" all nodes by negating their node_id. Note that using negation will allow a later step of the
         * algorithm to know the original order of nodes which will eventually be reparented to the root.</li>
         * <li>Import nodes from the document by either inserting or updating the existing node with a matching code.
         * The node_id, parent_id, max_child_id can all be taken directly from the document.</li>
         * <li>Any nodes which still have a negative node_id were not in the import document. These nodes will be placed
         * under the new root node.</li>
         * <li>Update the max_child_id of the root node now that new children may have been added to it.</li>
         * </ol>
         *
         * @param etk
         *            entellitrak execution context
         * @param hierarchyNodes
         *            the hierarchy nodes to import
         * @return The code of the nodes which existed in the destination site, but not in the source site
         */
        private static Collection<OrganizationInfo> importOrgUnits(final ExecutionContext etk,
            final List<EtkHierarchyDTO> hierarchyNodes) {
            final OrganizationTree organizationTree = etk.getOrganizationTree();

            orphanExistingNodes(etk);
            importDocumentNodes(etk, hierarchyNodes);
            final Set<String> orphanedNodeCodes = reparentOrphanNodes(etk);
            updateRoot(etk);
            return orphanedNodeCodes
                .stream()
                .map(nodeCode -> organizationTree.getOrganizationInfoByHierarchyCode(nodeCode).get(0))
                .collect(Collectors.toList());
        }

        /**
         * This will update the max_child_id of the root node. This is necessary because orphans could have been added
         * to it.
         *
         * @param etk
         *            entellitrak execution context
         */
        private static void updateRoot(final ExecutionContext etk) {
            etk.createSQL(
                "UPDATE etk_hierarchy SET max_child_id = (SELECT MAX(node_id) FROM etk_hierarchy) WHERE node_id = 0")
                .execute();
        }

        /**
         * This will find all nodes which were orphaned, but did not exist in the import document. These are the nodes
         * which still have negative node ids. Their new parent will be the root node.
         *
         * @param etk
         *            entellitrak execution context
         * @return The hierarchy nodes which were orphaned
         */
        private static Set<String> reparentOrphanNodes(final ExecutionContext etk) {
            final List<Map<String, Object>> orphanedHierarchies = etk
                .createSQL("SELECT HIERARCHY_ID, NAME, CODE FROM etk_hierarchy WHERE node_id < 0 ORDER BY node_id DESC")
                .fetchList();

            orphanedHierarchies.forEach(hierarchy -> {
                try {
                    final long newNodeId = 1 + etk.createSQL("SELECT MAX(node_id) FROM etk_hierarchy")
                        .fetchLong();

                    etk.createSQL(
                        "UPDATE etk_hierarchy SET node_id = :nodeId, parent_id = 0, max_child_id = :nodeId WHERE hierarchy_id = :hierarchyId")
                        .setParameter("hierarchyId", hierarchy.get("HIERARCHY_ID"))
                        .setParameter("nodeId", newNodeId)
                        .execute();
                } catch (final IncorrectResultSizeDataAccessException e) {
                    throw new GeneralRuntimeException(e);
                }
            });

            return orphanedHierarchies
                .stream()
                .map(node -> (String) node.get("CODE"))
                .collect(Collectors.toSet());
        }

        /**
         * Imports the nodes into to the new system. If a node with the same code is found in this system, that node
         * will be updated (thus maintaining its hierarchy id).
         *
         * @param etk
         *            entellitrak execution context
         * @param hierarchyNodes
         *            the hierarchy nodes to import
         */
        private static void importDocumentNodes(final ExecutionContext etk,
            final List<EtkHierarchyDTO> hierarchyNodes) {
            hierarchyNodes.forEach(hierarchyNode -> {
                try {
                    final String code = hierarchyNode.getCode();

                    final Long existingHierarchyId = etk
                        .createSQL("SELECT hierarchy_id FROM etk_hierarchy WHERE code = :code")
                        .setParameter("code", code)
                        .returnEmptyResultSetAs(null)
                        .fetchLong();

                    final long newHierarchyId;
                    final Map<String, Object> newParams = Utility.arrayToMap(String.class, Object.class,
                        new Object[][] {
                            { "CODE", hierarchyNode.getCode() },
                            { "MAX_CHILD_ID", hierarchyNode.getMaxChildId() },
                            { "NAME", hierarchyNode.getName() },
                            { "NODE_ID", hierarchyNode.getNodeId() },
                            { "PARENT_ID", hierarchyNode.getParentId() },
                        });

                    if (existingHierarchyId == null) {
                        /* Insert into the ETK_HIERARCHY_ROOT table */
                        if (Utility.isSqlServer(etk)) {
                            newHierarchyId = etk.createSQL("INSERT INTO etk_hierarchy_root DEFAULT VALUES")
                                .execute("HIERARCHY_ID");
                        } else if (Utility.isPostgreSQL(etk)) {
                            newHierarchyId = etk
                                .createSQL("INSERT INTO etk_hierarchy_root DEFAULT VALUES returning HIERARCHY_ID")
                                .execute("HIERARCHY_ID");
                        } else {
                            newHierarchyId = DatabaseSequence.HIBERNATE_SEQUENCE.getNextVal(etk);
                            etk.createSQL("INSERT INTO etk_hierarchy_root(hierarchy_id) VALUES(:hierarchy_id)")
                                .setParameter("hierarchy_id", newHierarchyId)
                                .execute();
                        }

                        /* Insert into ETK_HIERARCHY */
                        newParams.put("HIERARCHY_ID", newHierarchyId);

                        etk.createSQL(
                            "INSERT INTO etk_hierarchy(hierarchy_id, node_id, parent_id, max_child_id, name, code) VALUES(:HIERARCHY_ID, :NODE_ID, :PARENT_ID, :MAX_CHILD_ID, :NAME, :CODE)")
                            .setParameter(newParams)
                            .execute();
                    } else {
                        /* Update the existing ETK_HIERARCHY record */
                        newHierarchyId = existingHierarchyId;

                        newParams.put("HIERARCHY_ID", newHierarchyId);

                        etk.createSQL(
                            "UPDATE etk_hierarchy SET node_id = :NODE_ID, parent_id = :PARENT_ID, max_child_id = :MAX_CHILD_ID, name = :NAME, code = :CODE WHERE hierarchy_id = :HIERARCHY_ID")
                            .setParameter(newParams)
                            .execute();
                    }
                } catch (final IncorrectResultSizeDataAccessException e) {
                    throw new GeneralRuntimeException(e);
                }
            });
        }

        /**
         * "Orphans" all existing nodes in the system. Each node is given a new node id: (-1 * (node_id + 1)). This
         * makes all the node ids negative and preserves (although reverses) the relative node id order.
         *
         * @param etk
         *            entellitrak execution context
         */
        private static void orphanExistingNodes(final ExecutionContext etk) {
            etk.createSQL(
                "UPDATE etk_hierarchy SET node_id = -(node_id + 1), parent_id = -(node_id + 1), max_child_id = -(node_id + 1)")
                .execute();
        }
    }

    /**
     * Data Transfer Object for ETK_HIERARCHY.
     *
     * @author Zachary.Miller
     */
    static class EtkHierarchyDTO {

        private final long nodeId;
        private final long parentId;
        private final long maxChildId;
        private final String name;
        private final String code;

        /**
         * Simple constructor.
         *
         * @param theNodeId
         *            the node id
         * @param theParentId
         *            the parent id
         * @param theMaxChildId
         *            the max child id
         * @param theName
         *            the name
         * @param theCode
         *            the code
         */
        EtkHierarchyDTO(final long theNodeId, final long theParentId, final long theMaxChildId, final String theName,
            final String theCode) {
            nodeId = theNodeId;
            parentId = theParentId;
            maxChildId = theMaxChildId;
            name = theName;
            code = theCode;
        }

        public long getNodeId() {
            return nodeId;
        }

        public long getParentId() {
            return parentId;
        }

        public long getMaxChildId() {
            return maxChildId;
        }

        public String getName() {
            return name;
        }

        public String getCode() {
            return code;
        }
    }
}
