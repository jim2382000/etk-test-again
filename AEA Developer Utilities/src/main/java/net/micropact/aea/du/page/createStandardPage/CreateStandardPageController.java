package net.micropact.aea.du.page.createStandardPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.configuration.CoreHandlerTypes;
import com.entellitrak.configuration.LanguageType;
import com.entellitrak.configuration.Script;
import com.entellitrak.configuration.ServiceBundle;
import com.entellitrak.configuration.ServiceBundleAttribute;
import com.entellitrak.configuration.ServiceBundleService;
import com.entellitrak.configuration.Workspace;
import com.entellitrak.configuration.WorkspaceService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.legacy.util.StringUtility;
import com.entellitrak.page.Page;
import com.entellitrak.page.PageController;
import com.entellitrak.page.PageService;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.entellitrak.user.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.entellitrak.aea.gl.api.etk.BundleComponentType;
import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.core.query.Coersion;
import net.micropact.aea.core.utility.BundleUtility;
import net.micropact.aea.core.utility.PackageUtility;
import net.micropact.aea.core.utility.ScriptObjectUtility;
import net.micropact.aea.du.utility.DefaultScriptCodeGeneratorUtility;
import net.micropact.aea.du.utility.PagePermissionUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.ScriptObjectHandlerType;
import net.micropact.aea.utility.ScriptObjectLanguageType;
import net.micropact.aea.utility.Utility;

/**
 * Page Controller for a page which can be used to create a standard page which meets the AE guidelines and standards.
 *
 * @author Zachary.Miller
 */
@HandlerScript(type = PageController.class)
public class CreateStandardPageController implements PageController {

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        final ServiceBundleService serviceBundleService = etk.getServiceBundleService();
        final WorkspaceService workspaceService = etk.getWorkspaceService();

        final CreateStandardPageParametersTO parametersTO = CreateStandardPageParametersTO.getFromParameters(etk);

        final TextResponse response = etk.createTextResponse();

        final CreateStandardPageParametersTO outputParametersTO;

        setBreadcrumb(response);

        final List<String> errors;

        final Gson gson = new GsonBuilder().create();

        final Page newPage;

        if (parametersTO.isUpdate()) {
            PageUtility.validateCsrfToken(etk);

            errors = validateParameters(etk, parametersTO);

            if (errors.isEmpty()) {
                newPage = handleUpdate(etk, parametersTO);
                CreateStandardPageParametersTO.getDefaultParameters(etk);
                outputParametersTO = CreateStandardPageParametersTO.getDefaultParameters(etk);
            } else {
                newPage = null;
                outputParametersTO = parametersTO;
            }
        } else {
            outputParametersTO = CreateStandardPageParametersTO.getDefaultParameters(etk);
            newPage = null;
            errors = Collections.emptyList();
        }

        final List<Map<String, Object>> serviceBundleMaps = serviceBundleService
            .getServiceBundles(ServiceBundleAttribute.SOURCE)
            .stream()
            .map(serviceBundle -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
                { "businessKey", serviceBundle.getBusinessKey() },
                { "name", serviceBundle.getName() }
            }))
            .collect(Collectors.toList());

        final List<Map<String, Object>> packages = PackageUtility.getPackages(etk);

        final Workspace workspace = workspaceService.getSystemWorkspace();
        final List<String> controllerScripts = workspaceService
            .getScriptsByHandlerType(workspace, CoreHandlerTypes.PAGE_CONTROLLER)
            .stream()
            .map(Script::getFullyQualifiedName)
            .sorted()
            .collect(Collectors.toList());

        final List<LanguageType> viewScriptLanguages = Arrays.asList(LanguageType.HTML, LanguageType.JAVASCRIPT);

        final List<String> viewScripts = viewScriptLanguages.stream()
            .flatMap(languageType -> workspaceService.getScriptsByLanguageType(workspace, languageType).stream())
            .map(Script::getFullyQualifiedName)
            .sorted()
            .collect(Collectors.toList());

        final List<Map<String, Object>> viewScriptLanguageMaps = viewScriptLanguages
            .stream()
            .map(languageType -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
                { "name", languageType.name() },
                { "displayName", languageType.getLanguageName() }
            }))
            .collect(Collectors.toList());

        response.put("serviceBundles", gson.toJson(serviceBundleMaps));
        response.put("packages", gson.toJson(packages));
        response.put("controllerScripts", gson.toJson(controllerScripts));
        response.put("viewScripts", gson.toJson(viewScripts));
        response.put("viewScriptLanguageTypes", gson.toJson(viewScriptLanguageMaps));

        response.put("selectedServiceBundleKey", gson.toJson(outputParametersTO.getSelectedServiceBundleKey()));
        response.put("name", gson.toJson(outputParametersTO.getName()));
        response.put("isPublic", gson.toJson(outputParametersTO.isPublic()));
        response.put("includeHeaderFooter", gson.toJson(outputParametersTO.isIncludeHeaderFooter()));
        response.put("allUsersExecute", gson.toJson(outputParametersTO.isAllUsersExecute()));
        response.put("parentPackage", gson.toJson(outputParametersTO.getParentPackage()));
        response.put("subPackage", gson.toJson(outputParametersTO.getSubPackage()));
        response.put("makeNewControllerScript", gson.toJson(outputParametersTO.isMakeNewControllerScript()));
        response.put("makeNewViewScript", gson.toJson(outputParametersTO.isMakeNewViewScript()));
        response.put("selectedViewScriptLanguageType",
            gson.toJson(outputParametersTO.getSelectedViewScriptLanguageType()));
        response.put("selectedControllerScript", gson.toJson(outputParametersTO.getSelectedControllerScript()));
        response.put("selectedViewScript", gson.toJson(outputParametersTO.getSelectedViewScript()));
        response.put("newScriptNamePrefix", gson.toJson(outputParametersTO.getNewScriptNamePrefix()));

        response.put("csrfToken", gson.toJson(etk.getCSRFToken()));

        final Long newPageId = Optional.ofNullable(newPage)
            .map(page -> PageUtility.getPageId(etk, page))
            .orElse(null);

        response.put("errors", gson.toJson(errors));
        response.put("pageId", gson.toJson(newPageId));

        return response;
    }

    /**
     * Set the breadcrumb for the response.
     *
     * @param response
     *            the response
     */
    private static void setBreadcrumb(final TextResponse response) {
        BreadcrumbUtility.setBreadcrumbAndTitle(response,
            BreadcrumbUtility.addLastChildFluent(DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                new SimpleBreadcrumb("Create Standard Page", "page.request.do?page=du.page.createStandardPage")));
    }

    /**
     * Validate the parameters submitted to the page.
     *
     * @param etk
     *            entellitrak execution context
     * @param parametersTO
     *            the parameters transfer object to validate
     * @return the list of errors
     */
    private static List<String> validateParameters(final ExecutionContext etk,
        final CreateStandardPageParametersTO parametersTO) {
        final PageService pageService = etk.getPageService();
        final WorkspaceService workspaceService = etk.getWorkspaceService();

        final List<String> errors = new ArrayList<>();

        if (StringUtility.isBlank(parametersTO.getName())) {
            errors.add("Page Name is required.");
        }

        if (!Objects.equals(parametersTO.getName(), parametersTO.getName().trim())) {
            errors.add("Page Name cannot have leading or trailing whitespace.");
        }

        final boolean pageWithNameAlreadyExists = pageService.getPages().stream()
            .anyMatch(page -> Objects.equals(page.getName(), parametersTO.getName()));

        if (pageWithNameAlreadyExists) {
            errors.add("A page with that name already exists.");
        }

        if (parametersTO.isPublic() && parametersTO.isIncludeHeaderFooter()) {
            errors.add("A page cannot be both publicly accessible, and include Navigation, Header and Footer.");
        }

        final String packagePath = PackageUtility.combinePackagePaths(parametersTO.getParentPackage(),
            parametersTO.getSubPackage());
        if (packagePath.isEmpty()) {
            errors.add(
                "Full Package/Business Key cannot be blank. You must specify one or both of Parent Package/Sub Package.");
        }

        if (!PackageUtility.looksLikeValidJavaPackageFormat(packagePath)) {
            errors.add("Full Package/Business Key does not look like the valid format for a java package.");
        }

        if (pageService.getPageByBusinessKey(packagePath) != null) {
            errors.add("A page with the specified business key already exists.");
        }

        if (!parametersTO.getNewScriptNamePrefix().isEmpty()
            && !ScriptObjectUtility.looksLikeValidClassName(parametersTO.getNewScriptNamePrefix())) {
            errors.add("The New Script Name Prefix should look like a valid java class name.");
        }

        final Workspace workspace = workspaceService.getSystemWorkspace();

        if (parametersTO.isMakeNewControllerScript()
            && workspaceService.getScriptByFullyQualifiedName(
                workspace,
                getControllerScriptFullyQualifiedName(parametersTO.getParentPackage(), parametersTO.getSubPackage(),
                    parametersTO.getNewScriptNamePrefix())) != null) {
            errors.add("It appears that the Controller Script class already exists.");
        }

        if (parametersTO.isMakeNewViewScript()
            && workspaceService.getScriptByFullyQualifiedName(
                workspace,
                getViewScriptFullyQualifiedName(parametersTO.getParentPackage(), parametersTO.getSubPackage(),
                    parametersTO.getNewScriptNamePrefix())) != null) {
            errors.add("It appears that the View Script object already exists.");
        }

        return errors;
    }

    /**
     * Get the simple class name for the controller script.
     *
     * @param newScriptNamePrefix
     *            the name prefix
     * @return the simple class name of the controller script
     */
    private static String getControllerScriptSimpleName(final String newScriptNamePrefix) {
        return String.format("%sController", newScriptNamePrefix);
    }

    /**
     * Get the full class name for the controller script.
     *
     * @param parentPackage
     *            the parent package
     * @param subPackage
     *            the subpackage
     * @param newScriptNamePrefix
     *            the new script name prefix
     * @return the full class name of the controller script
     */
    private static String getControllerScriptFullyQualifiedName(final String parentPackage, final String subPackage,
        final String newScriptNamePrefix) {
        return String.format("%s.%s",
            PackageUtility.combinePackagePaths(parentPackage, subPackage),
            getControllerScriptSimpleName(newScriptNamePrefix));
    }

    /**
     * Get the simple script object name for the view script.
     *
     * @param newScriptNamePrefix
     *            the name prefix
     * @return the simple script object name
     */
    private static String getViewScriptSimpleName(final String newScriptNamePrefix) {
        return String.format("%sView", newScriptNamePrefix);
    }

    /**
     * Get the full name of the script for the view script.
     *
     * @param parentPackage
     *            the parent package
     * @param subPackage
     *            the sub package
     * @param newScriptNamePrefix
     *            the new script name prefix
     * @return the full name of the view script
     */
    private static String getViewScriptFullyQualifiedName(final String parentPackage, final String subPackage,
        final String newScriptNamePrefix) {
        return String.format("%s.%s",
            PackageUtility.combinePackagePaths(parentPackage, subPackage),
            getViewScriptSimpleName(newScriptNamePrefix));
    }

    /**
     * Handle the update request (making a new page).
     *
     * @param etk
     *            entellitrak execution context
     * @param parametersTO
     *            the parameters transfer object
     * @return the page
     */
    private static Page handleUpdate(final ExecutionContext etk, final CreateStandardPageParametersTO parametersTO) {
        final ServiceBundleService serviceBundleService = etk.getServiceBundleService();

        final String parentPackage = parametersTO.getParentPackage();
        final String subPackage = parametersTO.getSubPackage();

        final String path = PackageUtility.combinePackagePaths(parentPackage, subPackage);

        final ServiceBundle serviceBundle = serviceBundleService
            .getServiceBundleByBusinessKey(parametersTO.getSelectedServiceBundleKey());

        final String controllerScriptFullyQualifiedName;

        if (parametersTO.isMakeNewControllerScript()) {
            // TODO: This should not be duplicated and should moved into the thing which actually creates the script
            // object.
            PackageUtility.ensurePackageExistsInSystemRepository(etk, path);
            final Long packageId = PackageUtility.getPackageIdByPathInSystemRepository(etk, path);
            final String packagePath = PackageUtility.getPackagePathById(etk, packageId);

            controllerScriptFullyQualifiedName = getControllerScriptFullyQualifiedName(parentPackage, subPackage,
                parametersTO.getNewScriptNamePrefix());

            final String controllerClassName = getControllerScriptSimpleName(parametersTO.getNewScriptNamePrefix());

            final String code = DefaultScriptCodeGeneratorUtility.getDefaultPageControllerCode(etk, packagePath,
                controllerClassName);

            ScriptObjectUtility.createScriptObjectInSystemRepository(etk, serviceBundle, ScriptObjectLanguageType.JAVA,
                ScriptObjectHandlerType.PAGE_CONTROLLER, controllerClassName, packageId, controllerClassName, code,
                false);
        } else {
            controllerScriptFullyQualifiedName = parametersTO.getSelectedControllerScript();
        }

        final long controllerScriptId = ScriptObjectUtility.getScriptIdFromFullyQualifiedNameInSystemRepository(etk,
            controllerScriptFullyQualifiedName);

        final String viewScriptFullyQualifiedName;

        if (parametersTO.isMakeNewViewScript()) {
            PackageUtility.ensurePackageExistsInSystemRepository(etk, path);
            final Long packageId = PackageUtility.getPackageIdByPathInSystemRepository(etk, path);

            final String code;

            final String viewScriptName = getViewScriptSimpleName(parametersTO.getNewScriptNamePrefix());
            viewScriptFullyQualifiedName = getViewScriptFullyQualifiedName(parentPackage, subPackage,
                parametersTO.getNewScriptNamePrefix());

            final ScriptObjectLanguageType viewLanguageType = ScriptObjectLanguageType
                .valueOf(parametersTO.getSelectedViewScriptLanguageType());

            switch (viewLanguageType) {
                case HTML:
                    code = DefaultScriptCodeGeneratorUtility.getDefaultHtmlCode(etk);
                    break;
                case JAVASCRIPT:
                    code = DefaultScriptCodeGeneratorUtility.getDefaultJavascriptCode(etk);
                    break;
                default:
                    throw new GeneralRuntimeException(String.format("Language Type not supported: %s ", viewLanguageType));
            }

            ScriptObjectUtility.createScriptObjectInSystemRepository(etk, serviceBundle, viewLanguageType,
                ScriptObjectHandlerType.NONE, viewScriptName, packageId, viewScriptName, code, false);
        } else {
            viewScriptFullyQualifiedName = parametersTO.getSelectedViewScript();
        }

        final long viewScriptId = ScriptObjectUtility.getScriptIdFromFullyQualifiedNameInSystemRepository(etk,
            viewScriptFullyQualifiedName);

        final Page page = createStandardPage(etk, serviceBundle, path, parametersTO.getName(), controllerScriptId,
            viewScriptId, parametersTO.isPublic(), parametersTO.isIncludeHeaderFooter(),
            parametersTO.isAllUsersExecute());

        net.micropact.aea.core.utility.WorkspaceService.publishWorkspaceChanges(etk);

        return page;
    }

    /**
     * Create a standard page.
     *
     * @param etk
     *            entellitrak execution context
     * @param serviceBundle
     *            the service bundle
     * @param businessKey
     *            the business key
     * @param name
     *            the name
     * @param controllerScriptId
     *            the controller script id
     * @param viewScriptId
     *            the view script id
     * @param isPublic
     *            whether the page should be public
     * @param includeAppChrome
     *            whether to include the header/footer
     * @param isAllUsersExecute
     *            whether all users should have execute permission
     * @return the page
     */
    // Suppress warning about too many parameters. We need this many.
    @SuppressWarnings("java:S107")
    private static Page createStandardPage(final ExecutionContext etk,
        final ServiceBundle serviceBundle,
        final String businessKey,
        final String name,
        final long controllerScriptId,
        final long viewScriptId,
        final boolean isPublic,
        final boolean includeAppChrome,
        final boolean isAllUsersExecute) {
        final Page page = createPage(etk, serviceBundle, businessKey, name, controllerScriptId, viewScriptId, isPublic,
            includeAppChrome);

        PagePermissionUtility.createAdministratorPagePermission(etk, page);

        if (isAllUsersExecute) {
            PagePermissionUtility.createAllUsersPagePermission(etk, page);
        }

        return page;
    }

    /**
     * Create a page (including the bundle mapping).
     *
     * @param etk
     *            entellitrak execution context
     * @param serviceBundle
     *            the service bundle
     * @param businessKey
     *            the business key
     * @param name
     *            the name
     * @param controllerScriptId
     *            the controller script id
     * @param viewScriptId
     *            the view script id
     * @param isPublic
     *            whether it is public
     * @param includeAppChrome
     *            whether to include the header/footer
     * @return the page
     */
 // Suppress warning about too many parameters. We need this many.
    @SuppressWarnings("java:S107")
    private static Page createPage(
        final ExecutionContext etk,
        final ServiceBundle serviceBundle,
        final String businessKey,
        final String name,
        final long controllerScriptId,
        final long viewScriptId,
        final boolean isPublic,
        final boolean includeAppChrome) {
        final PageService pageService = etk.getPageService();
        final User user = etk.getCurrentUser();

        final Map<String, Object> insertQueryParameters = Utility.arrayToMap(String.class, Object.class,
            new Object[][] {
                { "business_key", businessKey },
                { "public_page", Coersion.toLong(isPublic) },
                { "component_page", 0 },
                { "controller_script_id", controllerScriptId },
                { "view_script_id", viewScriptId },
                { "user_id", user.getId() },
                { "created_by", user.getAccountName() },
                { "created_on", new Date() },
                { "last_updated_by", user.getAccountName() },
                { "last_updated_on", new Date() },
                { "description", name },
                { "name", name },
                { "include_app_chrome", includeAppChrome },
            });

        if (Utility.isSqlServer(etk) || Utility.isPostgreSQL(etk)) {
            etk.createSQL(
                "INSERT INTO etk_page(business_key, public_page, component_page, controller_script_id, view_script_id, user_id, created_by, created_on, last_updated_by, last_updated_on, description, name, include_app_chrome) VALUES(:business_key, :public_page, :component_page, :controller_script_id, :view_script_id, :user_id, :created_by, :created_on, :last_updated_by, :last_updated_on, :description, :name, :include_app_chrome)")
                .setParameter(insertQueryParameters)
                .execute();
        } else {
            etk.createSQL(
                "INSERT INTO etk_page(page_id, business_key, public_page, component_page, controller_script_id, view_script_id, user_id, created_by, created_on, last_updated_by, last_updated_on, description, name, include_app_chrome) VALUES(HIBERNATE_SEQUENCE.NEXTVAL, :business_key, :public_page, :component_page, :controller_script_id, :view_script_id, :user_id, :created_by, :created_on, :last_updated_by, :last_updated_on, :description, :name, :include_app_chrome)")
                .setParameter(insertQueryParameters)
                .execute();
        }

        BundleUtility.insertBundleMapping(etk, serviceBundle, BundleComponentType.PAGE, businessKey);

        return pageService.getPageByBusinessKey(businessKey);
    }
}
