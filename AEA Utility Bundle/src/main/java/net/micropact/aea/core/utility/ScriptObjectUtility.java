package net.micropact.aea.core.utility;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.configuration.ServiceBundle;
import com.entellitrak.user.User;

import net.entellitrak.aea.gl.api.etk.BundleComponentType;
import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.query.Coersion;
import net.micropact.aea.utility.ScriptObjectHandlerType;
import net.micropact.aea.utility.ScriptObjectLanguageType;
import net.micropact.aea.utility.Utility;

/**
 * Utility class for dealing with script objects.
 *
 * @author Zachary.Miller
 */
public final class ScriptObjectUtility {

    /**
     * Utility classes do not need public constructors.
     */
    private ScriptObjectUtility() {
    }

    /**
     * Create a new script object in the system repository.
     *
     * @param etk entellitrak execution context
     * @param serviceBundle the service bundle
     * @param scriptObjectLanguageType the script object language type
     * @param scriptObjectHandlerType the handler type
     * @param name the name
     * @param packageNodeId the package node id
     * @param description the description
     * @param code the code
     * @param isPublicResource whether it is a public resource
     */
    // Suppress warning about too many parameters. We need this many.
    @SuppressWarnings("java:S107")
    public static void createScriptObjectInSystemRepository(final ExecutionContext etk,
            final ServiceBundle serviceBundle,
            final ScriptObjectLanguageType scriptObjectLanguageType,
            final ScriptObjectHandlerType scriptObjectHandlerType,
            final String name,
            final Long packageNodeId,
            final String description,
            final String code,
            final boolean isPublicResource) {
        try {
            final User currentUser = etk.getCurrentUser();

            final String businessKey = generateScriptObjectBusinessKey(name);

            final Map<String, Object> parameters = Utility.arrayToMap(String.class, Object.class, new Object[][] {
                {"language_type", scriptObjectLanguageType.getId()},
                {"handler_type", scriptObjectHandlerType.getId()},
                {"workspace_id", Utility.getSystemRepositoryWorkspaceId(etk)},
                {"name", name},
                {"package_node_id", packageNodeId},
                {"business_key", businessKey},
                {"description", description},
                {"created_by", currentUser.getAccountName()},
                {"created_on", new Date()},
                {"delete_merge_required", 0},
                {"modified_locally", 0},
                {"deleted_locally", 0},
                {"created_locally", 0},
                {"revision", 1},
                {"code", code},
                {"public_resource", Coersion.toLong(isPublicResource)},

            });

            if(Utility.isOracle(etk)) {
            	etk.createSQL("INSERT INTO etk_script_object(script_id, language_type, handler_type, workspace_id, name, package_node_id, business_key, description, created_by, created_on, delete_merge_required, modified_locally, deleted_locally, created_locally, revision, code, public_resource) VALUES(HIBERNATE_SEQUENCE.NEXTVAL, :language_type, :handler_type, :workspace_id, :name, :package_node_id, :business_key, :description, :created_by, :created_on, :delete_merge_required, :modified_locally, :deleted_locally, :created_locally, :revision, :code, :public_resource)")
            	.setParameter(parameters)
            	.execute();
            } else {
            	etk.createSQL("INSERT INTO etk_script_object(language_type, handler_type, workspace_id, name, package_node_id, business_key, description, created_by, created_on, delete_merge_required, modified_locally, deleted_locally, created_locally, revision, code, public_resource) VALUES(:language_type, :handler_type, :workspace_id, :name, :package_node_id, :business_key, :description, :created_by, :created_on, :delete_merge_required, :modified_locally, :deleted_locally, :created_locally, :revision, :code, :public_resource)")
            	.setParameter(parameters)
            	.execute();
            }

            BundleUtility.insertBundleMapping(etk, serviceBundle, BundleComponentType.SCRIPT_OBJECT, businessKey);
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Generate a new script object business key.
     *
     * @param scriptObjectName the script object name
     * @return the business key
     */
    private static String generateScriptObjectBusinessKey(final String scriptObjectName) {
        return String.format("script.%s.%s",
                scriptObjectName,
                UUID.randomUUID());
    }

    /**
     * Determine whether a string looks like it would be a valid classname for a java object.
     *
     * @param className the class name
     * @return wehther the string looks like a valid class name
     */
    public static boolean looksLikeValidClassName(final String className) {
        final Pattern pattern = Pattern.compile("^([a-z\\d])+$", Pattern.CASE_INSENSITIVE);

        return pattern.matcher(className).matches();
    }

    /**
     * Get the script id for a script object in the system repository by its fully qualified name.
     *
     * @param etk entellitrak execution context
     * @param fullyQualifiedScriptName the fully qualified script name
     * @return the script id
     */
    public static long getScriptIdFromFullyQualifiedNameInSystemRepository(final ExecutionContext etk, final String fullyQualifiedScriptName) {
        try {
            return etk.createSQL("SELECT script_id FROM aea_script_pkg_view_sys_only WHERE fully_qualified_script_name = :fully_qualified_script_name")
                    .setParameter("fully_qualified_script_name", fullyQualifiedScriptName)
                    .fetchLong();
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }
}
