package net.micropact.aea.du.page.createStandardPage;

import java.util.Objects;

import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.LanguageType;
import com.entellitrak.configuration.ServiceBundleService;
import com.entellitrak.page.Parameters;

import net.entellitrak.aea.core.page.defaultControllers.DefaultHtmlController;

/**
 * Trasnfer object for dealing with the many page paraemters for the {@link CreateStandardPageController} tool.
 *
 * @author Zachary.Miller
 */
final class CreateStandardPageParametersTO {

    private final boolean isUpdate;
    private final String selectedServiceBundleKey;
    private final String name;
    private final boolean isPublic;
    private final boolean includeHeaderFooter;
    private final boolean allUsersExecute;
    private final String parentPackage;
    private final String subPackage;
    private final boolean makeNewControllerScript;
    private final String selectedControllerScript;
    private final boolean makeNewViewScript;
    private final String selectedViewScript;
    private final String selectedViewScriptLanguageType;
    private final String newScriptNamePrefix;

    /**
     * Simple constructor.
     *
     * @param theIsUpdate whether the user is requesting to make a new page (as opposed to opening the page for the first time)
     * @param theSelectedServiceBundleKey the selected service bundle key
     * @param theName the page name
     * @param theIsPublic whether the page should be public
     * @param theIncludeHeaderFooter whether the page should include the header/footer
     * @param theAllUsersExecute whether the page should give all users execute permission
     * @param theParentPackage the parent package
     * @param theSubPackage the sub package
     * @param theMakeNewControllerScript whether to make a new controller script
     * @param theSelectedControllerScript the pre-existing controller script to use
     * @param theMakeNewViewScript whether to make a new view script
     * @param theSelectedViewScript the pre-existing view script to use
     * @param theSelectedViewScriptLanguageType the selected view script language type for a new script
     * @param theNewScriptNamePrefix new script name prefix
     */
    // Suppress warning about too many parameters. We do this to be immutable.
    @SuppressWarnings("java:S107")
    private CreateStandardPageParametersTO(
            final boolean theIsUpdate,
            final String theSelectedServiceBundleKey,
            final String theName,
            final boolean theIsPublic,
            final boolean theIncludeHeaderFooter,
            final boolean theAllUsersExecute,
            final String theParentPackage,
            final String theSubPackage,
            final boolean theMakeNewControllerScript,
            final String theSelectedControllerScript,
            final boolean theMakeNewViewScript,
            final String theSelectedViewScript,
            final String theSelectedViewScriptLanguageType,
            final String theNewScriptNamePrefix) {
        isUpdate = theIsUpdate;
        selectedServiceBundleKey = theSelectedServiceBundleKey;
        name = theName;
        isPublic = theIsPublic;
        includeHeaderFooter = theIncludeHeaderFooter;
        allUsersExecute = theAllUsersExecute;
        parentPackage = theParentPackage;
        subPackage = theSubPackage;
        makeNewControllerScript = theMakeNewControllerScript;
        selectedControllerScript = theSelectedControllerScript;
        makeNewViewScript = theMakeNewViewScript;
        selectedViewScript = theSelectedViewScript;
        selectedViewScriptLanguageType = theSelectedViewScriptLanguageType;
        newScriptNamePrefix = theNewScriptNamePrefix;
    }

    /**
     * Get whether this is a form submission.
     *
     * @return is update
     */
    public boolean isUpdate() {
        return isUpdate;
    }

    /**
     * Get the selected service bundle key.
     *
     * @return the selected service bundle key
     */
    public String getSelectedServiceBundleKey() {
        return selectedServiceBundleKey;
    }

    /**
     * Get the page name.
     *
     * @return the page name
     */
    public String getName() {
        return name;
    }

    /**
     * Get whether the page is public.
     *
     * @return is public
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Get whether to include the header/footer.
     *
     * @return is include header footer
     */
    public boolean isIncludeHeaderFooter() {
        return includeHeaderFooter;
    }

    /**
     * Get whether all users should have execute permission.
     *
     * @return is all users execute
     */
    public boolean isAllUsersExecute() {
        return allUsersExecute;
    }

    /**
     * Get the parent package.
     *
     * @return the parent package
     */
    public String getParentPackage() {
        return parentPackage;
    }

    /**
     * Get the sub package.
     *
     * @return the sub package
     */
    public String getSubPackage() {
        return subPackage;
    }

    /**
     * Get whether to make a new controller script.
     *
     * @return is make new controller script
     */
    public boolean isMakeNewControllerScript() {
        return makeNewControllerScript;
    }

    /**
     * Get the selected controller script.
     *
     * @return the selected controller script
     */
    public String getSelectedControllerScript() {
        return selectedControllerScript;
    }

    /**
     * Get whether to make a new view script.
     *
     * @return is make new view script
     */
    public boolean isMakeNewViewScript() {
        return makeNewViewScript;
    }

    /**
     * Get the selected view script.
     *
     * @return the selected view script
     */
    public String getSelectedViewScript() {
        return selectedViewScript;
    }

    /**
     * Get the selected view script language type.
     *
     * @return the selected view script language type
     */
    public String getSelectedViewScriptLanguageType() {
        return selectedViewScriptLanguageType;
    }

    /**
     * Get the new script name prefix.
     *
     * @return the new script name prefix
     */
    public String getNewScriptNamePrefix() {
        return newScriptNamePrefix;
    }

    /**
     * Get the default parameters for the {@link CreateStandardPageController} when the page is initially run.
     *
     * @param etk entellitrak execution context
     * @return the default parameters
     */
    public static CreateStandardPageParametersTO getDefaultParameters(final ExecutionContext etk) {
        final ServiceBundleService serviceBundleService = etk.getServiceBundleService();

        return new CreateStandardPageParametersTO(
                false,
                serviceBundleService.getDefaultServiceBundle().getBusinessKey(),
                "",
                false,
                false,
                false,
                "",
                "",
                false,
                DefaultHtmlController.class.getName(),
                false,
                "net.entellitrak.aea.core.page.defaultViews.SimpleOutView",
                LanguageType.HTML.name(),
                "");
    }

    /**
     * Geta  {@link CreateStandardPageParametersTO} from parsing out the parameters passed to a {@link PageExecutionContext}.
     *
     * @param etk entellitrak execution context
     * @return the parameters determined from the page executino context
     */
    public static CreateStandardPageParametersTO getFromParameters(final PageExecutionContext etk) {
        final Parameters parameters = etk.getParameters();

        final boolean isUpdate = Objects.equals("1", parameters.getSingle("isUpdate"));

        if(isUpdate) {
            return new CreateStandardPageParametersTO(
                    isUpdate,
                    parameters.getSingle("selectedServiceBundleKey"),
                    parameters.getSingle("name"),
                    Objects.equals("1", parameters.getSingle("isPublic")),
                    Objects.equals("1", parameters.getSingle("includeHeaderFooter")),
                    Objects.equals("1", parameters.getSingle("allUsersExecute")),
                    parameters.getSingle("parentPackage"),
                    parameters.getSingle("subPackage"),
                    Objects.equals("1", parameters.getSingle("makeNewControllerScript")),
                    parameters.getSingle("selectedControllerScript"),
                    Objects.equals("1", parameters.getSingle("makeNewViewScript")),
                    parameters.getSingle("selectedViewScript"),
                    parameters.getSingle("selectedViewScriptLanguageType"),
                    parameters.getSingle("newScriptNamePrefix"));
        } else {
            return getDefaultParameters(etk);
        }

    }
}
