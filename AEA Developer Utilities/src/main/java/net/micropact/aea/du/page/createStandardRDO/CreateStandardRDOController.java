package net.micropact.aea.du.page.createStandardRDO;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.entellitrak.ApplicationException;
import com.entellitrak.ExecutionContext;
import com.entellitrak.IncorrectResultSizeDataAccessException;
import com.entellitrak.PageExecutionContext;
import com.entellitrak.SystemPreferenceService;
import com.entellitrak.configuration.ServiceBundle;
import com.entellitrak.configuration.ServiceBundleAttribute;
import com.entellitrak.configuration.ServiceBundleService;
import com.entellitrak.handler.HandlerScript;
import com.entellitrak.page.PageController;
import com.entellitrak.page.Parameters;
import com.entellitrak.page.Response;
import com.entellitrak.page.SimpleBreadcrumb;
import com.entellitrak.page.TextResponse;
import com.entellitrak.user.Role;
import com.entellitrak.user.User;
import com.google.gson.Gson;

import net.entellitrak.aea.gl.api.etk.BundleComponentType;
import net.micropact.aea.core.enums.DataPermissionAccessLevelType;
import net.micropact.aea.core.enums.Designator;
import net.micropact.aea.core.enums.SystemPreference;
import net.micropact.aea.core.pageUtility.BreadcrumbUtility;
import net.micropact.aea.core.pageUtility.PageUtility;
import net.micropact.aea.core.query.DatabaseSequence;
import net.micropact.aea.core.utility.BundleUtility;
import net.micropact.aea.du.utility.page.DuBreadcrumbUtility;
import net.micropact.aea.utility.DataElementRequiredLevel;
import net.micropact.aea.utility.DataElementType;
import net.micropact.aea.utility.FormControlType;
import net.micropact.aea.utility.LookupSourceType;
import net.micropact.aea.utility.Utility;

/**
 * This controller is for a page which generates a &quot;Standard&quot; Reference Data Object. This is because most
 * Reference Data Objects have columns for Name, Code, Order, Start Date and End Date. The page submits to itself
 * instead of making an AJAX call, so that is why it contains the creation logic.
 *
 * @author zmiller
 */
@HandlerScript(type = PageController.class)
public class CreateStandardRDOController implements PageController {

    private static final boolean GRANT_CURRENT_ROLE_PERMISSIONS_DEFAULT = true;

    /*
     * Note that this pattern is stricter than is absolutely necessary.
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z\\d ]*$");

    /**
     * The size of the Long data type.
     */
    private static final long LONG_DATA_SIZE = 50;

    /**
     * Maximum number of characters allowed for table name.
     */
    private static final long MAX_TABLE_NAME_SIZE = 30;

    /**
     * entellitrak's form designer calculates an extra padding of 4 when determining the y coordinate of elements.
     */
    private static final long FORM_VERTICAL_PADDING = 4;

    /**
     * Default height of entellitrak data form elements.
     */
    private static final long DEFAULT_FORM_ELEMENT_HEIGHT = 25;

    /**
     * This is the default width we will make text elements. It is NOT the width entellitrak uses by default.
     */
    private static final long DEFAULT_FORM_ELEMENT_WIDTH = 300;

    /**
     * This is how wide entellitrak date fields are on a form.
     */
    private static final long DEFAULT_FORM_DATE_WIDTH = 194;

    @Override
    public Response execute(final PageExecutionContext etk) throws ApplicationException {
        try {
            final ServiceBundleService serviceBundleService = etk.getServiceBundleService();
            final SystemPreferenceService systemPreferenceService = etk.getSystemPreferenceService();
            final Parameters parameters = etk.getParameters();
            final User currentUser = etk.getCurrentUser();

            final TextResponse response = etk.createTextResponse();

            setBreadcrumb(response);

            /*
             * requestedAction will say why they are submitting the form. If it is "generate" it means that they want
             * the Data Object created.
             */
            final String requestedAction = parameters.getSingle("requestedAction");

            // Get all the post parameters
            final String bundleParameter = parameters.getSingle("bundleKey");
            final String tableName = parameters.getSingle("tableName");
            final String name = parameters.getSingle("name");
            final String objectName = parameters.getSingle("objectName");
            // businessKeySegment is the part of a business key that follows "object."
            final String businessKeySegment = parameters.getSingle("businessKeySegment");
            final String label = parameters.getSingle("label");
            final String description = parameters.getSingle("description");
            final boolean generateLookup = "1".equals(parameters.getSingle("generateLookup"));
            final String lookupName = parameters.getSingle("lookupName");
            final String nameSizeParameter = parameters.getSingle("nameSize");
            final String codeSizeParameter = parameters.getSingle("codeSize");

            final String grantCurrentRolePermissionParameter = parameters.getSingle("grantCurrentRolePermission");
            final boolean grantCurrentRolePermission;

            final String defaultBundleKey = serviceBundleService.getDefaultServiceBundle().getBusinessKey();

            final String bundleKey = Optional.ofNullable(bundleParameter)
                .orElse(defaultBundleKey);

            final ServiceBundle serviceBundle = serviceBundleService.getServiceBundleByBusinessKey(bundleKey);

            if ("generate".equals(requestedAction)) {
                grantCurrentRolePermission = "true".equals(grantCurrentRolePermissionParameter);
            } else {
                grantCurrentRolePermission = GRANT_CURRENT_ROLE_PERMISSIONS_DEFAULT;
            }

            final String defaultTextSizePreferenceName = SystemPreference.DEFAULT_TEXT_SIZE.getName();
            final long defaultTextSize = Long
                .parseLong(systemPreferenceService.loadPreference(defaultTextSizePreferenceName), 10);

            final long nameSize = nameSizeParameter == null ? defaultTextSize : Long.parseLong(nameSizeParameter);
            final long codeSize = codeSizeParameter == null ? defaultTextSize : Long.parseLong(codeSizeParameter);

            final List<Map<String, Object>> dataElements = getDefaultDataElements(nameSize, codeSize);

            final Role currentUserRole = currentUser.getRole();
            final String currentUserRoleBusinessKey = currentUserRole.getBusinessKey();
            final Long currentUserRoleId = currentUserRole.getId();

            Long dataObjectId = null;
            Long dataFormId = null;
            Long dataViewId = null;
            Long lookupDefinitionId = null;
            String error = null;

            if ("generate".equals(requestedAction)) {
                PageUtility.validateCsrfToken(etk);

                // Declare a bunch of other variables mainly related to Forms and Views which will be used later
                final String businessKey = String.format("object.%s", businessKeySegment);

                final String formTitle = name;
                final String formName = String.format("%s Default Form", name);
                final String formBusinessKey = String.format("%s.form.%sDefaultForm", businessKey, businessKeySegment);
                final String formDescription = String.format("Default form for %s", name);

                final String viewName = String.format("%s Default View", name);
                final String viewTitle = String.format("%s Listing", name);
                final String viewBusinessKey = String.format("%s.view.%sDefaultView", businessKey, businessKeySegment);
                final String viewDescription = String.format("%s Listing", name);

                // This is where we will display any validation errors
                if (Utility.isBlank(name)) {
                    /* We check that the name's not blank. */
                    error = "Name cannot be blank.";
                } else if (tableName.length() > MAX_TABLE_NAME_SIZE) {
                    error = String.format("The requested table name is %s characters, but the limit is %s characters",
                        tableName.length(),
                        MAX_TABLE_NAME_SIZE);
                } else if (0 < etk.createSQL(
                    "SELECT COUNT(*) FROM etk_data_object WHERE business_key = :businessKey AND tracking_config_id = :trackingConfigId")
                    .setParameter("businessKey", businessKey)
                    .setParameter("trackingConfigId", Utility.getTrackingConfigIdNext(etk))
                    .fetchLong()) {
                    // Check that the business key is not being used
                    error = String.format("The Object Business Key \"%s\" was already in use.", businessKey);
                } else if (0 < etk.createSQL(
                    "SELECT COUNT(*) FROM etk_data_object WHERE table_name = :tableName AND tracking_config_id = :trackingConfigId")
                    .setParameter("tableName", tableName)
                    .setParameter("trackingConfigId", Utility.getTrackingConfigIdNext(etk))
                    .fetchLong()) {
                    // Check that the table is not being used.
                    error = String.format("The Table Name \"%s\" was already in use.", tableName);
                } else if (!NAME_PATTERN.matcher(name).matches()) {
                    error = "The chosen name is invalid. Name can only contain alphanumeric characters (and spaces) and cannot start with a number";
                } else {
                    final Map<String, Object> dataObjectQueryParameters = Utility.arrayToMap(String.class, Object.class,
                        new Object[][] {
                            { "tableName", tableName },
                            { "name", name },
                            { "objectName", objectName },
                            { "businessKey", businessKey },
                            { "label", label },
                            { "description", description },
                            { "designator", Designator.DESKTOP.getEntellitrakNumber() },
                        });

                    /* ETK_DATA_OBJECT */
                    if (Utility.isSqlServer(etk)) {
                        dataObjectId = etk.createSQL(
                            "INSERT INTO etk_data_object(tracking_config_id, parent_object_id, base_object, table_name, object_type, applied_changes, list_order, list_style, searchable, label, cardinality, object_name, separate_inbox, business_key, name, description, document_management_enabled, designator, auto_assignment, extensible, indexable) VALUES((SELECT tracking_config_id FROM etk_tracking_config WHERE config_version = (SELECT MAX(config_version) FROM etk_tracking_config)), NULL, 1, :tableName, 2, 0, 0, 1, 1, :label, -1, :objectName, 0, :businessKey, :name, :description, 0, :designator, 0, 0, 1) ")
                            .setParameter(dataObjectQueryParameters)
                            .execute("DATA_OBJECT_ID");
                    } else if (Utility.isPostgreSQL(etk)) {
                        dataObjectId = etk.createSQL(
                            "INSERT INTO etk_data_object(tracking_config_id, parent_object_id, base_object, table_name, object_type, applied_changes, list_order, list_style, searchable, label, cardinality, object_name, separate_inbox, business_key, name, description, document_management_enabled, designator, auto_assignment, extensible, indexable) VALUES((SELECT tracking_config_id FROM etk_tracking_config WHERE config_version = (SELECT MAX(config_version) FROM etk_tracking_config)), NULL, 1, :tableName, 2, 0, 0, 1, 1, :label, -1, :objectName, 0, :businessKey, :name, :description, 0, :designator, 0, 0, 1) returning DATA_OBJECT_ID")
                            .setParameter(dataObjectQueryParameters)
                            .execute("DATA_OBJECT_ID");
                    } else {
                        dataObjectId = DatabaseSequence.HIBERNATE_SEQUENCE.getNextVal(etk);

                        etk.createSQL(
                            "INSERT INTO etk_data_object(data_object_id, tracking_config_id, parent_object_id, base_object, table_name, object_type, applied_changes, list_order, list_style, searchable, label, cardinality, object_name, separate_inbox, business_key, name, description, document_management_enabled, designator, auto_assignment, extensible, indexable) VALUES(:dataObjectId, (SELECT tracking_config_id FROM etk_tracking_config WHERE config_version = (SELECT MAX(config_version) FROM etk_tracking_config)), NULL, 1, :tableName, 2, 0, 0, 1, 1, :label, -1, :objectName, 0, :businessKey, :name, :description, 0, :designator, 0, 0, 1) ")
                            .setParameter("dataObjectId", dataObjectId)
                            .setParameter(dataObjectQueryParameters)
                            .execute();
                    }

                    BundleUtility.insertBundleMapping(etk, serviceBundle, BundleComponentType.DATA_OBJECT, businessKey);

                    /* ETK_DATA_ELEMENT */
                    final long theDataObjectId = dataObjectId;

                    dataElements.forEach(dataElement -> {
                        final long dataElementId;

                        final Map<String, Object> dataElementQueryParameters = Utility.arrayToMap(String.class,
                            Object.class, new Object[][] {
                                { "dataObjectId", theDataObjectId },
                                { "name", dataElement.get("name") },
                                { "businessKey",
                                    String.format("%s.element.%s", businessKey, dataElement.get("elementName")) },
                                { "columnName", dataElement.get("columnName") },
                                { "dataType",
                                    ((DataElementType) dataElement.get("dataElementType"))
                                        .getEntellitrakNumber() },
                                { "elementName", dataElement.get("elementName") },
                                { "dataSize", dataElement.get("dataSize") },
                                { "futureDatesAllowed",
                                    (boolean) dataElement.get("futureDatesAllowed") ? 1 : 0 },
                                { "required",
                                    ((DataElementRequiredLevel) dataElement.get("required"))
                                        .getEntellitrakNumber() },
                                { "description", dataElement.get("description") },
                            });

                        if (Utility.isSqlServer(etk)) {
                            dataElementId = etk.createSQL(
                                "INSERT INTO etk_data_element(data_object_id, name, data_type, required, validation_required, column_name, primary_key, system_field, index_type, default_value, searchable, is_unique, data_size, bound_to_lookup, lookup_definition_id, element_name, default_to_today, future_dates_allowed, identifier, logged, plugin_registration_id, applied_changes, table_name, business_key, description, stored_in_document_management, used_for_escan) VALUES (:dataObjectId, :name, :dataType, :required, null, :columnName, null, 0, null, null, 1, 0, :dataSize, 0, null, :elementName, 0, :futureDatesAllowed, 0, 1, null, 0, null, :businessKey, :description, 0, 0)")
                                .setParameter(dataElementQueryParameters)
                                .execute("DATA_ELEMENT_ID");
                        } else if (Utility.isPostgreSQL(etk)) {
                            dataElementId = etk.createSQL(
                                "INSERT INTO etk_data_element(data_object_id, name, data_type, required, validation_required, column_name, primary_key, system_field, index_type, default_value, searchable, is_unique, data_size, bound_to_lookup, lookup_definition_id, element_name, default_to_today, future_dates_allowed, identifier, logged, plugin_registration_id, applied_changes, table_name, business_key, description, stored_in_document_management, used_for_escan) VALUES (:dataObjectId, :name, :dataType, :required, null, :columnName, null, 0, null, null, 1, 0, :dataSize, 0, null, :elementName, 0, :futureDatesAllowed, 0, 1, null, 0, null, :businessKey, :description, 0, 0) returning DATA_ELEMENT_ID")
                                .setParameter(dataElementQueryParameters)
                                .execute("DATA_ELEMENT_ID");
                        } else {
                            dataElementId = DatabaseSequence.HIBERNATE_SEQUENCE.getNextVal(etk);

                            etk.createSQL(
                                "INSERT INTO etk_data_element(data_element_id, data_object_id, name, data_type, required, validation_required, column_name, primary_key, system_field, index_type, default_value, searchable, is_unique, data_size, bound_to_lookup, lookup_definition_id, element_name, default_to_today, future_dates_allowed, identifier, logged, plugin_registration_id, applied_changes, table_name, business_key, description, stored_in_document_management, used_for_escan) VALUES (:dataElementId, :dataObjectId, :name, :dataType, :required, null, :columnName, null, 0, null, null, 1, 0, :dataSize, 0, null, :elementName, 0, :futureDatesAllowed, 0, 1, null, 0, null, :businessKey, :description, 0, 0)")
                                .setParameter(dataElementQueryParameters)
                                .setParameter("dataElementId", dataElementId)
                                .execute();
                        }

                        dataElement.put("dataElementId", dataElementId);
                    });

                    final Map<String, Object> dataViewQueryParameters = Utility.arrayToMap(String.class, Object.class,
                        new Object[][] {
                            { "dataObjectId", dataObjectId },
                            { "name", viewName },
                            { "title", viewTitle },
                            { "businessKey", viewBusinessKey },
                            { "description", viewDescription },
                        });

                    /* ETK_DATA_VIEW */
                    if (Utility.isSqlServer(etk)) {
                        dataViewId = etk.createSQL(
                            "INSERT INTO etk_data_view(data_object_id, title, text, default_view, search_view, business_key, name, description) VALUES(:dataObjectId, :title, NULL, 1, 1, :businessKey, :name, :description)")
                            .setParameter(dataViewQueryParameters)
                            .execute("DATA_VIEW_ID");
                    } else if (Utility.isPostgreSQL(etk)) {
                        dataViewId = etk.createSQL(
                            "INSERT INTO etk_data_view(data_object_id, title, text, default_view, search_view, business_key, name, description) VALUES(:dataObjectId, :title, NULL, 1, 1, :businessKey, :name, :description) returning DATA_VIEW_ID")
                            .setParameter(dataViewQueryParameters)
                            .execute("DATA_VIEW_ID");
                    } else {
                        dataViewId = DatabaseSequence.HIBERNATE_SEQUENCE.getNextVal(etk);

                        etk.createSQL(
                            "INSERT INTO etk_data_view(data_view_id, data_object_id, title, text, default_view, search_view, business_key, name, description) VALUES(:dataViewId, :dataObjectId, :title, NULL, 1, 1, :businessKey, :name, :description)")
                            .setParameter("dataViewId", dataViewId)
                            .setParameter(dataViewQueryParameters)
                            .execute();
                    }

                    /* ETK_DATA_VIEW_ELEMENT */
                    for (int displayOrder = 0; displayOrder < dataElements.size(); displayOrder++) {

                        final Map<String, Object> dataElement = dataElements.get(displayOrder);

                        etk.createSQL(Utility.isSqlServer(etk) || Utility.isPostgreSQL(etk)
                            ? "INSERT INTO etk_data_view_element(data_view_id, name, data_element_id, display_order, label, display_size, multi_value_delimiter, business_key, description, responsiveness_factor) VALUES(:dataViewId, :name, :dataElementId, :displayOrder, :label, null, null, :businessKey, :description, 1)"
                            : "INSERT INTO etk_data_view_element(data_view_element_id, data_view_id, name, data_element_id, display_order, label, display_size, multi_value_delimiter, business_key, description, responsiveness_factor) VALUES(HIBERNATE_SEQUENCE.NEXTVAL, :dataViewId, :name, :dataElementId, :displayOrder, :label, null, null, :businessKey, :description, 1)")
                            .setParameter("dataElementId", dataElement.get("dataElementId"))
                            .setParameter("dataViewId", dataViewId)
                            .setParameter("name", dataElement.get("elementName"))
                            .setParameter("businessKey",
                                String.format("%s.element.%s", viewBusinessKey, dataElement.get("elementName")))
                            .setParameter("label", dataElement.get("name"))
                            .setParameter("description", String.format("%s column.", dataElement.get("name")))
                            .setParameter("displayOrder", displayOrder)
                            .execute();
                    }

                    final Map<String, Object> dataFormQueryParameters = Utility.arrayToMap(String.class, Object.class,
                        new Object[][] {
                            { "dataObjectId", dataObjectId },
                            { "title", formTitle },
                            { "name", formName },
                            { "formBusinessKey", formBusinessKey },
                            { "description", formDescription },
                        });

                    /* ETK_DATA_FORM */
                    if (Utility.isSqlServer(etk)) {
                        dataFormId = etk.createSQL(
                            "INSERT INTO etk_data_form(data_object_id, title, instructions, default_form, search_form, layout_type, business_key, name, description, offline_form) VALUES(:dataObjectId, :title, NULL, 1, 1, 1, :formBusinessKey, :name, :description, 0)")
                            .setParameter(dataFormQueryParameters)
                            .execute("DATA_FORM_ID");
                    } else if (Utility.isPostgreSQL(etk)) {
                        dataFormId = etk.createSQL(
                            "INSERT INTO etk_data_form(data_object_id, title, instructions, default_form, search_form, layout_type, business_key, name, description, offline_form) VALUES(:dataObjectId, :title, NULL, 1, 1, 1, :formBusinessKey, :name, :description, 0) returning DATA_FORM_ID")
                            .setParameter(dataFormQueryParameters)
                            .execute("DATA_FORM_ID");
                    } else {
                        dataFormId = DatabaseSequence.HIBERNATE_SEQUENCE.getNextVal(etk);

                        etk.createSQL(
                            "INSERT INTO etk_data_form(data_form_id, data_object_id, title, instructions, default_form, search_form, layout_type, business_key, name, description, offline_form) VALUES(:dataFormId, :dataObjectId, :title, NULL, 1, 1, 1, :formBusinessKey, :name, :description, 0)")
                            .setParameter("dataFormId", dataFormId)
                            .setParameter(dataFormQueryParameters)
                            .execute();
                    }

                    /* ETK_FORM_CONTROL */
                    {
                        /*
                         * This variable holds the y coordinate of the field in form builder. It needs to be incremented
                         * as we loop through the fields since it depends on the fields above it.
                         */
                        long y = 0;

                        for (int index = 0; index < dataElements.size(); index++) {

                            final Map<String, Object> dataElement = dataElements.get(index);

                            final long formControlId;

                            final Map<String, Object> formControlQueryParameters = Utility.arrayToMap(String.class,
                                Object.class, new Object[][] {
                                    { "dataFormId", dataFormId },
                                    { "businessKey",
                                        String.format("%s.control.%s", formBusinessKey,
                                            dataElement.get("elementName")) },
                                    { "label", dataElement.get("name") },
                                    { "name", dataElement.get("elementName") },
                                    { "formControlType",
                                        ((FormControlType) dataElement.get("formControlType"))
                                            .getEntellitrakName() },
                                    { "displayOrder", index + 1 },
                                    { "width", dataElement.get("width") },
                                    { "height", dataElement.get("height") },
                                    { "x", 0 },
                                    { "y", y },
                                    { "description", String.format("%s form control", dataElement.get("name")) },
                                    { "readOnly", 0 },
                                    { "tooltipText", null },
                                    { "mutableReadOnly", 0 },
                                });

                            if (Utility.isSqlServer(etk)) {
                                formControlId = etk.createSQL(
                                    "INSERT INTO etk_form_control(form_control_type, name, data_form_id, display_order, label, read_only, height, width, x, y, business_key, description, tooltip_text, mutable_read_only) VALUES (:formControlType, :name, :dataFormId, :displayOrder, :label, :readOnly, :height, :width, :x, :y, :businessKey, :description, :tooltipText, :mutableReadOnly)")
                                    .setParameter(formControlQueryParameters)
                                    .execute("FORM_CONTROL_ID");
                            } else if (Utility.isPostgreSQL(etk)) {
                                formControlId = etk.createSQL(
                                    "INSERT INTO etk_form_control(form_control_type, name, data_form_id, display_order, label, read_only, height, width, x, y, business_key, description, tooltip_text, mutable_read_only) VALUES (:formControlType, :name, :dataFormId, :displayOrder, :label, :readOnly, :height, :width, :x, :y, :businessKey, :description, :tooltipText, :mutableReadOnly) returning FORM_CONTROL_ID")
                                    .setParameter(formControlQueryParameters)
                                    .execute("FORM_CONTROL_ID");
                            } else {
                                formControlId = DatabaseSequence.HIBERNATE_SEQUENCE.getNextVal(etk);

                                etk.createSQL(
                                    "INSERT INTO etk_form_control(form_control_id, form_control_type, name, data_form_id, display_order, label, read_only, height, width, x, y, business_key, description, tooltip_text, mutable_read_only) VALUES (:formControlId, :formControlType, :name, :dataFormId, :displayOrder, :label, :readOnly, :height, :width, :x, :y, :businessKey, :description, :tooltipText, :mutableReadOnly)")
                                    .setParameter("formControlId", formControlId)
                                    .setParameter(formControlQueryParameters)
                                    .execute();
                            }

                            y += ((Number) dataElement.get("height")).longValue() + FORM_VERTICAL_PADDING;

                            dataElement.put("formControlId", formControlId);
                        }
                    }

                    /* ETK_FORM_CTL_ELEMENT_BINDING */
                    dataElements.forEach(dataElement -> etk.createSQL(
                        "INSERT INTO etk_form_ctl_element_binding(form_control_id, data_element_id) VALUES(:formControlId, :dataElementId)")
                        .setParameter("formControlId", dataElement.get("formControlId"))
                        .setParameter("dataElementId", dataElement.get("dataElementId"))
                        .execute());

                    if (generateLookup) {
                        lookupDefinitionId = generateDataObjectLookup(etk, serviceBundle, dataObjectId, lookupName);
                    } else {
                        lookupDefinitionId = null;
                    }

                    if (grantCurrentRolePermission) {
                        insertRdoRoleDataPermissions(etk, currentUserRoleId, tableName);
                    }
                }
            }

            final boolean persistFormValues = "generate".equals(requestedAction) && error != null;

            final List<Map<String, Object>> bundles = serviceBundleService
                .getServiceBundles(ServiceBundleAttribute.SOURCE)
                .stream()
                .map(bundle -> Utility.arrayToMap(String.class, Object.class, new Object[][] {
                    { "BUSINESS_KEY", bundle.getBusinessKey() },
                    { "NAME", bundle.getName() }
                }))
                .collect(Collectors.toList());

            final Gson gson = new Gson();

            response.put("bundles", gson.toJson(bundles));
            response.put("bundleKey", gson.toJson(persistFormValues ? bundleKey : defaultBundleKey));
            response.put("name", gson.toJson(persistFormValues ? name : ""));
            response.put("objectName", gson.toJson(persistFormValues ? objectName : ""));
            response.put("businessKeySegment", gson.toJson(persistFormValues ? businessKeySegment : ""));
            response.put("description", gson.toJson(persistFormValues ? description : ""));
            response.put("label", gson.toJson(persistFormValues ? label : ""));
            response.put("tableName", gson.toJson(persistFormValues ? tableName : ""));
            response.put("generateLookup", gson.toJson(persistFormValues && generateLookup));
            response.put("lookupName", gson.toJson(persistFormValues ? lookupName : ""));
            response.put("currentRoleBusinessKey", gson.toJson(currentUserRoleBusinessKey));
            response.put("grantCurrentRolePermissions", gson.toJson(persistFormValues ? grantCurrentRolePermission : GRANT_CURRENT_ROLE_PERMISSIONS_DEFAULT));
            response.put("nameSize", gson.toJson(nameSize));
            response.put("codeSize", gson.toJson(codeSize));

            response.put("dataElements", gson.toJson(dataElements));
            response.put("dataObjectId", gson.toJson(dataObjectId));
            response.put("dataFormId", gson.toJson(dataFormId));
            response.put("dataViewId", gson.toJson(dataViewId));
            response.put("lookupDefinitionId", gson.toJson(lookupDefinitionId));
            response.put("error", gson.toJson(error));

            response.put("csrfToken", gson.toJson(etk.getCSRFToken()));

            return response;

        } catch (final IncorrectResultSizeDataAccessException e) {
            throw new ApplicationException(e);
        }
    }

    /**
     * Set the breadcrumb for the response.
     *
     * @param response
     *            the response
     */
    private static void setBreadcrumb(final TextResponse response) {
        BreadcrumbUtility.setBreadcrumbAndTitle(response,
            BreadcrumbUtility.addLastChildFluent(
                DuBreadcrumbUtility.getDeveloperUtilityBreadcrumb(),
                new SimpleBreadcrumb("Create Standard RDO",
                    "page.request.do?page=du.page.createStandardRDO")));
    }

    /**
     * Insert role data permissions for a specific role/ rdo object. If any permissions existed before for this
     * role/object combination it gets deleted. Grants full permissions to the role.
     *
     * @param etk
     *            entellitrak execution context
     * @param roleId
     *            the role id to generate the permissions for
     * @param tableName
     *            the table name of the data object to generate the permissions for
     */
    private static void insertRdoRoleDataPermissions(final ExecutionContext etk, final long roleId,
        final String tableName) {
        /*
         * Delete any existing records to protect against duplicates in case the object had previously exist at some
         * point
         */
        etk.createSQL("DELETE FROM etk_data_permission WHERE role_id = :roleId AND data_object_type = :dataObjectType")
            .setParameter("roleId", roleId)
            .setParameter("dataObjectType", tableName)
            .execute();

        final String insertDataPermissionQuery;
        final Long dataPermissionId;

        if (Utility.isSqlServer(etk) || Utility.isPostgreSQL(etk)) {
            dataPermissionId = null;
            insertDataPermissionQuery = "INSERT INTO etk_data_permission(role_id, data_object_type, assign_access_level, create_access_level, read_access_level, update_access_level, delete_access_level, reporting_access_level, searching_access_level, data_element_type, read_content_access_level, inbox_enabled) VALUES (:role_id, :data_object_type, :assign_access_level, :create_access_level, :read_access_level, :update_access_level, :delete_access_level, :reporting_access_level, :searching_access_level, :data_element_type, :read_content_access_level, :inbox_enabled)";
        } else {
            dataPermissionId = DatabaseSequence.HIBERNATE_SEQUENCE.getNextVal(etk);
            insertDataPermissionQuery = "INSERT INTO etk_data_permission(data_permission_id, role_id, data_object_type, assign_access_level, create_access_level, read_access_level, update_access_level, delete_access_level, reporting_access_level, searching_access_level, data_element_type, read_content_access_level, inbox_enabled) VALUES (:data_permission_id, :role_id, :data_object_type, :assign_access_level, :create_access_level, :read_access_level, :update_access_level, :delete_access_level, :reporting_access_level, :searching_access_level, :data_element_type, :read_content_access_level, :inbox_enabled)";
        }

        etk.createSQL(insertDataPermissionQuery)
            .setParameter(Utility.arrayToMap(String.class, Object.class, new Object[][] {
                { "data_permission_id", dataPermissionId },
                { "role_id", roleId },
                { "data_object_type", tableName },
                { "assign_access_level", DataPermissionAccessLevelType.NONE.getEntellitrakNumber() },
                { "create_access_level", DataPermissionAccessLevelType.GLOBAL.getEntellitrakNumber() },
                { "read_access_level", DataPermissionAccessLevelType.GLOBAL.getEntellitrakNumber() },
                { "update_access_level", DataPermissionAccessLevelType.GLOBAL.getEntellitrakNumber() },
                { "delete_access_level", DataPermissionAccessLevelType.GLOBAL.getEntellitrakNumber() },
                { "reporting_access_level", DataPermissionAccessLevelType.NONE.getEntellitrakNumber() },
                { "searching_access_level", DataPermissionAccessLevelType.NONE.getEntellitrakNumber() },
                { "data_element_type", null },
                { "read_content_access_level", DataPermissionAccessLevelType.NONE.getEntellitrakNumber() },
                { "inbox_enabled", 1 },
            }))
            .execute();
    }

    /**
     * Utility method which just converts the passed in values into a Map.
     *
     * @param name
     *            ETK_DATA_ELEMENT.NAME
     * @param columnName
     *            ETK_DATA_ELEMENT.COLUMN_NAME
     * @param dataElementType
     *            ETK_DATA_ELEMENT.DATA_TYPE
     * @param elementName
     *            ETK_DATA_ELEMENT.ELEMENT_NAME
     * @param dataSize
     *            ETK_DATA_ELEMENT.DATA_SIZE
     * @param futureDatesAllowed
     *            ETK_DATA_ELEMENT.FUTURE_DATES_ALLOWED
     * @param required
     *            ETK_DATA_ELEMENT.REQUIRED
     * @param description
     *            ET_DATA_ELEMENT.DESCRIPTION
     * @param width
     *            ETK_DATA_ELEMENT.WIDTH
     * @param height
     *            ETK_DATA_ELEMENT.HEIGHT
     * @param formControlType
     *            ETK_FORM_CONTROL.FORM_CONTROL_TYPE
     * @return Map containing the given fields.
     */
    // Suppress warning about too many parameters. We need this many.
    @SuppressWarnings("java:S107")
    private static Map<String, Object> generateDefaultDataElement(
        final String name,
        final String columnName,
        final DataElementType dataElementType,
        final String elementName,
        final long dataSize,
        final boolean futureDatesAllowed,
        final DataElementRequiredLevel required,
        final String description,
        final long width,
        final long height,
        final FormControlType formControlType) {
        return Utility.arrayToMap(String.class, Object.class, new Object[][] {
            { "name", name },
            { "columnName", columnName },
            { "dataElementType", dataElementType },
            { "elementName", elementName },
            { "dataSize", dataSize },
            { "futureDatesAllowed", futureDatesAllowed },
            { "required", required },
            { "description", description },
            { "width", width },
            { "height", height },
            { "formControlType", formControlType },
        });
    }

    /**
     * Returns default data that we know about Name, Code, Order, Start Date and End Date. More information needs to be
     * added to the Data Elements before they can actually be inserted into the database.
     *
     * @param nameSize
     *            the length of the name field
     * @param codeSize
     *            the length of the code field
     * @return List of Default Data Elements for Name, Code, Order, Start Date and End Date
     */
    private static List<Map<String, Object>> getDefaultDataElements(final long nameSize, final long codeSize) {
        return Stream.of(
            generateDefaultDataElement("Name", "C_NAME", DataElementType.TEXT, "name", nameSize,
                false, DataElementRequiredLevel.REQUIRED, "Name", DEFAULT_FORM_ELEMENT_WIDTH,
                DEFAULT_FORM_ELEMENT_HEIGHT, FormControlType.TEXT),
            generateDefaultDataElement("Code", "C_CODE", DataElementType.TEXT, "code", codeSize,
                false, DataElementRequiredLevel.REQUIRED, "Code", DEFAULT_FORM_ELEMENT_WIDTH,
                DEFAULT_FORM_ELEMENT_HEIGHT, FormControlType.TEXT),
            generateDefaultDataElement("Order", "C_ORDER", DataElementType.LONG, "order", LONG_DATA_SIZE, false,
                DataElementRequiredLevel.NOT_REQUIRED, "Order", DEFAULT_FORM_ELEMENT_WIDTH, DEFAULT_FORM_ELEMENT_HEIGHT,
                FormControlType.TEXT),
            generateDefaultDataElement("Start Date", "ETK_START_DATE", DataElementType.DATE, "startDate",
                0, true, DataElementRequiredLevel.NOT_REQUIRED,
                "Start Date is used to define when a reference record becomes active.  A null/empty value means that the record will always be active unless an End Date is set.",
                DEFAULT_FORM_DATE_WIDTH, DEFAULT_FORM_ELEMENT_HEIGHT, FormControlType.DATE),
            generateDefaultDataElement("End Date", "ETK_END_DATE", DataElementType.DATE, "endDate", 0,
                true, DataElementRequiredLevel.NOT_REQUIRED,
                "End Date is used to define when a reference record becomes inactive.  A null/empty value means that the record will always be active unless a Start Date is set.",
                DEFAULT_FORM_DATE_WIDTH, DEFAULT_FORM_ELEMENT_HEIGHT, FormControlType.DATE))
            .collect(Collectors.toList());
    }

    /**
     * Generates a Lookup of type Data Object for the given Data Object. It uses name for Display, Order for Order, and
     * Start Date and End Date for Start and End Date.
     *
     * @param etk
     *            entellitrak Execution Context
     * @param serviceBundle
     *            the service bundle
     * @param dataObjectId
     *            id of the Data Object for which the lookup will be created
     * @param lookupName
     *            Optional Custom Lookup Name
     * @return The id of the newly created lookup
     * @throws IncorrectResultSizeDataAccessException
     *             If there was an underlying {@link IncorrectResultSizeDataAccessException}
     */
    private static long generateDataObjectLookup(final ExecutionContext etk, final ServiceBundle serviceBundle,
        final long dataObjectId, final String lookupName)
        throws IncorrectResultSizeDataAccessException {
        final long lookupId;

        /* keys: NAME_ID, ORDER_ID, START_DATE_ID, END_DATE_ID, OBJECT_NAME, BUSINESS_KEY */
        final Map<String, Object> dataObjectInfo = etk.createSQL(
            "SELECT (SELECT dataElement.data_element_id FROM etk_data_element dataElement WHERE dataElement.data_object_id = dataObject.data_object_id AND dataElement.element_name = 'name') NAME_ID, (SELECT dataElement.data_element_id FROM etk_data_element dataElement WHERE dataElement.data_object_id = dataObject.data_object_id AND dataElement.element_name = 'order') ORDER_ID, (SELECT dataElement.data_element_id FROM etk_data_element dataElement WHERE dataElement.data_object_id = dataObject.data_object_id AND dataElement.element_name = 'startDate') START_DATE_ID, (SELECT dataElement.data_element_id FROM etk_data_element dataElement WHERE dataElement.data_object_id = dataObject.data_object_id AND dataElement.element_name = 'endDate') END_DATE_ID, dataObject.name OBJECT_NAME, dataobject.business_key FROM etk_data_object dataObject WHERE dataObject.data_object_id = :dataObjectId")
            .setParameter("dataObjectId", dataObjectId)
            .fetchMap();

        final String dataObjectName = (String) dataObjectInfo.get("OBJECT_NAME");
        final String dataObjectBusinessKey = (String) dataObjectInfo.get("BUSINESS_KEY");
        final String businessKeyPart = dataObjectBusinessKey.substring(dataObjectBusinessKey.indexOf('.') + 1);
        // TODO: lookupBusinessKey should now be based off of the lookup name, not the object name?
        // TODO: Should the UI include a separate read-only field for lookup business key?
        // TODO: Does any validation have to be done against lookup name?
        final String lookupBusinessKey = String.format("lookup.%s", businessKeyPart);

        final Map<String, Object> queryParameters = Utility.arrayToMap(String.class, Object.class, new Object[][] {
            { "lookupSourceType", LookupSourceType.DATA_OBJECT_LOOKUP.getEntellitrakNumber() },
            { "dataObjectId", dataObjectId },
            { "valueElementId", null },
            { "displayElementId", dataObjectInfo.get("NAME_ID") },
            { "orderByElementId", dataObjectInfo.get("ORDER_ID") },
            { "ascendingOrder", null },
            { "startDateElementId", dataObjectInfo.get("START_DATE_ID") },
            { "endDateElementId", dataObjectInfo.get("END_DATE_ID") },
            { "sqlScriptObjectId", null },
            { "pluginRegistrationId", null },
            { "valueReturnType", null },
            { "trackingConfigId", Utility.getTrackingConfigIdNext(etk) },
            { "businessKey", lookupBusinessKey },
            { "name", !Utility.isBlank(lookupName) ? lookupName : dataObjectName },
            { "description", String.format("Lookup of %s", dataObjectName) },
            { "enableCaching", 0 },
        });

        if (Utility.isSqlServer(etk)) {
            lookupId = etk.createSQL(
                "INSERT INTO etk_lookup_definition(lookup_source_type, data_object_id, value_element_id, display_element_id, order_by_element_id, ascending_order, start_date_element_id, end_date_element_id, sql_script_object_id, plugin_registration_id, value_return_type, tracking_config_id, business_key, name, description, enable_caching) VALUES(:lookupSourceType, :dataObjectId, :valueElementId, :displayElementId, :orderByElementId, :ascendingOrder, :startDateElementId, :endDateElementId, :sqlScriptObjectId, :pluginRegistrationId, :valueReturnType, :trackingConfigId, :businessKey, :name, :description, :enableCaching)")
                .setParameter(queryParameters)
                .execute("LOOKUP_DEFINITION_ID");
        } else if (Utility.isPostgreSQL(etk)) {
            lookupId = etk.createSQL(
                "INSERT INTO etk_lookup_definition(lookup_source_type, data_object_id, value_element_id, display_element_id, order_by_element_id, ascending_order, start_date_element_id, end_date_element_id, sql_script_object_id, plugin_registration_id, value_return_type, tracking_config_id, business_key, name, description, enable_caching) VALUES(:lookupSourceType, :dataObjectId, :valueElementId, :displayElementId, :orderByElementId, :ascendingOrder, :startDateElementId, :endDateElementId, :sqlScriptObjectId, :pluginRegistrationId, :valueReturnType, :trackingConfigId, :businessKey, :name, :description, :enableCaching) returning LOOKUP_DEFINITION_ID")
                .setParameter(queryParameters)
                .execute("LOOKUP_DEFINITION_ID");
        } else {
            lookupId = DatabaseSequence.HIBERNATE_SEQUENCE.getNextVal(etk);

            queryParameters.put("lookupDefinitionId", lookupId);

            etk.createSQL(
                "INSERT INTO etk_lookup_definition(lookup_definition_id, lookup_source_type, data_object_id, value_element_id, display_element_id, order_by_element_id, ascending_order, start_date_element_id, end_date_element_id, sql_script_object_id, plugin_registration_id, value_return_type, tracking_config_id, business_key, name, description, enable_caching) VALUES(:lookupDefinitionId, :lookupSourceType, :dataObjectId, :valueElementId, :displayElementId, :orderByElementId, :ascendingOrder, :startDateElementId, :endDateElementId, :sqlScriptObjectId, :pluginRegistrationId, :valueReturnType, :trackingConfigId, :businessKey, :name, :description, :enableCaching)")
                .setParameter(queryParameters)
                .execute();
        }

        BundleUtility.insertBundleMapping(etk, serviceBundle, BundleComponentType.LOOKUP_DEFINITION, lookupBusinessKey);

        return lookupId;
    }
}
