package net.entellitrak.aea.gl.api.etk;

/**
 * Enum representing the type of entellitrak objects which can be placed within bundles.
 *
 * @author Zachary.Miller
 */
public enum BundleComponentType {

    /**
     * Role.
     */
    ROLE("role", "Role"),
    /**
     * Group.
     */
    GROUP("group", "Group"),
    /**
     * Package. It is unclear whether core actually uses this.
     */
    PACKAGE("package", "Package"),
    /**
     * Script Object. Note that script objects are not actually in the ETK_BUNDLE_MAPPING table.
     */
    SCRIPT_OBJECT("scriptObject", "Script Object"),
    /**
     * Plug-in.
     */
    PLUG_IN("plugin", "Plug-in"),
    /**
     * Page.
     */
    PAGE("page", "Page"),
    /**
     * Data Object.
     */
    DATA_OBJECT("dataObject", "DataObject"),
    /**
     * System Event Listener.
     */
    SYSTEM_EVENT_LISTENER("systemEventListener", "System Event Listener"),
    /**
     * Scheduler Job.
     */
    JOB("job", "Job"),
    /**
     * Report.
     */
    REPORT("report", "Report"),
    /**
     * Lookup Definition.
     */
    LOOKUP_DEFINITION("lookupDefinition", "Lookup Definition"),
    /**
     * Query.
     */
    QUERY("query", "Query");

    private final String entellitrakKey;
    private final String displayName;

    /**
     * Simple constructor.
     *
     * @param theEntellitrakKey the entellitrak key identifier
     * @param theDisplayName the display name
     */
    BundleComponentType(final String theEntellitrakKey, final String theDisplayName) {
        entellitrakKey = theEntellitrakKey;
        displayName = theDisplayName;
    }

    /**
     * Get the key that entellitrak uses internally to identify this bundle component type.
     *
     * @return the entellitrak number
     */
    public String getEntellitrakKey() {
        return entellitrakKey;
    }

    /**
     * Get a user-friendly representation of the bundle component type.
     *
     * @return the name
     */
    public String getDisplayName() {
        return displayName;
    }
}
