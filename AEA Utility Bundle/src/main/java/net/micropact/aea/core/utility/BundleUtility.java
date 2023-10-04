package net.micropact.aea.core.utility;

import java.util.Objects;

import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.configuration.ServiceBundle;

import net.entellitrak.aea.gl.api.etk.BundleComponentType;
import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.utility.Utility;

/**
 * Utility methods for dealing with bundles.
 *
 * @author Zachary.Miller
 */
public final class BundleUtility {

    /**
     * Utility classes do not need public constructors.
     */
    private BundleUtility() {
    }

    /**
     * Get the id for a service bundle.
     *
     * @param etk
     *            entellitrak execution context
     * @param serviceBundle
     *            the service bundle
     * @return the bundle id
     */
    public static long getBundleId(final ExecutionContext etk, final ServiceBundle serviceBundle) {
        try {
            return etk.createSQL("SELECT bundle_id FROM etk_bundle WHERE business_key = :business_key")
                .setParameter("business_key", serviceBundle.getBusinessKey())
                .fetchLong();
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Insert a record into the ETK_BUNDLE_MAPPING or ETK_BUNDLE_SCRIPT_OBJECT table for a particular component.
     * <strong>Does not check whether a mapping already exists.</strong> <strong>If the component is a script objet, a
     * mapping will only be inserted for the system workspace.</strong>
     *
     * @param etk
     *            entellitrak execution context
     * @param serviceBundle
     *            the service bundle
     * @param bundleComponentType
     *            the bundle component type
     * @param componentBusinessKey
     *            the component business key
     */
    public static void insertBundleMapping(final ExecutionContext etk, final ServiceBundle serviceBundle,
        final BundleComponentType bundleComponentType, final String componentBusinessKey) {
        final long bundleId = getBundleId(etk, serviceBundle);

        if (Objects.equals(bundleComponentType, BundleComponentType.SCRIPT_OBJECT)) {
            insertBundleScriptObject(etk, bundleId, componentBusinessKey);
        } else {
            etk.createSQL(Utility.isSqlServer(etk) || Utility.isPostgreSQL(etk)
                ? "INSERT INTO etk_bundle_mapping(component_type, component_business_key, bundle_id) VALUES (:component_type, :component_business_key, :bundle_id)"
                : "INSERT INTO etk_bundle_mapping(bundle_mapping_id, component_type, component_business_key, bundle_id) VALUES (HIBERNATE_SEQUENCE.NEXTVAL, :component_type, :component_business_key, :bundle_id)")
                .setParameter("component_type", bundleComponentType.getEntellitrakKey())
                .setParameter("bundle_id", bundleId)
                .setParameter("component_business_key", componentBusinessKey)
                .execute();
        }
    }

    /**
     * Insert a record into ETK_BUNDLE_SCRIPT_OBJECT. <strong>Only inserts a record for the system workspace.</strong>
     *
     * @param etk
     *            entellitrak execution context
     * @param bundleId
     *            the bundle id
     * @param scriptObjectBusinessKey
     *            the script object business key
     */
    private static void insertBundleScriptObject(final ExecutionContext etk, final long bundleId,
        final String scriptObjectBusinessKey) {
        try {
            etk.createSQL(Utility.isSqlServer(etk) || Utility.isPostgreSQL(etk)
                ? "INSERT INTO etk_bundle_script_object (workspace_id, business_key, bundle_id) VALUES (:workspace_id, :business_key, :bundle_id)"
                : "INSERT INTO etk_bundle_script_object (bundle_mapping_id, workspace_id, business_key, bundle_id) VALUES (HIBERNATE_SEQUENCE.NEXTVAL, :workspace_id, :business_key, :bundle_id)")
                .setParameter("workspace_id", Utility.getSystemRepositoryWorkspaceId(etk))
                .setParameter("business_key", scriptObjectBusinessKey)
                .setParameter("bundle_id", bundleId)
                .execute();
        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new GeneralRuntimeException(e);
        }
    }
}
